package ru.bozaro.gitlfs.server;

import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;

/**
 * Server side error exception.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
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

  public void updateHeaders(@NotNull HttpServletResponse response) {
  }
}
