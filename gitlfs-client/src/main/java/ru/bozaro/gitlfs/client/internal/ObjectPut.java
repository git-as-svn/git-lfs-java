package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.client.Constants;
import ru.bozaro.gitlfs.client.StreamProvider;

import java.io.IOException;

/**
 * PUT object request.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class ObjectPut implements Request<Void> {
  private final StreamProvider streamProvider;

  public ObjectPut(StreamProvider streamProvider) {
    this.streamProvider = streamProvider;
  }

  @NotNull
  @Override
  public HttpMethod createRequest(@NotNull ObjectMapper mapper, @NotNull String url) throws IOException {
    final PutMethod req = new PutMethod(url);
    req.setRequestEntity(new InputStreamRequestEntity(streamProvider.getStream(), Constants.MIME_BINARY));
    return req;
  }

  @Override
  public Void processResponse(@NotNull ObjectMapper mapper, @NotNull HttpMethod request) throws IOException {
    return null;
  }
}
