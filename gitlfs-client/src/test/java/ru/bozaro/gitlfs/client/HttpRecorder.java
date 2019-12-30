package ru.bozaro.gitlfs.client;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Client with recording all request.
 *
 * @author Artem V. Navrotskiy
 */
public class HttpRecorder implements HttpExecutor {
  @Nonnull
  private final HttpExecutor executor;
  @Nonnull
  private final List<HttpRecord> records = new ArrayList<>();

  public HttpRecorder(@Nonnull HttpExecutor executor) {
    this.executor = executor;
  }

  @Nonnull
  @Override
  public CloseableHttpResponse executeMethod(@Nonnull HttpUriRequest request) throws IOException {
    final CloseableHttpResponse response = executor.executeMethod(request);
    records.add(new HttpRecord(request, response));
    return response;
  }

  @Nonnull
  public List<HttpRecord> getRecords() {
    return records;
  }

  @Override
  public void close() throws IOException {
    executor.close();
  }
}
