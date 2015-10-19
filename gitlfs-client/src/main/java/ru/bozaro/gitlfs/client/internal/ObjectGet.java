package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

/**
 * GET object request.
 *
 * @author Artem V. Navrotskiy
 */
public class ObjectGet implements Request<InputStream> {
  @NotNull
  @Override
  public HttpMethod createRequest(@NotNull ObjectMapper mapper, @NotNull String url) {
    return new GetMethod(url);
  }

  @Nullable
  @Override
  public int[] statusCodes() {
    return null;
  }

  @Override
  public InputStream processResponse(@NotNull ObjectMapper mapper, @NotNull HttpMethod request) throws IOException {
    return request.getResponseBodyAsStream();
  }
}
