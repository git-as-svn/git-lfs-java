package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.client.io.StreamProvider;

import java.io.IOException;

import static ru.bozaro.gitlfs.common.Constants.MIME_BINARY;

/**
 * PUT object request.
 *
 * @author Artem V. Navrotskiy
 */
public class ObjectPut implements Request<Void> {
  private final StreamProvider streamProvider;
  private final long size;

  public ObjectPut(StreamProvider streamProvider, long size) {
    this.streamProvider = streamProvider;
    this.size = size;
  }

  @NotNull
  @Override
  public HttpUriRequest createRequest(@NotNull ObjectMapper mapper, @NotNull String url) throws IOException {
    final HttpPut req = new HttpPut(url);
    final AbstractHttpEntity entity = new InputStreamEntity(streamProvider.getStream(), size);
    entity.setContentType(MIME_BINARY);
    req.setEntity(entity);
    return req;
  }

  @Override
  public Void processResponse(@NotNull ObjectMapper mapper, @NotNull HttpResponse response) throws IOException {
    return null;
  }

  @NotNull
  @Override
  public int[] statusCodes() {
    return new int[]{
        HttpStatus.SC_OK,
        HttpStatus.SC_CREATED,
    };
  }
}
