package ru.bozaro.gitlfs.client.io;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Create stream by URL.
 *
 * @author Artem V. Navrotskiy
 */
public class UrlStreamProvider implements StreamProvider {
  @Nonnull
  private final URL url;

  public UrlStreamProvider(@Nonnull URL url) {
    this.url = url;
  }

  @Nonnull
  @Override
  public InputStream getStream() throws IOException {
    return url.openStream();
  }
}
