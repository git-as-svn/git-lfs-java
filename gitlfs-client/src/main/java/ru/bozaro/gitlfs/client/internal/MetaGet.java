package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.client.exceptions.RequestException;
import ru.bozaro.gitlfs.common.data.ObjectRes;

import java.io.IOException;

import static ru.bozaro.gitlfs.client.Constants.HEADER_ACCEPT;
import static ru.bozaro.gitlfs.client.Constants.MIME_LFS_JSON;

/**
 * GET object metadata request.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class MetaGet implements Request<ObjectRes> {
  @NotNull
  @Override
  public HttpMethod createRequest(@NotNull ObjectMapper mapper, @NotNull String url) {
    final GetMethod req = new GetMethod(url);
    req.addRequestHeader(HEADER_ACCEPT, MIME_LFS_JSON);
    return req;
  }

  @Override
  public ObjectRes processResponse(@NotNull ObjectMapper mapper, @NotNull HttpMethod request) throws IOException {
    switch (request.getStatusCode()) {
      case HttpStatus.SC_OK:
        return mapper.readValue(request.getResponseBodyAsStream(), ObjectRes.class);
      case HttpStatus.SC_NOT_FOUND:
        return null;
      default:
        throw new RequestException(request);
    }
  }
}
