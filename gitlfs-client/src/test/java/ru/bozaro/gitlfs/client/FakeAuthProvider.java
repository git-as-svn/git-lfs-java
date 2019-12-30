package ru.bozaro.gitlfs.client;

import com.google.common.collect.ImmutableMap;
import ru.bozaro.gitlfs.client.auth.AuthProvider;
import ru.bozaro.gitlfs.common.data.Link;
import ru.bozaro.gitlfs.common.data.Operation;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fake authenticator.
 *
 * @author Artem V. Navrotskiy
 */
public class FakeAuthProvider implements AuthProvider {
  @Nonnull
  private final Object lock = new Object();
  @Nonnull
  private final AtomicInteger id = new AtomicInteger(0);
  @CheckForNull
  private Link auth = null;

  @Nonnull
  @Override
  public Link getAuth(@Nonnull Operation operation) {
    synchronized (lock) {
      if (auth == null) {
        auth = createAuth();
      }
      return auth;
    }
  }

  @Override
  public void invalidateAuth(@Nonnull Operation operation, @Nonnull Link auth) {
    synchronized (lock) {
      if (this.auth == auth) {
        this.auth = null;
      }
    }
  }

  @Nonnull
  private Link createAuth() {
    return new Link(URI.create("http://gitlfs.local/test.git/info/lfs"), ImmutableMap.<String, String>builder()
        .put("Authorization", "RemoteAuth Token-" + id.incrementAndGet())
        .build(), null);
  }
}
