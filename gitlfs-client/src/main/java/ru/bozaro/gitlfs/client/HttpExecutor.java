package ru.bozaro.gitlfs.client;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.IOException;

/**
 * Abstract class for HTTP connection execution.
 *
 * @author Artem V. Navrotskiy
 */
public interface HttpExecutor extends Closeable {
  @Nonnull
  CloseableHttpResponse executeMethod(@Nonnull HttpUriRequest request) throws IOException;
}
