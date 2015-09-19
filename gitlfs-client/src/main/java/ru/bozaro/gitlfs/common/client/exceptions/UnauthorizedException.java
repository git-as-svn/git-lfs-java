package ru.bozaro.gitlfs.common.client.exceptions;

import org.apache.commons.httpclient.HttpMethod;
import org.jetbrains.annotations.NotNull;

/**
 * Unauthorized HTTP exception.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class UnauthorizedException extends HttpException {
  public UnauthorizedException(@NotNull HttpMethod request) {
    super(request, false);
  }
}
