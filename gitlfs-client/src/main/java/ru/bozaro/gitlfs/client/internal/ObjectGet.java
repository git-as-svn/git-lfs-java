package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
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
  public HttpMethod createRequest(@NotNull ObjectMapper mapper, @NotNull String url) {
    return new GetMethod(url);
  }

  @Nullable
  @Override
  public int[] statusCodes() {
    return null;
  }

  @Override
  public T processResponse(@NotNull ObjectMapper mapper, @NotNull HttpMethod request) throws IOException {
    return handler.accept(request.getResponseBodyAsStream());
  }
}
