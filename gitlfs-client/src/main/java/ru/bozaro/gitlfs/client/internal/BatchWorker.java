package ru.bozaro.gitlfs.client.internal;

import org.apache.commons.httpclient.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.client.AuthHelper;
import ru.bozaro.gitlfs.client.BatchSettings;
import ru.bozaro.gitlfs.client.Client;
import ru.bozaro.gitlfs.client.exceptions.ForbiddenException;
import ru.bozaro.gitlfs.client.exceptions.UnauthorizedException;
import ru.bozaro.gitlfs.common.data.*;
import ru.bozaro.gitlfs.common.data.Error;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static ru.bozaro.gitlfs.common.Constants.PATH_BATCH;

/**
 * Base batch client.
 *
 * @author Artem V. Navrotskiy
 */
public abstract class BatchWorker<T, R> {
  @NotNull
  private final Client client;
  @NotNull
  private final ExecutorService pool;

  @NotNull
  private final ConcurrentMap<String, State<T, R>> objectQueue = new ConcurrentHashMap<>();
  @NotNull
  private final AtomicBoolean batchInProgress = new AtomicBoolean(false);
  @NotNull
  private final AtomicInteger objectInProgress = new AtomicInteger(0);
  @NotNull
  private final AtomicReference<Link> currentAuth = new AtomicReference<>(null);
  @NotNull
  private final BatchSettings settings;
  @NotNull
  private final Operation operation;

  public BatchWorker(@NotNull Client client, @NotNull ExecutorService pool, @NotNull BatchSettings settings, @NotNull Operation operation) {
    this.settings = settings;
    this.client = client;
    this.pool = pool;
    this.operation = operation;
  }

  /**
   * This method start send object metadata to server.
   *
   * @param context Object worker context.
   * @param meta    Object metadata.
   * @return Return future with result. For same objects can return same future.
   */
  @NotNull
  protected CompletableFuture<R> enqueue(@NotNull final Meta meta, @NotNull final T context) {
    State<T, R> state = objectQueue.get(meta.getOid());
    if (state != null) {
      if (state.future.isCancelled()) {
        objectQueue.remove(meta.getOid(), state);
        state = null;
      }
    }
    if (state == null) {
      final State<T, R> newState = new State<>(meta, context);
      state = objectQueue.putIfAbsent(meta.getOid(), newState);
      if (state == null) {
        tryBatchRequest();
        state = newState;
      }
    }
    return state.future;
  }

  @NotNull
  protected ExecutorService getPool() {
    return pool;
  }

  @NotNull
  protected Client getClient() {
    return client;
  }

  private void tryBatchRequest() {
    if (objectInProgress.get() > settings.getThreshold()) {
      return;
    }
    if (batchInProgress.compareAndSet(false, true)) {
      try {
        pool.execute(() -> {
          try {
            submitBatchTask();
          } finally {
            batchInProgress.compareAndSet(true, false);
          }
          tryBatchRequest();
        });
      } catch (Throwable e) {
        batchInProgress.set(false);
        throw e;
      }
    }
  }

  private void invalidateAuth(@NotNull Link auth) {
    if (currentAuth.compareAndSet(auth, null)) {
      client.getAuthProvider().invalidateAuth(operation, auth);
    }
  }

