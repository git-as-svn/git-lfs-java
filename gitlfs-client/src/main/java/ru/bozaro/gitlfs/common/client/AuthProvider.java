package ru.bozaro.gitlfs.common.client;

import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.common.data.Auth;

import java.io.IOException;

/**
 * Authentication provider.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public interface AuthProvider {
  enum Mode {
    Upload("upload"),
    Download("download");

    @NotNull
    private final String token;

    /**
     * The name is given by a string constant to avoid problems with obfuscation.
     */
    Mode(@NotNull String token) {
      this.token = token;
    }

    @NotNull
    public String getToken() {
      return token;
    }
  }

  @NotNull
  Auth getAuth(@NotNull Mode mode) throws IOException;
}
