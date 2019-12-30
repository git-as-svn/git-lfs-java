package ru.bozaro.gitlfs.server.internal;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * HTTP response writer.
 *
 * @author Artem V. Navrotskiy
 */
public interface ResponseWriter {
  void write(@Nonnull HttpServletResponse response) throws IOException;
}
