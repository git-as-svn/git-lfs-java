package ru.bozaro.gitlfs.client;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.common.data.Link;
import ru.bozaro.gitlfs.common.data.Operation;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fake authenticator.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class FakeAuthProvider implements AuthProvider {
  @NotNull
  private final Object lock = new Object();
  @NotNull
  private final AtomicInteger id = new AtomicInteger(0);
  @Nullable
  private Link auth = null;

  @NotNull
  @Override
  public Link getAuth(@NotNull Operation operation) throws IOException {
    synchronized (lock) {
      if (auth == null) {
        auth = createAuth();
      }
      return auth;
    }
  }

  @Override
  public void invalidateAuth(@NotNull Operation operation, @NotNull Link auth) {
    synchronized (lock) {
      if (this.auth == auth) {
        this.auth = null;
      }
    }
  }

  @NotNull
  private Link createAuth() {
    return new Link(URI.create("http://gitlfs.local/test.git/info/lfs"), ImmutableMap.<String, String>builder()
        .put("Authorization", "RemoteAuth Token-" + id.incrementAndGet())
        .build(), null);
  }
}
