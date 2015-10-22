package ru.bozaro.gitlfs.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.client.exceptions.UnauthorizedException;
import ru.bozaro.gitlfs.client.internal.JsonPost;
import ru.bozaro.gitlfs.client.io.StreamProvider;
import ru.bozaro.gitlfs.common.data.*;
import ru.bozaro.gitlfs.common.data.Error;

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

import static ru.bozaro.gitlfs.common.Constants.BATCH_SIZE;
import static ru.bozaro.gitlfs.common.Constants.PATH_BATCH;

/**
 * Batching uploader client.
 *
 * @author Artem V. Navrotskiy
 */
public class BatchUploader {
  private static final int BATCH_THRESHOLD = 10;
  @NotNull
  private final Client client;
  @NotNull
  private final ExecutorService pool;

  @NotNull
  private final ConcurrentMap<String, UploadState> uploadQueue = new ConcurrentHashMap<>();
  @NotNull
  private final AtomicBoolean batchInProgress = new AtomicBoolean(false);
  @NotNull
  private final AtomicInteger uploadInProgress = new AtomicInteger(0);
  @NotNull
  private final AtomicReference<Link> currentAuth = new AtomicReference<>(null);
  private final int batchLimit;
  private final int batchThreshold;

  public BatchUploader(@NotNull Client client, @NotNull ExecutorService pool) {
    this(client, pool, BATCH_SIZE, BATCH_THRESHOLD);
  }

  public BatchUploader(@NotNull Client client, @NotNull ExecutorService pool, int batchLimit, int batchThreshold) {
    this.batchLimit = Math.min(batchLimit, 1);
    this.batchThreshold = Math.max(batchThreshold, 0);
    this.client = client;
    this.pool = pool;
  }

  /**
   * This method computes stream metadata and upload object.
   *
   * @param streamProvider Stream provider.
   * @return Return future with upload result.
   */
  @NotNull
  public CompletableFuture<Meta> upload(@NotNull final StreamProvider streamProvider) {
    final CompletableFuture<Meta> future = new CompletableFuture<>();
    pool.submit(() -> {
      try {
        future.complete(client.generateMeta(streamProvider));
      } catch (Throwable e) {
        future.completeExceptionally(e);
      }
    });
    return future.thenCompose(meta -> upload(streamProvider, meta));
  }

  /**
   * This method start uploading object to server.
   *
   * @param streamProvider Stream provider.
   * @param meta           Object metadata.
   * @return Return future with upload result. For same objects can return same future.
   */
  @NotNull
  public CompletableFuture<Meta> upload(@NotNull final StreamProvider streamProvider, @NotNull final Meta meta) {
    UploadState state = uploadQueue.get(meta.getOid());
    if (state != null) {
      if (state.future.isCancelled()) {
        uploadQueue.remove(meta.getOid(), state);
        state = null;
      }
    }
    if (state == null) {
      final UploadState newState = new UploadState(streamProvider, meta);
      state = uploadQueue.putIfAbsent(meta.getOid(), newState);
      if (state == null) {
        tryBatchRequest();
        state = newState;
      }
    }
    return state.future;
  }

  private void tryBatchRequest() {
    if (uploadInProgress.get() > batchThreshold) {
      return;
    }
    if (batchInProgress.compareAndSet(false, true)) {
      try {
        pool.execute(new Runnable() {
          @Override
          public void run() {
            Link auth = currentAuth.get();
            try {
              final Map<String, UploadState> batch = takeBatch();
              if (!batch.isEmpty()) {
                if (auth == null) {
                  auth = client.getAuthProvider().getAuth(Operation.Upload);
                  currentAuth.set(auth);
                }
                final List<Meta> metas = batch.values().stream().map(s -> s.meta).collect(Collectors.toList());
                final BatchRes result = client.doRequest(auth, new JsonPost<>(new BatchReq(Operation.Upload, metas), BatchRes.class), AuthHelper.join(auth.getHref(), PATH_BATCH));
                for (BatchItem item : result.getObjects()) {
                  UploadState state = batch.remove(item.getOid());
                  if (state != null) {
                    final Error error = item.getError();
                    if (error != null) {
                      state.future.completeExceptionally(new IOException("Can't get upload location (code " + error.getCode() + "): " + error.getMessage()));
                    }
                    submitUploadTask(state, item, auth);
                  }
                }
                for (UploadState value : batch.values()) {
                  value.future.completeExceptionally(new IOException("Requested object not found in server responce: " + value.meta.getOid()));
                }
              }
            } catch (UnauthorizedException e) {
              invalidateAuth(auth);
            } catch (IOException e) {
              // todo: e.printStackTrace();
            } finally {
              batchInProgress.compareAndSet(true, false);
            }
            tryBatchRequest();
          }
        });
      } catch (Throwable e) {
        batchInProgress.set(false);
        throw e;
      }
    }
  }

  private void invalidateAuth(@NotNull Link auth) {
    if (currentAuth.compareAndSet(auth, null)) {
      client.getAuthProvider().invalidateAuth(Operation.Upload, auth);
    }
  }

  private void submitUploadTask(@NotNull UploadState state, @NotNull BatchItem item, @NotNull Link auth) {
    // Already uploaded
    if (item.getLinks().containsKey(LinkType.Download)) {
      uploadQueue.remove(state.meta.getOid(), state);
      state.future.complete(state.meta);
      return;
    }
    // Invalid links data
    if (!item.getLinks().containsKey(LinkType.Upload)) {
      state.future.completeExceptionally(new IOException("Upload link not found"));
      return;
    }
    // Submit upload task
    state.auth = auth;
    try {
      uploadInProgress.incrementAndGet();
      pool.execute(() -> {
        try {
          uploadFile(state, item, auth);
        } finally {
          uploadInProgress.decrementAndGet();
          tryBatchRequest();
        }
      });
    } catch (Throwable e) {
      uploadInProgress.decrementAndGet();
      state.auth = null;
      throw e;
    }
  }

  @NotNull
  private Map<String, UploadState> takeBatch() {
    final Map<String, UploadState> batch = new HashMap<>();
    final Iterator<UploadState> iter = uploadQueue.values().iterator();
    while (iter.hasNext()) {
      final UploadState state = iter.next();
      if (state.future.isDone()) {
        iter.remove();
        continue;
      }
      if (state.auth == null) {
        batch.put(state.meta.getOid(), state);
      }
    }
    return batch;
  }

  private void uploadFile(@NotNull UploadState state, @NotNull BatchItem links, @NotNull Link auth) {
    if (currentAuth.get() != auth) {
      state.auth = null;
      return;
    }
    try {
      state.auth = auth;
      client.putObject(state.provider, state.meta, links);
      uploadQueue.remove(state.meta.getOid(), state);
      state.future.complete(state.meta);
    } catch (UnauthorizedException e) {
      invalidateAuth(auth);
    } catch (Throwable e) {
      state.future.completeExceptionally(e);
    } finally {
      state.auth = null;
    }
  }

  public void flush() {
  }

  private final static class UploadState {
    @NotNull
    private final StreamProvider provider;
    @NotNull
    private final Meta meta;
    @NotNull
    private final CompletableFuture<Meta> future = new CompletableFuture<>();
    @Nullable
    private volatile Link auth;

    public UploadState(@NotNull StreamProvider provider, @NotNull Meta meta) {
      this.provider = provider;
      this.meta = meta;
      this.auth = null;
    }
  }
}
