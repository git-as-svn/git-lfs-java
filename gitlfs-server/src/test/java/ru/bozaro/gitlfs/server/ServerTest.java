package ru.bozaro.gitlfs.server;

import com.google.common.io.ByteStreams;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.bozaro.gitlfs.client.Client;
import ru.bozaro.gitlfs.client.auth.AuthProvider;
import ru.bozaro.gitlfs.client.io.StringStreamProvider;
import ru.bozaro.gitlfs.common.data.BatchReq;
import ru.bozaro.gitlfs.common.data.Meta;
import ru.bozaro.gitlfs.common.data.Operation;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;

/**
 * Git LFS server implementation test.
 *
 * @author Artem V. Navrotskiy
 */
public class ServerTest {
  @Test
  public void simpleTest() throws Exception {
    try (final EmbeddedLfsServer server = new EmbeddedLfsServer(new MemoryStorage(-1))) {
      final AuthProvider auth = server.getAuthProvider();
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
