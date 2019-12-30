package ru.bozaro.gitlfs.client.auth;

import ru.bozaro.gitlfs.common.data.Link;
import ru.bozaro.gitlfs.common.data.Operation;

import javax.annotation.Nonnull;
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
   */
  @Nonnull
  Link getAuth(@Nonnull Operation operation) throws IOException;

  /**
   * Set auth as expired.
   *
   * @param operation Operation type.
   * @param auth      Expired auth data.
   */
  void invalidateAuth(@Nonnull Operation operation, @Nonnull Link auth);
}
