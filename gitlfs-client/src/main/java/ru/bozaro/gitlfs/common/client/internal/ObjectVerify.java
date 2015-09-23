package ru.bozaro.gitlfs.common.client.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Object verification after upload request.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class ObjectVerify implements Request<Void> {
  @NotNull
  @Override
  public HttpMethod createRequest(@NotNull ObjectMapper mapper, @NotNull String url) throws IOException {
    return new GetMethod(url);
  }

  @Override
  public Void processResponse(@NotNull ObjectMapper mapper, @NotNull HttpMethod request) throws IOException {
    return null;
  }
}
