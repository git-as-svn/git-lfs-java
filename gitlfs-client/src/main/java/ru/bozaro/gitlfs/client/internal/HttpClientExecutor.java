package ru.bozaro.gitlfs.client.internal;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import ru.bozaro.gitlfs.client.HttpExecutor;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Simple HttpClient wrapper.
 *
 * @author Artem V. Navrotskiy
 */
public final class HttpClientExecutor implements HttpExecutor {
  @Nonnull
  private final CloseableHttpClient http;

  public HttpClientExecutor(@Nonnull CloseableHttpClient http) {
    this.http = http;
  }

  @Nonnull
  @Override
  public CloseableHttpResponse executeMethod(@Nonnull HttpUriRequest request) throws IOException {
    return http.execute(request);
  }

  @Override
  public void close() throws IOException {
    http.close();
  }
}
