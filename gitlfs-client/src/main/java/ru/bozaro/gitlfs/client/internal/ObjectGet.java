package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.client.io.StreamHandler;

import java.io.IOException;

/**
 * GET object request.
 *
 * @author Artem V. Navrotskiy
 */
public class ObjectGet<T> implements Request<T> {
  @NotNull
  final StreamHandler<T> handler;

  public ObjectGet(@NotNull StreamHandler<T> handler) {
    this.handler = handler;
  }

  @NotNull
  @Override
  public HttpUriRequest createRequest(@NotNull ObjectMapper mapper, @NotNull String url) {
    return new HttpGet(url);
  }

  @Nullable
  @Override
  public int[] statusCodes() {
    return null;
  }

  @Override
  public T processResponse(@NotNull ObjectMapper mapper, @NotNull HttpResponse response) throws IOException {
    return handler.accept(response.getEntity().getContent());
  }
}
