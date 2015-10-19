package ru.bozaro.gitlfs.server.internal;

import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * HTTP response writer.
 *
 * @author Artem V. Navrotskiy
 */
public interface ResponseWriter {
  void write(@NotNull HttpServletResponse response) throws IOException;
}
