package ru.bozaro.gitlfs.client;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Abstract class for HTTP connection execution.
 *
 * @author Artem V. Navrotskiy
 */
public interface HttpExecutor {
  @NotNull
  HttpResponse executeMethod(@NotNull HttpUriRequest request) throws IOException;
}
