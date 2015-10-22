package ru.bozaro.gitlfs.client;

import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.client.io.StreamProvider;
import ru.bozaro.gitlfs.common.data.Meta;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Batching uploader client.
 *
 * @author Artem V. Navrotskiy
 */
public class BatchUploader {
  @NotNull
  private final Client client;
  @NotNull
  private final ExecutorService pool;

  @NotNull
  private final ConcurrentMap<String, UploadState> uploadQueue = new ConcurrentHashMap<>();
  @NotNull
  private final AtomicBoolean batchInProgress = new AtomicBoolean();

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
    // todo: Send batch request.
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

    public UploadState(@NotNull StreamProvider provider, @NotNull Meta meta) {
      this.provider = provider;
      this.meta = meta;
    }
  }
}
