package ru.bozaro.gitlfs.client;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * Stream provider.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public interface StreamProvider {
  @NotNull
  InputStream getStream() throws IOException;
}
