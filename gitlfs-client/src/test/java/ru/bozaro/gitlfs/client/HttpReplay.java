package ru.bozaro.gitlfs.client;

import org.apache.commons.httpclient.HttpMethod;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Replay recorded HTTP requests.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class HttpReplay implements HttpExecutor {
  @NotNull
  private final Deque<HttpRecord> records;

  public HttpReplay(@NotNull List<HttpRecord> records) {
    this.records = new ArrayDeque<>(records);
  }

  @Override
  public void executeMethod(@NotNull HttpMethod request) throws IOException {
    final HttpRecord record = records.pollFirst();
    Assert.assertNotNull(record);
    Assert.assertEquals(record.getRequest().toString(), new HttpRecord.Request(request).toString());
    record.getResponse().apply(request);
  }

  public void close() {
    Assert.assertTrue(records.isEmpty());
  }
}
