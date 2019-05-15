package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.common.data.LocksRes;

import java.io.IOException;

import static ru.bozaro.gitlfs.common.Constants.HEADER_ACCEPT;
import static ru.bozaro.gitlfs.common.Constants.MIME_LFS_JSON;

public final class LocksList implements Request<LocksRes> {
  @NotNull
  @Override
  public HttpUriRequest createRequest(@NotNull ObjectMapper mapper, @NotNull String url) {
    final HttpGet req = new HttpGet(url);
    req.addHeader(HEADER_ACCEPT, MIME_LFS_JSON);
    return req;
  }

  @Override
  public LocksRes processResponse(@NotNull ObjectMapper mapper, @NotNull HttpResponse response) throws IOException {
    return mapper.readValue(response.getEntity().getContent(), LocksRes.class);
  }

  @Nullable
  @Override
  public int[] statusCodes() {
    return null;
  }
}
