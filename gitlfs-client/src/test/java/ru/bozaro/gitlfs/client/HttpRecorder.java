package ru.bozaro.gitlfs.client;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Client with recording all request.
 *
 * @author Artem V. Navrotskiy
 */
public class HttpRecorder implements HttpExecutor {
  @NotNull
  private final HttpExecutor executor;
  @NotNull
  private final List<HttpRecord> records = new ArrayList<>();

  public HttpRecorder(@NotNull HttpExecutor executor) {
    this.executor = executor;
  }

  @NotNull
  @Override
  public CloseableHttpResponse executeMethod(@NotNull HttpUriRequest request) throws IOException {
    final CloseableHttpResponse response = executor.executeMethod(request);
    records.add(new HttpRecord(request, response));
    return response;
  }

  @NotNull
  public List<HttpRecord> getRecords() {
    return records;
  }

  @Override
  public void close() throws IOException {
    executor.close();
  }
}
