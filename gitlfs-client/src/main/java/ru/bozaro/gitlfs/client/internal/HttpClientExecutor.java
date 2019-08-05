package ru.bozaro.gitlfs.client.internal;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.client.HttpExecutor;

import java.io.IOException;

/**
 * Simple HttpClient wrapper.
 *
 * @author Artem V. Navrotskiy
 */
public final class HttpClientExecutor implements HttpExecutor {
  @NotNull
  private final CloseableHttpClient http;

  public HttpClientExecutor(@NotNull CloseableHttpClient http) {
    this.http = http;
  }

  @NotNull
  @Override
  public CloseableHttpResponse executeMethod(@NotNull HttpUriRequest request) throws IOException {
    return http.execute(request);
  }

  @Override
  public void close() throws IOException {
    http.close();
  }
}
