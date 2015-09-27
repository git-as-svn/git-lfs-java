package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.client.Constants;
import ru.bozaro.gitlfs.client.exceptions.RequestException;

import java.io.IOException;

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
    method.addRequestHeader(Constants.HEADER_ACCEPT, Constants.MIME_LFS_JSON);
    final byte[] content = mapper.writeValueAsBytes(req);
    method.setRequestEntity(new ByteArrayRequestEntity(content, Constants.MIME_LFS_JSON));
    return method;
  }

  @NotNull
  @Override
  public Res processResponse(@NotNull ObjectMapper mapper, @NotNull HttpMethod request) throws IOException {
    switch (request.getStatusCode()) {
      case HttpStatus.SC_OK:
        return mapper.readValue(request.getResponseBodyAsStream(), type);
      default:
        throw new RequestException(request);
    }
  }
}
