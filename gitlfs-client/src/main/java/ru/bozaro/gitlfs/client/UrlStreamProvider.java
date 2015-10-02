package ru.bozaro.gitlfs.client;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Create stream by URL.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class UrlStreamProvider implements StreamProvider {
  @NotNull
  private final URL url;

  public UrlStreamProvider(@NotNull URL url) {
    this.url = url;
  }

  @NotNull
  @Override
  public InputStream getStream() throws IOException {
    return url.openStream();
  }
}
