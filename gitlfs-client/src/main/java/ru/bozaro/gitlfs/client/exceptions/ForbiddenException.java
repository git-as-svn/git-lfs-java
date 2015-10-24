package ru.bozaro.gitlfs.client.exceptions;

import org.apache.commons.httpclient.HttpMethod;
import org.jetbrains.annotations.NotNull;

/**
 * Forbidden HTTP exception.
 *
 * @author Artem V. Navrotskiy
 */
public class ForbiddenException extends RequestException {
  public ForbiddenException(@NotNull HttpMethod request) {
    super(request);
  }
}
