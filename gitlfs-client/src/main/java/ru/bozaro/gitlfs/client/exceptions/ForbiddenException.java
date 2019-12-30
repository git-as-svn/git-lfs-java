package ru.bozaro.gitlfs.client.exceptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import javax.annotation.Nonnull;

/**
 * Forbidden HTTP exception.
 *
 * @author Artem V. Navrotskiy
 */
public class ForbiddenException extends RequestException {
  public ForbiddenException(@Nonnull HttpUriRequest request, @Nonnull HttpResponse response) {
    super(request, response);
  }
}
