package ru.bozaro.gitlfs.client.io;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * Stream provider.
 *
 * @author Artem V. Navrotskiy
 */
@FunctionalInterface
public interface StreamProvider {
  @Nonnull
  InputStream getStream() throws IOException;
}
