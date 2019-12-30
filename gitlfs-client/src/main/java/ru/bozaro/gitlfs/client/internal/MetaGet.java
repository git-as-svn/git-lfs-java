package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import ru.bozaro.gitlfs.common.data.ObjectRes;

import javax.annotation.Nonnull;
import java.io.IOException;

import static ru.bozaro.gitlfs.common.Constants.HEADER_ACCEPT;
import static ru.bozaro.gitlfs.common.Constants.MIME_LFS_JSON;

/**
 * GET object metadata request.
 *
 * @author Artem V. Navrotskiy
 */
public class MetaGet implements Request<ObjectRes> {
  @Nonnull
  @Override
  public HttpUriRequest createRequest(@Nonnull ObjectMapper mapper, @Nonnull String url) {
    final HttpGet req = new HttpGet(url);
    req.addHeader(HEADER_ACCEPT, MIME_LFS_JSON);
    return req;
  }

  @Override
  public ObjectRes processResponse(@Nonnull ObjectMapper mapper, @Nonnull HttpResponse response) throws IOException {
    switch (response.getStatusLine().getStatusCode()) {
      case HttpStatus.SC_OK:
        return mapper.readValue(response.getEntity().getContent(), ObjectRes.class);
      case HttpStatus.SC_NOT_FOUND:
        return null;
      default:
        throw new IllegalStateException();
    }
  }

  @Nonnull
  @Override
  public int[] statusCodes() {
    return new int[]{
        HttpStatus.SC_OK,
        HttpStatus.SC_NOT_FOUND,
    };
  }
}
