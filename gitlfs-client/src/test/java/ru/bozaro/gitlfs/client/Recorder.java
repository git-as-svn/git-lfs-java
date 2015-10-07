package ru.bozaro.gitlfs.client;

import org.apache.commons.httpclient.HttpClient;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;
import ru.bozaro.gitlfs.client.auth.AuthProvider;
import ru.bozaro.gitlfs.client.internal.HttpClientExecutor;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Simple code for recording replay.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
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
      client.getObject("01ba4719c80b6fe911b091a7c05124b64eeece964e09c058ef8f9805daca546b");
    } catch (IOException e) {
    }
  }
}
