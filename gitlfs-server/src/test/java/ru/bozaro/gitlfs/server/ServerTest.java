package ru.bozaro.gitlfs.server;

import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.bozaro.gitlfs.client.Client;
import ru.bozaro.gitlfs.client.auth.BasicAuthProvider;
import ru.bozaro.gitlfs.client.io.StringStreamProvider;
import ru.bozaro.gitlfs.common.data.*;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.TreeMap;

/**
 * Git LFS server implementation test.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class ServerTest {
  @Test
  public void simpleTest() throws Exception {
    try (final EmbeddedHttpServer server = new EmbeddedHttpServer()) {
      final MemoryStorage storage = new MemoryStorage();
      final URI storageUri = server.getBase().resolve("/foo/bar.git/info/lfs/storage/");
      server.addServlet("/foo/bar.git/info/lfs/objects/*", new PointerServlet<>(new PointerManager() {
        @Override
        public Object checkAccess(@NotNull HttpServletRequest request, @NotNull Operation operation) {
          return null;
        }

        @NotNull
        @Override
        public BatchItem[] getLocations(Object context, @NotNull Operation operation, @NotNull Meta[] metas) throws IOException {
          final BatchItem[] result = new BatchItem[metas.length];
          for (int i = 0; i < metas.length; ++i) {
            result[i] = getLocation(context, operation, metas[i]);
          }
          return result;
        }

        @NotNull
        public BatchItem getLocation(Object context, @NotNull Operation operation, @NotNull Meta meta) throws IOException {
          final TreeMap<LinkType, Link> links = new TreeMap<>();
          if (storage.getMetadata(meta.getOid()) != null) {
            links.put(LinkType.Download, createLink(context, meta));
          } else {
            links.put(LinkType.Upload, createLink(context, meta));
          }
          return new BatchItem(meta, links);
        }

        public Link createLink(Object context, @NotNull Meta meta) {
          return new Link(storageUri.resolve(meta.getOid()), null, null);
        }
      }));
      server.addServlet("/foo/bar.git/info/lfs/storage/*", new ContentServlet<>(storage));

      final BasicAuthProvider auth = new BasicAuthProvider(server.getBase().resolve("/foo/bar.git/info/lfs"));
      final Client client = new Client(auth);
      final StringStreamProvider streamProvider = new StringStreamProvider("Hello, world");
      final Meta meta = client.generateMeta(streamProvider);
      // Not uploaded yet.
      try {
        client.getObject(meta.getOid());
        Assert.fail();
      } catch (FileNotFoundException ignored) {
      }
      client.postBatch(new BatchReq(Operation.Download, Collections.singletonList(meta)));
      // Can upload.
      Assert.assertTrue(client.putObject(streamProvider, meta));
      // Can download uploaded.
      try (final InputStream stream = client.getObject(meta.getOid())) {
        Assert.assertEquals(ByteStreams.toByteArray(stream), ByteStreams.toByteArray(streamProvider.getStream()));
      }
      // Already uploaded.
      Assert.assertFalse(client.putObject(streamProvider, meta));
    }
  }
}
