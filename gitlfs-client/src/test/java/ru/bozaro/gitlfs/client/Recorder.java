package ru.bozaro.gitlfs.client;

import org.apache.commons.httpclient.HttpClient;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;
import ru.bozaro.gitlfs.client.internal.HttpClientExecutor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Simple code for recording replay.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class Recorder {
  public static void main(@NotNull String[] args) throws IOException {
    final ExternalAuthProvider auth = new ExternalAuthProvider("git@github.com:bozaro/test.git");
    final HttpRecorder recorder = new HttpRecorder(new HttpClientExecutor(new HttpClient()));

    final Client client = new Client(auth, recorder);
    doWork(client);

    recorder.getRecords();

    Yaml yaml = YamlHelper.get();
    try (OutputStream replay = new FileOutputStream("replay.yml")) {
      yaml.dumpAll(recorder.getRecords().iterator(), new OutputStreamWriter(replay, StandardCharsets.UTF_8));
    }
  }

  private static void doWork(@NotNull Client client) throws IOException {
    //client.getObject("ebf421bb3e5b6aa398bacd5ec64fcd3d415eeda2df4ec9a40883b7d7d008ee9d");
    client.putObject(new ByteArrayStreamProvider(new Date().toString().getBytes()));
  }
}
