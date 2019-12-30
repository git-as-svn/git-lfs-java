package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import ru.bozaro.gitlfs.client.io.StreamHandler;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * GET object request.
 *
 * @author Artem V. Navrotskiy
 */
public class ObjectGet<T> implements Request<T> {
  @Nonnull
  private final StreamHandler<T> handler;

  public ObjectGet(@Nonnull StreamHandler<T> handler) {
    this.handler = handler;
  }

  @Nonnull
  @Override
  public HttpUriRequest createRequest(@Nonnull ObjectMapper mapper, @Nonnull String url) {
    return new HttpGet(url);
  }

  @Override
  public T processResponse(@Nonnull ObjectMapper mapper, @Nonnull HttpResponse response) throws IOException {
    return handler.accept(response.getEntity().getContent());
  }
}
