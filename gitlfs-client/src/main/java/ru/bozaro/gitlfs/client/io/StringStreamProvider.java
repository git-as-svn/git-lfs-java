package ru.bozaro.gitlfs.client.io;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;

/**
 * Create stream from string.
 *
 * @author Artem V. Navrotskiy
 */
public class StringStreamProvider extends ByteArrayStreamProvider {
  public StringStreamProvider(@Nonnull String data) {
    super(data.getBytes(StandardCharsets.UTF_8));
  }
}
