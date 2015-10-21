package ru.bozaro.gitlfs.client;

import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.common.data.Meta;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Batching downloader client.
 *
 * @author Artem V. Navrotskiy
 */
public class BatchDownloader {
  @FunctionalInterface
  public interface StreamConsumer<T> {
    T accept(@NotNull InputStream inputStream) throws IOException;
  }

  @NotNull
  private final Client client;

  public BatchDownloader(@NotNull Client client, @NotNull ExecutorService pool) {
    this.client = client;
  }

  @NotNull
  public <T> CompletableFuture<T> download(@NotNull final Meta meta, @NotNull StreamConsumer<T> callback) {
    return null;
  }

  public void flush() {
  }
}
