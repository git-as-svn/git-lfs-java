package ru.bozaro.gitlfs.server;

import ru.bozaro.gitlfs.client.auth.AuthProvider;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Embedded LFS server for servlet testing.
 *
 * @author Artem V. Navrotskiy
 */
public class EmbeddedLfsServer implements AutoCloseable {
  @Nonnull
  private final EmbeddedHttpServer server;
  @Nonnull
  private final MemoryStorage storage;
  @CheckForNull
  private final LockManager lockManager;

  public EmbeddedLfsServer(@Nonnull MemoryStorage storage, @CheckForNull LockManager lockManager) throws Exception {
    this.lockManager = lockManager;
    this.server = new EmbeddedHttpServer();
    this.storage = storage;
    server.addServlet("/foo/bar.git/info/lfs/objects/*", new PointerServlet(storage, "/foo/bar.git/info/lfs/storage/"));
    server.addServlet("/foo/bar.git/info/lfs/storage/*", new ContentServlet(storage));
    if (lockManager != null)
      server.addServlet("/foo/bar.git/info/lfs/locks/*", new LocksServlet(lockManager));
  }

  public AuthProvider getAuthProvider() {
    return storage.getAuthProvider(server.getBase().resolve("/foo/bar.git/info/lfs"));
  }

  @Nonnull
  public MemoryStorage getStorage() {
    return storage;
  }

  @CheckForNull
  public LockManager getLockManager() {
    return lockManager;
  }

  @Override
  public void close() throws Exception {
    server.close();
  }
}
