package ru.bozaro.gitlfs.common.client;

import org.jetbrains.annotations.NotNull;

/**
 * Requested access.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public enum AuthAccess {
  Upload("upload"),
  Download("download");

  @NotNull
  private final String token;

  /**
   * The name is given by a string constant to avoid problems with obfuscation.
   */
  AuthAccess(@NotNull String token) {
    this.token = token;
  }

  @NotNull
  public String getToken() {
    return token;
  }
}
