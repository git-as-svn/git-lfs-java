package ru.bozaro.gitlfs.client;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.testng.Assert;

import javax.annotation.Nonnull;
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
  @Nonnull
  private final Deque<HttpRecord> records;

  public HttpReplay(@Nonnull List<HttpRecord> records) {
    this.records = new ArrayDeque<>(records);
  }

  @Nonnull
  @Override
  public CloseableHttpResponse executeMethod(@Nonnull HttpUriRequest request) throws IOException {
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
