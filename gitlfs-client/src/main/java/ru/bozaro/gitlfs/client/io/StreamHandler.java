package ru.bozaro.gitlfs.client.io;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for handle stream of downloading data.
 *
 * @author Artem V. Navrotskiy
 */
@FunctionalInterface
public interface StreamHandler<T> {
  @Nonnull
  T accept(@Nonnull InputStream inputStream) throws IOException;
}
