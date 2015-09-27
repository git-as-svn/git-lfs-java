package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpMethod;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Single HTTP request.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public interface Request<R> {
  @NotNull
  HttpMethod createRequest(@NotNull ObjectMapper mapper, @NotNull String url) throws IOException;

  R processResponse(@NotNull ObjectMapper mapper, @NotNull HttpMethod request) throws IOException;
}