  private void submitBatchTask() {
    final Map<String, State<T, R>> batch = takeBatch();
    Link auth = currentAuth.get();
    try {
      if (!batch.isEmpty()) {
        if (auth == null) {
          auth = client.getAuthProvider().getAuth(operation);
          currentAuth.set(auth);
        }
        final List<Meta> metas = batch.values().stream().map(s -> s.meta).collect(Collectors.toList());
        final BatchRes result = client.doRequest(auth, new JsonPost<>(new BatchReq(operation, metas), BatchRes.class), AuthHelper.join(auth.getHref(), PATH_BATCH));
        for (BatchItem item : result.getObjects()) {
          final State<T, R> state = batch.remove(item.getOid());
          if (state != null) {
            final Error error = item.getError();
            if (error != null) {
              objectQueue.remove(item.getOid(), state);
              state.future.completeExceptionally(createError(error));
            } else {
              submitTask(state, item, auth);
            }
          }
        }
        for (State value : batch.values()) {
          value.future.completeExceptionally(new IOException("Requested object not found in server responce: " + value.meta.getOid()));
        }
      }
    } catch (UnauthorizedException e) {
      if (auth != null) {
        invalidateAuth(auth);
      }
    } catch (IOException e) {
      for (State state : batch.values()) {
        state.onException(e, state.retry);
      }
    }
    tryBatchRequest();
  }

  @NotNull
  protected Throwable createError(@NotNull Error error) {
    if (error.getCode() == HttpStatus.SC_NOT_FOUND) {
      return new FileNotFoundException(error.getMessage());
    }
    return new IOException("Can't process object (code " + error.getCode() + "): " + error.getMessage());
  }

  @Nullable
  protected abstract Work<R> objectTask(@NotNull State<T, R> state, @NotNull BatchItem item);

  private void submitTask(@NotNull State<T, R> state, @NotNull BatchItem item, @NotNull Link auth) {
    // Submit task
    state.auth = auth;
    try {
      objectInProgress.incrementAndGet();
      final Work<R> worker = objectTask(state, item);
      if (state.future.isDone()) {
        objectQueue.remove(state.meta.getOid(), state);
        objectInProgress.decrementAndGet();
        return;
      }
      if (worker != null) {
        pool.execute(() -> {
          try {
            processObject(state, auth, worker);
          } finally {
            objectInProgress.decrementAndGet();
            tryBatchRequest();
          }
        });
      }
    } catch (Throwable e) {
      objectInProgress.decrementAndGet();
      state.auth = null;
      throw e;
    }
  }

  @NotNull
  private Map<String, State<T, R>> takeBatch() {
    final Map<String, State<T, R>> batch = new HashMap<>();
    final Iterator<State<T, R>> iter = objectQueue.values().iterator();
    while (iter.hasNext()) {
      final State<T, R> state = iter.next();
      if (state.future.isDone()) {
        iter.remove();
        continue;
      }
      if (state.auth == null) {
        batch.put(state.meta.getOid(), state);
        if (batch.size() >= settings.getLimit()) {
          break;
        }
      }
    }
    return batch;
  }

  private void processObject(@NotNull State<T, R> state, @NotNull Link auth, @NotNull Work<R> worker) {
    if (currentAuth.get() != auth) {
      state.auth = null;
      return;
    }
    try {
      state.auth = auth;
      final R result = worker.exec(auth);
      objectQueue.remove(state.meta.getOid(), state);
      state.future.complete(result);
    } catch (UnauthorizedException e) {
      invalidateAuth(auth);
    } catch (ForbiddenException e) {
      state.onException(e, 0);
    } catch (Throwable e) {
      state.onException(e, settings.getRetryCount());
    } finally {
      state.auth = null;
    }
  }

  public final static class State<T, R> {
    @NotNull
    private final Meta meta;
    @NotNull
    private final T context;
    @NotNull
    private final CompletableFuture<R> future = new CompletableFuture<>();
    @Nullable
    private volatile Link auth;
    private int retry;

    public State(@NotNull Meta meta, @NotNull T context) {
      this.context = context;
      this.meta = meta;
      this.auth = null;
    }

    public void onException(@NotNull Throwable e, int maxRetryCount) {
      retry++;
      if (retry >= maxRetryCount) {
        future.completeExceptionally(e);
      }
      auth = null;
    }

    @NotNull
    public Meta getMeta() {
      return meta;
    }

    @NotNull
    public T getContext() {
      return context;
    }

    @NotNull
    public CompletableFuture<R> getFuture() {
      return future;
    }
  }
}
