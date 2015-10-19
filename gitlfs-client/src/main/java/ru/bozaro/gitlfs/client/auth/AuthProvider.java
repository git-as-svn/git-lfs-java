package ru.bozaro.gitlfs.client.auth;

import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.common.data.Link;
import ru.bozaro.gitlfs.common.data.Operation;

import java.io.IOException;

/**
 * Authentication provider.
 *
 * @author Artem V. Navrotskiy
 */
public interface AuthProvider {

  /**
   * Get auth data.
   * Auth data can be cached in this method.
   *
   * @param operation Operation type.
   * @return Auth data.
   * @throws IOException
   */
  @NotNull
  Link getAuth(@NotNull Operation operation) throws IOException;

  /**
   * Set auth as expired.
   *
   * @param operation Operation type.
   * @param auth Expired auth data.
   */
  void invalidateAuth(@NotNull Operation operation, @NotNull Link auth);
}
