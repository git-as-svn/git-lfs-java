package ru.bozaro.gitlfs.client.io;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for handle stream of downloading data.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public interface StreamHandler<T> {
  @NotNull
  T accept(@NotNull InputStream inputStream) throws IOException;
}
