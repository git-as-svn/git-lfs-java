package ru.bozaro.gitlfs.server;

import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.client.auth.AuthProvider;

/**
 * Embedded LFS server for servlet testing.
 *
 * @author Artem V. Navrotskiy
 */
public class EmbeddedLfsServer implements AutoCloseable {
  @NotNull
  private final EmbeddedHttpServer server;
  @NotNull
  private final MemoryStorage storage;

  public EmbeddedLfsServer(@NotNull MemoryStorage storage) throws Exception {
    this.server = new EmbeddedHttpServer();
    this.storage = storage;
    server.addServlet("/foo/bar.git/info/lfs/objects/*", new PointerServlet(storage, "/foo/bar.git/info/lfs/storage/"));
    server.addServlet("/foo/bar.git/info/lfs/storage/*", new ContentServlet(storage));
  }

  public AuthProvider getAuthProvider() {
    return storage.getAuthProvider(server.getBase().resolve("/foo/bar.git/info/lfs"));
  }

  @NotNull
  public MemoryStorage getStorage() {
    return storage;
  }

  @Override
  public void close() throws Exception {
    server.close();
  }
}
