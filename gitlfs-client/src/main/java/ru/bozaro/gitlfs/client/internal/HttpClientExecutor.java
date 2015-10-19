package ru.bozaro.gitlfs.client.internal;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
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

  @Override
  public void executeMethod(@NotNull HttpMethod request) throws IOException {
    http.executeMethod(request);
  }
}
