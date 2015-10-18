package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static ru.bozaro.gitlfs.common.Constants.HEADER_ACCEPT;
import static ru.bozaro.gitlfs.common.Constants.MIME_LFS_JSON;

/**
 * POST simple JSON request.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
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
  public HttpMethod createRequest(@NotNull ObjectMapper mapper, @NotNull String url) throws JsonProcessingException {
    final PostMethod method = new PostMethod(url);
    method.addRequestHeader(HEADER_ACCEPT, MIME_LFS_JSON);
    final byte[] content = mapper.writeValueAsBytes(req);
    method.setRequestEntity(new ByteArrayRequestEntity(content, MIME_LFS_JSON));
    return method;
  }

  @Nullable
  @Override
  public int[] statusCodes() {
    return null;
  }

  @NotNull
  @Override
  public Res processResponse(@NotNull ObjectMapper mapper, @NotNull HttpMethod request) throws IOException {
    return mapper.readValue(request.getResponseBodyAsStream(), type);
  }
}
