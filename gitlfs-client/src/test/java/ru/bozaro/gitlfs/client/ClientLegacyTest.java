package ru.bozaro.gitlfs.client;

import org.testng.annotations.Test;

import java.io.IOException;

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

}
