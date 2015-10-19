package ru.bozaro.gitlfs.client;

import org.apache.commons.httpclient.HttpMethod;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Abstract class for HTTP connection execution.
 *
 * @author Artem V. Navrotskiy
 */
public interface HttpExecutor {
  void executeMethod(@NotNull HttpMethod request) throws IOException;
}
