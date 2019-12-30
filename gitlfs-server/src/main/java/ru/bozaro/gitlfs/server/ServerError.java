package ru.bozaro.gitlfs.server;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;

/**
 * Server side error exception.
 *
 * @author Artem V. Navrotskiy
 */
public class ServerError extends Exception {
  private final int statusCode;

  public ServerError(int statusCode, String message) {
    super(message);
    this.statusCode = statusCode;
  }

  public ServerError(int statusCode, String message, Throwable cause) {
    super(message, cause);
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void updateHeaders(@Nonnull HttpServletResponse response) {
  }
}
