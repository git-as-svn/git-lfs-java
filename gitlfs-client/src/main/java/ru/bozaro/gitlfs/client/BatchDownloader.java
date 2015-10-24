package ru.bozaro.gitlfs.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.client.internal.BatchWorker;
import ru.bozaro.gitlfs.client.internal.Work;
import ru.bozaro.gitlfs.client.io.StreamHandler;
import ru.bozaro.gitlfs.common.data.BatchItem;
import ru.bozaro.gitlfs.common.data.LinkType;
import ru.bozaro.gitlfs.common.data.Meta;
import ru.bozaro.gitlfs.common.data.Operation;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Batching downloader client.
 *
 * @author Artem V. Navrotskiy
 */
public class BatchDownloader extends BatchWorker<StreamHandler<?>, Object> {
  public BatchDownloader(@NotNull Client client, @NotNull ExecutorService pool) {
    this(client, pool, new BatchSettings());
  }

  public BatchDownloader(@NotNull Client client, @NotNull ExecutorService pool, @NotNull BatchSettings settings) {
    super(client, pool, settings, Operation.Download);
  }

  @SuppressWarnings("unchecked")
  @NotNull
  public <T> CompletableFuture<T> download(@NotNull final Meta meta, @NotNull StreamHandler<T> callback) {
    return (CompletableFuture<T>) enqueue(meta, callback);
  }

  @Nullable
  protected Work<Object> objectTask(@NotNull State<StreamHandler<?>, Object> state, @NotNull BatchItem item) {
    // Invalid links data
    if (!item.getLinks().containsKey(LinkType.Download)) {
      state.getFuture().completeExceptionally(new IOException("Download link not found"));
      return null;
    }
    // Already processed
    return auth -> getClient().getObject(item, state.getContext());
  }
}
