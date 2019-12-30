package ru.bozaro.gitlfs.client.io;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Create stream by bytes.
 *
 * @author Artem V. Navrotskiy
 */
public class ByteArrayStreamProvider implements StreamProvider {
  @Nonnull
  private final byte[] data;

  public ByteArrayStreamProvider(@Nonnull byte[] data) {
    this.data = data;
  }

  @Nonnull
  @Override
  public InputStream getStream() {
    return new ByteArrayInputStream(data);
  }
}
