package ru.bozaro.gitlfs.common.client.internal;

import org.apache.commons.httpclient.HttpMethod;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Single HTTP request.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public interface Request<T extends HttpMethod, R> {
  @NotNull
  T createRequest(@NotNull String url);

  R processResponse(@NotNull T request) throws IOException;
}
