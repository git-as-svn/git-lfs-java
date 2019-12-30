package ru.bozaro.gitlfs.client.auth;

import ru.bozaro.gitlfs.common.data.Link;
import ru.bozaro.gitlfs.common.data.Operation;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.EnumMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Get authentication data from external application.
 * This AuthProvider is EXPERIMENTAL and it can only be used at your own risk.
 *
 * @author Artem V. Navrotskiy
 */
public abstract class CachedAuthProvider implements AuthProvider {
  @Nonnull
  private final ConcurrentMap<Operation, Link> authCache;
  @Nonnull
  private final EnumMap<Operation, Object> locks;

  public CachedAuthProvider() {
    this.locks = createLocks();
    this.authCache = new ConcurrentHashMap<>(Operation.values().length);
  }

  @Nonnull
  private static EnumMap<Operation, Object> createLocks() {
    final EnumMap<Operation, Object> result = new EnumMap<>(Operation.class);
    for (Operation value : Operation.values()) {
      result.put(value, new Object());
    }
    return result;
  }

  @Nonnull
  @Override
  public final Link getAuth(@Nonnull Operation operation) throws IOException {
    Link auth = authCache.get(operation);
    if (auth == null) {
      synchronized (locks.get(operation)) {
        auth = authCache.get(operation);
        if (auth == null) {
          try {
            auth = getAuthUncached(operation);
            authCache.put(operation, auth);
          } catch (InterruptedException e) {
            throw new IOException(e);
          }
        }
      }
    }
    return auth;
  }

  @Nonnull
  protected abstract Link getAuthUncached(@Nonnull Operation operation) throws IOException, InterruptedException;

  @Override
  public final void invalidateAuth(@Nonnull Operation operation, @Nonnull Link auth) {
    authCache.remove(operation, auth);
  }
}
