package ru.bozaro.gitlfs.server;

import javax.servlet.http.HttpServletResponse;

/**
 * Forbidden error.
 *
 * @author Artem V. Navrotskiy
 */
public class ForbiddenError extends ServerError {
  public ForbiddenError() {
    super(HttpServletResponse.SC_FORBIDDEN, "Access forbidden");
  }
}
