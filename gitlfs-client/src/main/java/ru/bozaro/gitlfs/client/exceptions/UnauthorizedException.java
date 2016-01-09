package ru.bozaro.gitlfs.client.exceptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.jetbrains.annotations.NotNull;

/**
 * Unauthorized HTTP exception.
 *
 * @author Artem V. Navrotskiy
 */
public class UnauthorizedException extends RequestException {
  public UnauthorizedException(@NotNull HttpUriRequest request, @NotNull HttpResponse response) {
    super(request, response);
  }
}
