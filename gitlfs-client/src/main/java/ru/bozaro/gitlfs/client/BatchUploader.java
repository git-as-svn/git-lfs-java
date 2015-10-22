package ru.bozaro.gitlfs.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.client.io.StreamProvider;
import ru.bozaro.gitlfs.common.data.*;
import ru.bozaro.gitlfs.common.data.Error;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static ru.bozaro.gitlfs.common.Constants.BATCH_SIZE;

/**
 * Batching uploader client.
 *
 * @author Artem V. Navrotskiy
 */
public class BatchUploader {
  private static final int BATCH_TRASHOLD = 10;
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

  public BatchUploader(@NotNull Client client, @NotNull ExecutorService pool) {
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
    if (uploadInProgress.get() > BATCH_TRASHOLD) {
      return;
    }
    if (batchInProgress.compareAndSet(false, true)) {
      try {
        pool.execute(new Runnable() {
          @Override
          public void run() {
            try {
              final Map<String, UploadState> batch = new HashMap<>();
              uploadQueue.values().stream().filter(state -> state.location == null).limit(BATCH_SIZE).forEach(state -> {
                // todo: check for cancelled future
                batch.put(state.meta.getOid(), state);
              });
              if (!batch.isEmpty()) {
                final List<Meta> metas = batch.values().stream().map(s -> s.meta).collect(Collectors.toList());
                BatchRes result = client.postBatch(new BatchReq(Operation.Upload, metas));
                for (BatchItem item : result.getObjects()) {
                  UploadState state = batch.remove(item.getOid());
                  if (state != null) {
                    final Error error = item.getError();
                    if (error != null) {
                      state.future.completeExceptionally(new IOException("Can't get upload location (code " + error.getCode() + "): " + error.getMessage()));
                    }
                    if (item.getLinks().containsKey(LinkType.Download)) {
                      uploadQueue.remove(state.meta.getOid(), state);
                      state.future.complete(state.meta);
                      continue;
                    }
                    if (!item.getLinks().containsKey(LinkType.Upload)) {
                      state.future.completeExceptionally(new IOException("Upload link not found"));
                      continue;
                    }
                    final UploadLocation location = new UploadLocation(item);
                    state.location = location;
                    try {
                      uploadInProgress.incrementAndGet();
                      pool.execute(new Runnable() {
                        @Override
                        public void run() {
                          try {
                            uploadFile(state, location);
                          } finally {
                            uploadInProgress.decrementAndGet();
                            tryBatchRequest();
                          }
                        }
                      });
                    } catch (Throwable e) {
                      state.location = null;
                      throw e;
                    }
                  }
                }
                for (UploadState value : batch.values()) {
                  value.future.completeExceptionally(new IOException("Requested object not found in server responce: " + value.meta.getOid()));
                }
              }
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

  private void uploadFile(@NotNull UploadState state, @NotNull UploadLocation location) {
    try {
      state.location = location;
      client.putObject(state.provider, state.meta, location.links);
      uploadQueue.remove(state.meta.getOid(), state);
      state.future.complete(state.meta);
    } catch (Throwable e) {
      // todo: Auth error
      state.future.completeExceptionally(e);
    } finally {
      state.location = null;
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
    private volatile UploadLocation location;

    public UploadState(@NotNull StreamProvider provider, @NotNull Meta meta) {
      this.provider = provider;
      this.meta = meta;
      this.location = null;
    }
  }

  private final static class UploadLocation {
    @NotNull
    private final Links links;

    public UploadLocation(@NotNull Links links) {
      this.links = links;
    }
  }
}
