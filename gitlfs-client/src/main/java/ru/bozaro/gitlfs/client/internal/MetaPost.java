package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import ru.bozaro.gitlfs.common.data.Meta;
import ru.bozaro.gitlfs.common.data.ObjectRes;

import javax.annotation.Nonnull;
import java.io.IOException;

import static ru.bozaro.gitlfs.common.Constants.HEADER_ACCEPT;
import static ru.bozaro.gitlfs.common.Constants.MIME_LFS_JSON;

/**
 * POST object metadata request.
 *
 * @author Artem V. Navrotskiy
 */
public class MetaPost implements Request<ObjectRes> {
  @Nonnull
  private final Meta meta;

  public MetaPost(@Nonnull Meta meta) {
    this.meta = meta;
  }

  @Nonnull
  @Override
  public LfsRequest createRequest(@Nonnull ObjectMapper mapper, @Nonnull String url) throws JsonProcessingException {
    final HttpPost req = new HttpPost(url);
    req.addHeader(HEADER_ACCEPT, MIME_LFS_JSON);
    final AbstractHttpEntity entity = new ByteArrayEntity(mapper.writeValueAsBytes(meta));
    entity.setContentType(MIME_LFS_JSON);
    req.setEntity(entity);
    return new LfsRequest(req, entity);
  }

  @Override
  public ObjectRes processResponse(@Nonnull ObjectMapper mapper, @Nonnull HttpResponse response) throws IOException {
    switch (response.getStatusLine().getStatusCode()) {
      case HttpStatus.SC_OK:
        return null;
      case HttpStatus.SC_ACCEPTED:
        return mapper.readValue(response.getEntity().getContent(), ObjectRes.class);
      default:
        throw new IllegalStateException();
    }
  }

  @Nonnull
  @Override
  public int[] statusCodes() {
    return new int[]{
        HttpStatus.SC_OK,
        HttpStatus.SC_ACCEPTED,
    };
  }
}
