package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Single HTTP request.
 *
 * @author Artem V. Navrotskiy
 */
public interface Request<R> {
  @NotNull
  HttpUriRequest createRequest(@NotNull ObjectMapper mapper, @NotNull String url) throws IOException;

  R processResponse(@NotNull ObjectMapper mapper, @NotNull HttpResponse response) throws IOException;

  /**
   * Success status codes.
   *
   * @return Success status codes.
   */
  @NotNull
  default int[] statusCodes() {
    return new int[]{HttpStatus.SC_OK};
  }
}
