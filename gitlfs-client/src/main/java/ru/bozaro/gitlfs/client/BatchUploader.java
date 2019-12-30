package ru.bozaro.gitlfs.client;

import ru.bozaro.gitlfs.client.internal.BatchWorker;
import ru.bozaro.gitlfs.client.internal.Work;
import ru.bozaro.gitlfs.client.io.StreamProvider;
import ru.bozaro.gitlfs.common.data.BatchItem;
import ru.bozaro.gitlfs.common.data.LinkType;
import ru.bozaro.gitlfs.common.data.Meta;
import ru.bozaro.gitlfs.common.data.Operation;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Batching uploader client.
 *
 * @author Artem V. Navrotskiy
 */
public class BatchUploader extends BatchWorker<StreamProvider, Meta> {
  public BatchUploader(@Nonnull Client client, @Nonnull ExecutorService pool) {
    this(client, pool, new BatchSettings());
  }

  public BatchUploader(@Nonnull Client client, @Nonnull ExecutorService pool, @Nonnull BatchSettings settings) {
    super(client, pool, settings, Operation.Upload);
  }

  /**
   * This method computes stream metadata and upload object.
   *
   * @param streamProvider Stream provider.
   * @return Return future with upload result.
   */
  @Nonnull
  public CompletableFuture<Meta> upload(@Nonnull final StreamProvider streamProvider) {
    final CompletableFuture<Meta> future = new CompletableFuture<>();
    getPool().submit(() -> {
      try {
        future.complete(Client.generateMeta(streamProvider));
      } catch (Throwable e) {
        future.completeExceptionally(e);
      }
    });
    return future.thenCompose(meta -> upload(meta, streamProvider));
  }

  /**
   * This method start uploading object to server.
   *
   * @param meta           Object metadata.
   * @param streamProvider Stream provider.
   * @return Return future with upload result. For same objects can return same future.
   */
  @Nonnull
  public CompletableFuture<Meta> upload(@Nonnull final Meta meta, @Nonnull final StreamProvider streamProvider) {
    return enqueue(meta, streamProvider);
  }

  @CheckForNull
  protected Work<Meta> objectTask(@Nonnull State<StreamProvider, Meta> state, @Nonnull BatchItem item) {
    if (item.getLinks().containsKey(LinkType.Upload)) {
      // Wait for upload.
      return auth -> {
        getClient().putObject(state.getContext(), state.getMeta(), item);
        return null;
      };
    } else {
      // Already uploaded.
      state.getFuture().complete(state.getMeta());
      return null;
    }
  }
}
