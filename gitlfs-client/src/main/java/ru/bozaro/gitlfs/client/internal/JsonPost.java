package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static ru.bozaro.gitlfs.common.Constants.HEADER_ACCEPT;
import static ru.bozaro.gitlfs.common.Constants.MIME_LFS_JSON;

/**
 * POST simple JSON request.
 *
 * @author Artem V. Navrotskiy
 */
public class JsonPost<Req, Res> implements Request<Res> {
  @NotNull
  private final Class<Res> type;
  @NotNull
  private final Req req;

  public JsonPost(@NotNull Req req, @NotNull Class<Res> type) {
    this.req = req;
    this.type = type;
  }

  @NotNull
  @Override
  public HttpUriRequest createRequest(@NotNull ObjectMapper mapper, @NotNull String url) throws JsonProcessingException {
    final HttpPost method = new HttpPost(url);
    method.addHeader(HEADER_ACCEPT, MIME_LFS_JSON);
    final ByteArrayEntity entity = new ByteArrayEntity(mapper.writeValueAsBytes(req));
    entity.setContentType(MIME_LFS_JSON);
    method.setEntity(entity);
    return method;
  }

  @Nullable
  @Override
  public int[] statusCodes() {
    return null;
  }

  @Override
  public Res processResponse(@NotNull ObjectMapper mapper, @NotNull HttpResponse response) throws IOException {
    return mapper.readValue(response.getEntity().getContent(), type);
  }
}
