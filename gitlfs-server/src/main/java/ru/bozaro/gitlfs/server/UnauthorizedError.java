package ru.bozaro.gitlfs.server;

import jakarta.servlet.http.HttpServletResponse;

import javax.annotation.Nonnull;

/**
 * Unauthorized error.
 *
 * @author Artem V. Navrotskiy
 */
public class UnauthorizedError extends ServerError {
  @Nonnull
  private String authenticate;

  public UnauthorizedError(@Nonnull String authenticate) {
    super(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    this.authenticate = authenticate;
  }

  @Nonnull
  public String getAuthenticate() {
    return authenticate;
  }

  @Override
  public void updateHeaders(@Nonnull HttpServletResponse response) {
    super.updateHeaders(response);
    response.addHeader("WWW-Authenticate", authenticate);
  }
}
