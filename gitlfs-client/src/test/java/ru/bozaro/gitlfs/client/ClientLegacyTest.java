package ru.bozaro.gitlfs.client;

import com.google.common.io.ByteStreams;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.bozaro.gitlfs.client.exceptions.ForbiddenException;
import ru.bozaro.gitlfs.client.io.StringStreamProvider;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Replay tests for https://github.com/github/git-lfs/blob/master/docs/api/http-v1-original.md
 *
 * @author Artem V. Navrotskiy
 */
public class ClientLegacyTest {
  /**
   * Simple upload.
   */
  @Test
  public void legacyUpload01() throws IOException {
    final HttpReplay replay = YamlHelper.createReplay("/ru/bozaro/gitlfs/client/legacy-upload-01.yml");
    final Client client = new Client(new FakeAuthProvider(false), replay);
    Assert.assertTrue(client.putObject(new StringStreamProvider("Fri Oct 02 21:07:33 MSK 2015")));
    replay.close();
  }

  /**
   * Forbidden.
   */
  @Test(expectedExceptions = ForbiddenException.class)
  public void legacyUpload02() throws IOException {
    final HttpReplay replay = YamlHelper.createReplay("/ru/bozaro/gitlfs/client/legacy-upload-02.yml");
    final Client client = new Client(new FakeAuthProvider(false), replay);
    Assert.assertFalse(client.putObject(new StringStreamProvider("Fri Oct 02 21:07:33 MSK 2015")));
    replay.close();
  }

  /**
   * Expired token.
   */
  @Test
  public void legacyUpload03() throws IOException {
    final HttpReplay replay = YamlHelper.createReplay("/ru/bozaro/gitlfs/client/legacy-upload-03.yml");
    final Client client = new Client(new FakeAuthProvider(false), replay);
    Assert.assertTrue(client.putObject(new StringStreamProvider("Fri Oct 02 21:07:33 MSK 2015")));
    replay.close();
  }

  /**
   * Already uploaded,
   */
  @Test
  public void legacyUpload04() throws IOException {
    final HttpReplay replay = YamlHelper.createReplay("/ru/bozaro/gitlfs/client/legacy-upload-04.yml");
    final Client client = new Client(new FakeAuthProvider(false), replay);
    Assert.assertFalse(client.putObject(new StringStreamProvider("Hello, world!!!")));
    replay.close();
  }

  /**
   * Simple download
   */
  @Test
  public void legacyDownload01() throws IOException {
    final HttpReplay replay = YamlHelper.createReplay("/ru/bozaro/gitlfs/client/legacy-download-01.yml");
    final Client client = new Client(new FakeAuthProvider(false), replay);
    final byte[] data = client.getObject("b810bbe954d51e380f395de0c301a0a42d16f115453f2feb4188ca9f7189074e", ByteStreams::toByteArray);
    Assert.assertEquals(new String(data, StandardCharsets.UTF_8), "Fri Oct 02 21:07:33 MSK 2015");
    replay.close();
  }

  /**
   * Download not uploaded object
   */
  @Test
  public void legacyDownload02() throws IOException {
    final HttpReplay replay = YamlHelper.createReplay("/ru/bozaro/gitlfs/client/legacy-download-02.yml");
    final Client client = new Client(new FakeAuthProvider(false), replay);
    try {
      client.getObject("01ba4719c80b6fe911b091a7c05124b64eeece964e09c058ef8f9805daca546b", ByteStreams::toByteArray);
      Assert.fail();
    } catch (FileNotFoundException ignored) {
    }
    replay.close();
  }

}
