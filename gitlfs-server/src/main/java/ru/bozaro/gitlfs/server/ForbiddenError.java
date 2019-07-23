package ru.bozaro.gitlfs.server;

import org.jetbrains.annotations.NotNull;

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

  public ForbiddenError(@NotNull String message) {
    super(HttpServletResponse.SC_FORBIDDEN, message);
  }
}
