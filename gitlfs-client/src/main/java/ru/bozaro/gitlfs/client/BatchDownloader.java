package ru.bozaro.gitlfs.client;

import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.common.data.Meta;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

/**
 * Batching downloader client.
 *
 * @author Artem V. Navrotskiy
 */
public class BatchDownloader {
  @FunctionalInterface
  public interface StreamConsumer {
    void accept(@NotNull InputStream inputStream) throws IOException;
  }

  @NotNull
  private final Client client;

  public BatchDownloader(@NotNull Client client) {
    this.client = client;
  }

  @NotNull
  public CompletableFuture<?> download(@NotNull final Meta meta, @NotNull StreamConsumer callback) {
    return null;
  }

  public void flush() {
  }
}
