package ru.bozaro.gitlfs.client.exceptions;

import org.apache.commons.httpclient.HttpMethod;
import org.jetbrains.annotations.NotNull;

/**
 * Unauthorized HTTP exception.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class UnauthorizedException extends RequestException {
  public UnauthorizedException(@NotNull HttpMethod request) {
    super(request);
  }
}
