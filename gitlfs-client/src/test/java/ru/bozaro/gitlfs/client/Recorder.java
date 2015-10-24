package ru.bozaro.gitlfs.client;

import org.apache.commons.httpclient.HttpClient;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;
import ru.bozaro.gitlfs.client.auth.AuthProvider;
import ru.bozaro.gitlfs.client.internal.HttpClientExecutor;
import ru.bozaro.gitlfs.common.data.BatchReq;
import ru.bozaro.gitlfs.common.data.Meta;
import ru.bozaro.gitlfs.common.data.Operation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Simple code for recording replay.
 *
 * @author Artem V. Navrotskiy
 */
public class Recorder {
  public static void main(@NotNull String[] args) throws IOException {
    final AuthProvider auth = AuthHelper.create("git@github.com:bozaro/test.git");
    final HttpRecorder recorder = new HttpRecorder(new HttpClientExecutor(new HttpClient()));

    doWork(new Client(auth, recorder));

    final Yaml yaml = YamlHelper.get();
    final File file = new File("build/replay.yml");
    //noinspection ResultOfMethodCallIgnored
    file.getParentFile().mkdirs();
    try (OutputStream replay = new FileOutputStream(file)) {
      yaml.dumpAll(recorder.getRecords().iterator(), new OutputStreamWriter(replay, StandardCharsets.UTF_8));
    }
  }

  private static void doWork(@NotNull Client client) throws IOException {
    try {
      client.postBatch(new BatchReq(
          Operation.Upload,
          Arrays.asList(
              new Meta("b810bbe954d51e380f395de0c301a0a42d16f115453f2feb4188ca9f7189074e", 28),
              new Meta("1cbec737f863e4922cee63cc2ebbfaafcd1cff8b790d8cfd2e6a5d550b648afa", 3)
          )
      ));
    } catch (IOException e) {
    }
  }
}
