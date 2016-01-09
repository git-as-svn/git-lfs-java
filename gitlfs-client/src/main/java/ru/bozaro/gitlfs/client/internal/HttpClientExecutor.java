package ru.bozaro.gitlfs.client.internal;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.client.HttpExecutor;

import java.io.IOException;

/**
 * Simple HttpClient wrapper.
 *
 * @author Artem V. Navrotskiy
 */
public class HttpClientExecutor implements HttpExecutor {
  private final HttpClient http;

  public HttpClientExecutor(HttpClient http) {
    this.http = http;
  }

  @NotNull
  @Override
  public HttpResponse executeMethod(@NotNull HttpUriRequest request) throws IOException {
    return http.execute(request);
  }
}
