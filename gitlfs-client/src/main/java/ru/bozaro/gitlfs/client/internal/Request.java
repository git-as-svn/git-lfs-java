package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Single HTTP request.
 *
 * @author Artem V. Navrotskiy
 */
public interface Request<R> {
  @Nonnull
  HttpUriRequest createRequest(@Nonnull ObjectMapper mapper, @Nonnull String url) throws IOException;

  R processResponse(@Nonnull ObjectMapper mapper, @Nonnull HttpResponse response) throws IOException;

  /**
   * Success status codes.
   *
   * @return Success status codes.
   */
  @Nonnull
  default int[] statusCodes() {
    return new int[]{HttpStatus.SC_OK};
  }
}
