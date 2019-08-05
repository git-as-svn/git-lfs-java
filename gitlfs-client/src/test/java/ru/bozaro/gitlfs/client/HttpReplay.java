package ru.bozaro.gitlfs.client;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Replay recorded HTTP requests.
 *
 * @author Artem V. Navrotskiy
 */
public class HttpReplay implements HttpExecutor {
  @NotNull
  private final Deque<HttpRecord> records;

  public HttpReplay(@NotNull List<HttpRecord> records) {
    this.records = new ArrayDeque<>(records);
  }

  @NotNull
  @Override
  public CloseableHttpResponse executeMethod(@NotNull HttpUriRequest request) throws IOException {
    final HttpRecord record = records.pollFirst();
    Assert.assertNotNull(record);

    final String expected = record.getRequest().toString();
    final String actual = new HttpRecord.Request(request).toString();
    Assert.assertEquals(actual, expected);

    return record.getResponse().toHttpResponse();
  }

  public void close() {
    Assert.assertTrue(records.isEmpty());
  }
}
