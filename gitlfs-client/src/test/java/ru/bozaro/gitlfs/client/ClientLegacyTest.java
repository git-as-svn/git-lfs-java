package ru.bozaro.gitlfs.client;

import com.google.common.io.ByteStreams;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Replay tests for https://github.com/github/git-lfs/blob/master/docs/api/http-v1-original.md
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class ClientLegacyTest {
  @Test
  public void legacyUpload01() throws IOException {
    AuthProvider auth = new FakeAuthProvider();
    final HttpReplay replay = YamlHelper.createReplay("/ru/bozaro/gitlfs/client/legacy-upload-01.yml");
    Client client = new Client(auth, replay);
    client.putObject(new StringStreamProvider("Fri Oct 02 21:07:33 MSK 2015"));
  }

  @Test
  public void legacyDownload01() throws IOException {
    final HttpReplay replay = YamlHelper.createReplay("/ru/bozaro/gitlfs/client/legacy-download-01.yml");
    final Client client = new Client(new FakeAuthProvider(), replay);
    final byte[] data = ByteStreams.toByteArray(client.getObject("b810bbe954d51e380f395de0c301a0a42d16f115453f2feb4188ca9f7189074e"));
    Assert.assertEquals(new String(data, StandardCharsets.UTF_8), "Fri Oct 02 21:07:33 MSK 2015");
  }
}
