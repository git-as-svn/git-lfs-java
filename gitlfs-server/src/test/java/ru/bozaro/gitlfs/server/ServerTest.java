package ru.bozaro.gitlfs.server;

import com.google.common.io.ByteStreams;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.bozaro.gitlfs.client.Client;
import ru.bozaro.gitlfs.client.auth.BasicAuthProvider;
import ru.bozaro.gitlfs.client.io.StringStreamProvider;
import ru.bozaro.gitlfs.common.data.BatchReq;
import ru.bozaro.gitlfs.common.data.Meta;
import ru.bozaro.gitlfs.common.data.Operation;

import java.io.FileNotFoundException;
import java.util.Collections;

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
      server.addServlet("/foo/bar.git/info/lfs/objects/*", new PointerServlet(storage, "/foo/bar.git/info/lfs/storage/"));
      server.addServlet("/foo/bar.git/info/lfs/storage/*", new ContentServlet(storage));

      final BasicAuthProvider auth = new BasicAuthProvider(server.getBase().resolve("/foo/bar.git/info/lfs"));
      final Client client = new Client(auth);
      final StringStreamProvider streamProvider = new StringStreamProvider("Hello, world");
      final Meta meta = client.generateMeta(streamProvider);
      // Not uploaded yet.
      try {
        client.getObject(meta.getOid(), new ByteStreamHandler());
        Assert.fail();
      } catch (FileNotFoundException ignored) {
      }
      client.postBatch(new BatchReq(Operation.Download, Collections.singletonList(meta)));
      // Can upload.
      Assert.assertTrue(client.putObject(streamProvider, meta));
      // Can download uploaded.
      final byte[] content = client.getObject(meta.getOid(), new ByteStreamHandler());
      Assert.assertEquals(content, ByteStreams.toByteArray(streamProvider.getStream()));
      // Already uploaded.
      Assert.assertFalse(client.putObject(streamProvider, meta));
    }
  }
}
