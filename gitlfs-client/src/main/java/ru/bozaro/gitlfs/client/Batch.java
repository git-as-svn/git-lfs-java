package ru.bozaro.gitlfs.client;

import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.client.io.StreamProvider;
import ru.bozaro.gitlfs.common.data.Meta;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

/**
 * Batch API client.
 *
 * @author Artem V. Navrotskiy
 */
public class Batch {
  @FunctionalInterface
  public interface StreamConsumer {
    void accept(@NotNull InputStream inputStream) throws IOException;
  }

  @NotNull
  private final Client client;

  public Batch(@NotNull Client client) {
    this.client = client;
  }

  @NotNull
  public CompletableFuture<?> download(@NotNull final Meta meta, @NotNull StreamConsumer callback) {
    return null;
  }

  @NotNull
  public CompletableFuture<Meta> upload(@NotNull final StreamProvider streamProvider) {
    return null;
  }

  @NotNull
  public CompletableFuture<Meta> upload(@NotNull final StreamProvider streamProvider, @NotNull final Meta meta) {
    return null;
  }
}
