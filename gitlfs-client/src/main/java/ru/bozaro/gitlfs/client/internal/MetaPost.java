package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.common.data.Meta;
import ru.bozaro.gitlfs.common.data.ObjectRes;

import java.io.IOException;

import static ru.bozaro.gitlfs.client.Constants.HEADER_ACCEPT;
import static ru.bozaro.gitlfs.client.Constants.MIME_LFS_JSON;

/**
 * POST object metadata request.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class MetaPost implements Request<ObjectRes> {
  @NotNull
  private final Meta meta;

  public MetaPost(@NotNull Meta meta) {
    this.meta = meta;
  }

  @NotNull
  @Override
  public HttpMethod createRequest(@NotNull ObjectMapper mapper, @NotNull String url) throws JsonProcessingException {
    final PostMethod req = new PostMethod(url);
    req.addRequestHeader(HEADER_ACCEPT, MIME_LFS_JSON);
    final byte[] content = mapper.writeValueAsBytes(meta);
    req.setRequestEntity(new ByteArrayRequestEntity(content, MIME_LFS_JSON));
    return req;
  }

  @Nullable
  @Override
  public int[] statusCodes() {
    return new int[]{
        HttpStatus.SC_OK,
        HttpStatus.SC_ACCEPTED,
    };
  }

  @Override
  public ObjectRes processResponse(@NotNull ObjectMapper mapper, @NotNull HttpMethod request) throws IOException {
    switch (request.getStatusCode()) {
      case HttpStatus.SC_OK:
        return null;
      case HttpStatus.SC_ACCEPTED:
        return mapper.readValue(request.getResponseBodyAsStream(), ObjectRes.class);
      default:
        throw new IllegalStateException();
    }
  }
}
