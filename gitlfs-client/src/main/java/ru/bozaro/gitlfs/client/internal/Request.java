package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Single HTTP request.
 *
 * @author Artem V. Navrotskiy
 */
public interface Request<R> {
  @NotNull
  HttpMethod createRequest(@NotNull ObjectMapper mapper, @NotNull String url) throws IOException;

  R processResponse(@NotNull ObjectMapper mapper, @NotNull HttpMethod request) throws IOException;

  /**
   * Success status codes.
   *
   * @return Success status codes.
   */
  @Nullable
  int[] statusCodes();
}
