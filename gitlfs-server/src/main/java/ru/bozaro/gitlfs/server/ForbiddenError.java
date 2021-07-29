package ru.bozaro.gitlfs.server;

import jakarta.servlet.http.HttpServletResponse;

import javax.annotation.Nonnull;

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
