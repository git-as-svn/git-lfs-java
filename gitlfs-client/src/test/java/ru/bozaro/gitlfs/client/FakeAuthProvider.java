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
  private final boolean chunkedUpload;
  @CheckForNull
  private Link[] auth = null;

  public FakeAuthProvider(boolean chunkedUpload) {
    this.chunkedUpload = chunkedUpload;
  }

  @Nonnull
  @Override
  public Link getAuth(@Nonnull Operation operation) {
    synchronized (lock) {
      if (auth == null) {
        auth = createAuth();
      }
      return auth[operation.ordinal()];
    }
  }

  @Override
  public void invalidateAuth(@Nonnull Operation operation, @Nonnull Link auth) {
    synchronized (lock) {
      if (this.auth != null && (this.auth[0] == auth || this.auth[1] == auth)) {
        this.auth = null;
      }
    }
  }

  @Nonnull
  private Link[] createAuth() {
    final URI uri = URI.create("http://gitlfs.local/test.git/info/lfs");
    final ImmutableMap.Builder<String, String> headers = ImmutableMap.<String, String>builder()
        .put("Authorization", "RemoteAuth Token-" + id.incrementAndGet());

    final Link downloadAuth = new Link(uri, headers.build(), null);

    if (chunkedUpload) {
      headers.put("Transfer-Encoding", "chunked");
    }
    final Link uploadAuth = new Link(uri, headers.build(), null);
    return new Link[]{downloadAuth, uploadAuth};
  }
}
