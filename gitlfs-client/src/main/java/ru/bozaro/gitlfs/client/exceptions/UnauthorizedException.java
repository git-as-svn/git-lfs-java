package ru.bozaro.gitlfs.client.exceptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import javax.annotation.Nonnull;

/**
 * Unauthorized HTTP exception.
 *
 * @author Artem V. Navrotskiy
 */
public class UnauthorizedException extends RequestException {
  public UnauthorizedException(@Nonnull HttpUriRequest request, @Nonnull HttpResponse response) {
    super(request, response);
  }
}
