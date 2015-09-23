package ru.bozaro.gitlfs.common.client.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.common.client.exceptions.RequestException;
import ru.bozaro.gitlfs.common.data.Meta;
import ru.bozaro.gitlfs.common.data.ObjectRes;

import java.io.IOException;

import static ru.bozaro.gitlfs.common.client.Constants.HEADER_ACCEPT;
import static ru.bozaro.gitlfs.common.client.Constants.MIME_LFS_JSON;

/**
 * POST object metadata request.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class MetaPost implements Request<ObjectRes> {
  @NotNull
  private final String hash;
  private final long size;

  public MetaPost(@NotNull String hash, long size) {
    this.hash = hash;
    this.size = size;
  }

  @NotNull
  @Override
  public HttpMethod createRequest(@NotNull ObjectMapper mapper, @NotNull String url) throws JsonProcessingException {
    final PostMethod req = new PostMethod(url);
    req.addRequestHeader(HEADER_ACCEPT, MIME_LFS_JSON);
    final byte[] content = mapper.writeValueAsBytes(new Meta(hash, size));
    req.setRequestEntity(new ByteArrayRequestEntity(content, MIME_LFS_JSON));
    return req;
  }

  @Override
  public ObjectRes processResponse(@NotNull ObjectMapper mapper, @NotNull HttpMethod request) throws IOException {
    switch (request.getStatusCode()) {
      case HttpStatus.SC_OK:
      case HttpStatus.SC_ACCEPTED:
        return mapper.readValue(request.getResponseBodyAsStream(), ObjectRes.class);
      default:
        throw new RequestException(request);
    }
  }
}
