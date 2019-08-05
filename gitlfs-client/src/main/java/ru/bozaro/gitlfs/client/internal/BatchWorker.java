package ru.bozaro.gitlfs.client.internal;

import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.bozaro.gitlfs.client.AuthHelper;
import ru.bozaro.gitlfs.client.BatchSettings;
import ru.bozaro.gitlfs.client.Client;
import ru.bozaro.gitlfs.client.exceptions.ForbiddenException;
import ru.bozaro.gitlfs.client.exceptions.UnauthorizedException;
import ru.bozaro.gitlfs.common.data.Error;
import ru.bozaro.gitlfs.common.data.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
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

  private final Logger log = LoggerFactory.getLogger(BatchWorker.class);

  @NotNull
  private final Client client;
  @NotNull
  private final ExecutorService pool;

  @NotNull
  private final AtomicInteger batchSequence = new AtomicInteger(0);
  @NotNull
  private final AtomicInteger batchInProgress = new AtomicInteger();

  @NotNull
  private final ConcurrentMap<String, State<T, R>> objectQueue = new ConcurrentHashMap<>();
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
        state = newState;
        stateEnqueue(true);
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

  private void stateEnqueue(boolean pooled) {
    int batchId = batchSequence.incrementAndGet();
    tryBatchRequest(batchId, pooled);
  }

  private boolean tryBatchRequestPredicate() {
    return objectInProgress.get() < settings.getThreshold()
        && !objectQueue.isEmpty();
  }

  private boolean tryBatchRequest(int batchId, boolean pooled) {
    if (!tryBatchRequestPredicate()) {
      return false;
    }
    if (batchInProgress.compareAndSet(0, batchId)) {
      executeInPool(
          "batch request: " + objectQueue.size() + " in queue",
          () -> {
            int curBatchId = batchId;
            while (true) {
              try {
                submitBatchTask();
              } finally {
                batchInProgress.set(0);
              }
              int newBatchId = batchSequence.get();
              if ((newBatchId == curBatchId) && (!tryBatchRequestPredicate())) {
                break;
              }
              curBatchId = newBatchId;
              if (!batchInProgress.compareAndSet(0, curBatchId)) {
                break;
              }
            }
          },
          () -> batchInProgress.compareAndSet(batchId, 0),
          pooled
      );
      return true;
    }
    return false;
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
        final BatchRes result = client.doRequest(auth, new JsonPost<>(new BatchReq(operation, metas), BatchRes.class), AuthHelper.join(auth.getHref(), PATH_BATCH), Client.ConnectionClosePolicy.Close);
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
          value.future.completeExceptionally(new IOException("Requested object not found in server response: " + value.meta.getOid()));
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

  /**
   * Submit object processing task.
   *
   * @param state Current object state
   * @param item  Metadata information with upload/download urls.
   * @param auth  Urls authentication state.
   */
  private void submitTask(@NotNull State<T, R> state, @NotNull BatchItem item, @NotNull Link auth) {
    // Submit task
    final StateHolder holder = new StateHolder(state);
    try {
      state.auth = auth;
      final Work<R> worker = objectTask(state, item);
      if (state.future.isDone()) {
        holder.close();
        return;
      }
      if (worker == null) {
        throw new IllegalStateException("Uncompleted task worker is null: " + item);
      }
      executeInPool(
          "task: " + state.getMeta().getOid(),
          () -> processObject(state, auth, worker),
          holder::close,
          true
      );
    } catch (Throwable e) {
      holder.close();
      throw e;
    }
  }

  @NotNull
  private Map<String, State<T, R>> takeBatch() {
    final Map<String, State<T, R>> batch = new HashMap<>();
    final List<State<T, R>> completed = new ArrayList<>();
    for (State<T, R> state : objectQueue.values()) {
      if (state.future.isDone()) {
        completed.add(state);
        continue;
      }
      if (state.auth == null) {
        batch.put(state.meta.getOid(), state);
        if (batch.size() >= settings.getLimit()) {
          break;
        }
      }
    }
    for (final State<T, R> state : completed) {
      objectQueue.remove(state.meta.getOid(), state);
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

  /**
   * Schedule task in thread pool.
   * <p>
   * If pool reject task - task will execute immediately in current thread.
   * <p>
   * If pool is shutdown - task will not run, but finalizer will executed.
   *
   * @param name      Task name for debug
   * @param task      Task runnable
   * @param finalizer Finalizer to execute like 'try-final' block
   */
  private void executeInPool(@NotNull String name, @NotNull Runnable task, @Nullable Runnable finalizer, boolean pooled) {
    if (pool.isShutdown()) {
      log.warn("Thread pool is shutdown");
      if (finalizer != null) {
        finalizer.run();
      }
      return;
    }
    if (!pooled) {
      log.debug("Begin: " + name);
      try {
        task.run();
      } catch (Throwable e) {
        log.error("Execute exception: " + e);
        if (finalizer != null) {
          finalizer.run();
        }
        throw e;
      } finally {
        if (finalizer != null) {
          finalizer.run();
        }
        log.debug("End: " + name);
      }
      return;
    }
    try {
      pool.execute(new Runnable() {
        @Override
        public void run() {
          executeInPool(name, task, finalizer, false);
        }

        @Override
        public String toString() {
          return name;
        }
      });
    } catch (RejectedExecutionException e) {
      if (pool.isShutdown()) {
        log.warn("Thread pool is shutdown");
      } else {
        executeInPool(name, task, finalizer, false);
      }
    } catch (Throwable e) {
      log.error("Execute in pool exception: " + e);
      if (finalizer != null) {
        finalizer.run();
      }
      throw e;
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

  private final class StateHolder implements AutoCloseable {
    @NotNull
    private AtomicReference<State<T, R>> stateRef;

    public StateHolder(@NotNull State<T, R> state) {
      this.stateRef = new AtomicReference<>(state);
      objectInProgress.incrementAndGet();
    }

    @Override
    public void close() {
      final State<T, R> state = stateRef.getAndSet(null);
      if (state == null) return;
      if (state.future.isDone()) {
        objectInProgress.decrementAndGet();
        objectQueue.remove(state.meta.getOid(), state);
      } else {
        state.auth = null;
        objectInProgress.decrementAndGet();
      }
      stateEnqueue(false);
    }
  }
}
