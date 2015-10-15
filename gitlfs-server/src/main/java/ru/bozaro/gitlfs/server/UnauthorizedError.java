package ru.bozaro.gitlfs.server;

import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;

/**
 * Unauthorized error.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class UnauthorizedError extends ServerError {
  @NotNull
  private String authenticate;

  public UnauthorizedError(@NotNull String authenticate) {
    super(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    this.authenticate = authenticate;
  }

  @NotNull
  public String getAuthenticate() {
    return authenticate;
  }

  @Override
  public void updateHeaders(@NotNull HttpServletResponse response) {
    super.updateHeaders(response);
    response.addHeader("WWW-Authenticate", authenticate);
  }
}
