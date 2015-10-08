package ru.bozaro.gitlfs.server.internal;

import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * HTTP response writer.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public interface ResponseWriter {
  void write(@NotNull HttpServletResponse response) throws IOException;
}
