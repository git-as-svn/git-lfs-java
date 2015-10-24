package ru.bozaro.gitlfs.client.io;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * Stream provider.
 *
 * @author Artem V. Navrotskiy
 */
@FunctionalInterface
public interface StreamProvider {
  @NotNull
  InputStream getStream() throws IOException;
}
