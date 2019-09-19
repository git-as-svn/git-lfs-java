package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.common.data.Meta;

import java.io.IOException;

import static ru.bozaro.gitlfs.common.Constants.HEADER_ACCEPT;
import static ru.bozaro.gitlfs.common.Constants.MIME_LFS_JSON;

/**
 * Object verification after upload request.
 *
 * @author Artem V. Navrotskiy
 */
public class ObjectVerify implements Request<Void> {
  @NotNull
  private final Meta meta;

  public ObjectVerify(@NotNull Meta meta) {
    this.meta = meta;
  }

  @NotNull
  @Override
  public HttpUriRequest createRequest(@NotNull ObjectMapper mapper, @NotNull String url) throws IOException {
    final HttpPost req = new HttpPost(url);
    req.addHeader(HEADER_ACCEPT, MIME_LFS_JSON);
    final byte[] content = mapper.writeValueAsBytes(meta);
    final AbstractHttpEntity entity = new ByteArrayEntity(content);
    entity.setContentType(MIME_LFS_JSON);
    req.setEntity(entity);
    return req;
  }

  @Override
  public Void processResponse(@NotNull ObjectMapper mapper, @NotNull HttpResponse response) {
    return null;
  }
}
