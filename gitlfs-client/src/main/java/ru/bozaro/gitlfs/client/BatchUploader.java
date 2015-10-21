package ru.bozaro.gitlfs.client;

import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.client.io.StreamProvider;
import ru.bozaro.gitlfs.common.data.Meta;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;

/**
 * Batching uploader client.
 *
 * @author Artem V. Navrotskiy
 */
public class BatchUploader {
  @FunctionalInterface
  public interface StreamConsumer {

    void accept(@NotNull InputStream inputStream) throws IOException;
  }

  @NotNull
  private final Client client;
  @NotNull
  private final ExecutorService pool;

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

  @NotNull
  public CompletionStage<Meta> upload(@NotNull final StreamProvider streamProvider, @NotNull final Meta meta) {
    return null;
  }

  public void flush() {
  }
}
