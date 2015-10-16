package ru.bozaro.gitlfs.server;

import javax.servlet.http.HttpServletResponse;

/**
 * Forbidden error.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class ForbiddenError extends ServerError {
  public ForbiddenError() {
    super(HttpServletResponse.SC_FORBIDDEN, "Access forbidden");
  }
}
