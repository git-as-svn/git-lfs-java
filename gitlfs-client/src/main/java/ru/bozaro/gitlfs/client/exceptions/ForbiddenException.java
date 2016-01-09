package ru.bozaro.gitlfs.client.exceptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.jetbrains.annotations.NotNull;

/**
 * Forbidden HTTP exception.
 *
 * @author Artem V. Navrotskiy
 */
public class ForbiddenException extends RequestException {
  public ForbiddenException(@NotNull HttpUriRequest request, @NotNull HttpResponse response) {
    super(request, response);
  }
}
