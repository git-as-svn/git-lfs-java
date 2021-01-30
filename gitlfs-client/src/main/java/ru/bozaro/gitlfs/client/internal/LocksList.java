package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import ru.bozaro.gitlfs.common.data.LocksRes;

import javax.annotation.Nonnull;
import java.io.IOException;

import static ru.bozaro.gitlfs.common.Constants.HEADER_ACCEPT;
import static ru.bozaro.gitlfs.common.Constants.MIME_LFS_JSON;

public final class LocksList implements Request<LocksRes> {
  @Nonnull
  @Override
  public LfsRequest createRequest(@Nonnull ObjectMapper mapper, @Nonnull String url) {
    final HttpGet req = new HttpGet(url);
    req.addHeader(HEADER_ACCEPT, MIME_LFS_JSON);
    return new LfsRequest(req, null);
  }

  @Override
  public LocksRes processResponse(@Nonnull ObjectMapper mapper, @Nonnull HttpResponse response) throws IOException {
    return mapper.readValue(response.getEntity().getContent(), LocksRes.class);
  }
}
