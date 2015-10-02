package ru.bozaro.gitlfs.client;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

/**
 * Create stream from string.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class StringStreamProvider extends ByteArrayStreamProvider {
  public StringStreamProvider(@NotNull String data) {
    super(data.getBytes(StandardCharsets.UTF_8));
  }
}
