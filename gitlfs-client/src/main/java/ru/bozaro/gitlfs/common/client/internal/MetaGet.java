package ru.bozaro.gitlfs.common.client.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.common.client.exceptions.RequestException;
import ru.bozaro.gitlfs.common.data.Meta;

import java.io.IOException;

import static ru.bozaro.gitlfs.common.client.Constants.HEADER_ACCEPT;
import static ru.bozaro.gitlfs.common.client.Constants.MIME_LFS_JSON;

/**
 * GET object metadata request.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class MetaGet implements Request<GetMethod, Meta> {
  @NotNull
  @Override
  public GetMethod createRequest(@NotNull ObjectMapper mapper, @NotNull String url) {
    final GetMethod req = new GetMethod(url);
    req.addRequestHeader(HEADER_ACCEPT, MIME_LFS_JSON);
    return req;
  }

  @Override
  public Meta processResponse(@NotNull ObjectMapper mapper, @NotNull GetMethod request) throws IOException {
    switch (request.getStatusCode()) {
      case HttpStatus.SC_OK:
        return mapper.readValue(request.getResponseBodyAsStream(), Meta.class);
      case HttpStatus.SC_NOT_FOUND:
        return null;
      default:
        throw new RequestException(request);
    }
  }
}
