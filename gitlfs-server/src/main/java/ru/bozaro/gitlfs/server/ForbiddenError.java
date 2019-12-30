package ru.bozaro.gitlfs.server;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;

/**
 * Forbidden error.
 *
 * @author Artem V. Navrotskiy
 */
public class ForbiddenError extends ServerError {
  public ForbiddenError() {
    this("Access forbidden");
  }

  public ForbiddenError(@Nonnull String message) {
    super(HttpServletResponse.SC_FORBIDDEN, message);
  }
}
