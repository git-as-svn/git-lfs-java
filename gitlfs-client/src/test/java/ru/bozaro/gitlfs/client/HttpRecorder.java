package ru.bozaro.gitlfs.client;

import org.apache.commons.httpclient.HttpMethod;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Client with recording all request.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class HttpRecorder implements HttpExecutor {
  @NotNull
  private final HttpExecutor executor;
  @NotNull
  private final List<HttpRecord> records = new ArrayList<>();

  public HttpRecorder(@NotNull HttpExecutor executor) {
    this.executor = executor;
  }

  @Override
  public void executeMethod(@NotNull HttpMethod request) throws IOException {
    executor.executeMethod(request);
    records.add(new HttpRecord(request));
  }

  @NotNull
  public List<HttpRecord> getRecords() {
    return records;
  }
}
