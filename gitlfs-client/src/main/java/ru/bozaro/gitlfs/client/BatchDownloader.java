package ru.bozaro.gitlfs.client;

import ru.bozaro.gitlfs.client.internal.BatchWorker;
import ru.bozaro.gitlfs.client.internal.Work;
import ru.bozaro.gitlfs.client.io.StreamHandler;
import ru.bozaro.gitlfs.common.data.BatchItem;
import ru.bozaro.gitlfs.common.data.LinkType;
import ru.bozaro.gitlfs.common.data.Meta;
import ru.bozaro.gitlfs.common.data.Operation;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Batching downloader client.
 *
 * @author Artem V. Navrotskiy
 */
public class BatchDownloader extends BatchWorker<StreamHandler<?>, Object> {
  public BatchDownloader(@Nonnull Client client, @Nonnull ExecutorService pool) {
    this(client, pool, new BatchSettings());
  }

  public BatchDownloader(@Nonnull Client client, @Nonnull ExecutorService pool, @Nonnull BatchSettings settings) {
    super(client, pool, settings, Operation.Download);
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  public <T> CompletableFuture<T> download(@Nonnull final Meta meta, @Nonnull StreamHandler<T> callback) {
    return (CompletableFuture<T>) enqueue(meta, callback);
  }

  @CheckForNull
  protected Work<Object> objectTask(@Nonnull State<StreamHandler<?>, Object> state, @Nonnull BatchItem item) {
    // Invalid links data
    if (!item.getLinks().containsKey(LinkType.Download)) {
      state.getFuture().completeExceptionally(new IOException("Download link not found"));
      return null;
    }
    // Already processed
    return auth -> getClient().getObject(item, item, state.getContext());
  }
}
