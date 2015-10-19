package ru.bozaro.gitlfs.client.exceptions;

import org.apache.commons.httpclient.HttpMethod;
import org.jetbrains.annotations.NotNull;

/**
 * Unauthorized HTTP exception.
 *
 * @author Artem V. Navrotskiy
 */
public class UnauthorizedException extends RequestException {
  public UnauthorizedException(@NotNull HttpMethod request) {
    super(request);
  }
}
