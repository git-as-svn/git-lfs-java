package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;

import javax.annotation.Nonnull;
import java.io.IOException;

import static ru.bozaro.gitlfs.common.Constants.HEADER_ACCEPT;
import static ru.bozaro.gitlfs.common.Constants.MIME_LFS_JSON;

/**
 * POST simple JSON request.
 *
 * @author Artem V. Navrotskiy
 */
public class JsonPost<Req, Res> implements Request<Res> {
  @Nonnull
  private final Class<Res> type;
  @Nonnull
  private final Req req;

  public JsonPost(@Nonnull Req req, @Nonnull Class<Res> type) {
    this.req = req;
    this.type = type;
  }

  @Nonnull
  @Override
  public LfsRequest createRequest(@Nonnull ObjectMapper mapper, @Nonnull String url) throws JsonProcessingException {
    final HttpPost method = new HttpPost(url);
    method.addHeader(HEADER_ACCEPT, MIME_LFS_JSON);
    final ByteArrayEntity entity = new ByteArrayEntity(mapper.writeValueAsBytes(req));
    entity.setContentType(MIME_LFS_JSON);
    method.setEntity(entity);
    return new LfsRequest(method, entity);
  }

  @Override
  public Res processResponse(@Nonnull ObjectMapper mapper, @Nonnull HttpResponse response) throws IOException {
    return mapper.readValue(response.getEntity().getContent(), type);
  }
}
