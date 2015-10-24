package ru.bozaro.gitlfs.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.client.internal.BatchWorker;
import ru.bozaro.gitlfs.client.internal.Work;
import ru.bozaro.gitlfs.client.io.StreamProvider;
import ru.bozaro.gitlfs.common.data.BatchItem;
import ru.bozaro.gitlfs.common.data.LinkType;
import ru.bozaro.gitlfs.common.data.Meta;
import ru.bozaro.gitlfs.common.data.Operation;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Batching uploader client.
 *
 * @author Artem V. Navrotskiy
 */
public class BatchUploader extends BatchWorker<StreamProvider, Meta> {
  public BatchUploader(@NotNull Client client, @NotNull ExecutorService pool) {
    this(client, pool, new BatchSettings());
  }

  public BatchUploader(@NotNull Client client, @NotNull ExecutorService pool, @NotNull BatchSettings settings) {
    super(client, pool, settings, Operation.Upload);
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
  @NotNull
  public CompletableFuture<Meta> upload(@NotNull final Meta meta, @NotNull final StreamProvider streamProvider) {
    return enqueue(meta, streamProvider);
  }

  @Nullable
  protected Work<Meta> objectTask(@NotNull State<StreamProvider, Meta> state, @NotNull BatchItem item) {
    // Already processed
    if (item.getLinks().containsKey(LinkType.Download)) {
      state.getFuture().complete(state.getMeta());
      return null;
    }
    // Invalid links data
    if (!item.getLinks().containsKey(LinkType.Upload)) {
      state.getFuture().completeExceptionally(new IOException("Upload link not found"));
      return null;
    }
    return auth -> {
      getClient().putObject(state.getContext(), state.getMeta(), item);
      return null;
    };
  }
}
