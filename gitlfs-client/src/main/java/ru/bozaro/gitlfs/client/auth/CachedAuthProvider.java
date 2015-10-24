package ru.bozaro.gitlfs.client.auth;

import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.common.data.Link;
import ru.bozaro.gitlfs.common.data.Operation;

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
  @NotNull
  private final ConcurrentMap<Operation, Link> authCache;
  @NotNull
  private final EnumMap<Operation, Object> locks;

  public CachedAuthProvider() {
    this.locks = createLocks();
    this.authCache = new ConcurrentHashMap<>(Operation.values().length);
  }

  @NotNull
  @Override
  public final Link getAuth(@NotNull Operation operation) throws IOException {
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

  @NotNull
  private static EnumMap<Operation, Object> createLocks() {
    final EnumMap<Operation, Object> result = new EnumMap<>(Operation.class);
    for (Operation value : Operation.values()) {
      result.put(value, new Object());
    }
    return result;
  }

  @NotNull
  protected abstract Link getAuthUncached(@NotNull Operation operation) throws IOException, InterruptedException;

  @Override
  public final void invalidateAuth(@NotNull Operation operation, @NotNull Link auth) {
    authCache.remove(operation, auth);
  }
}
