package ru.bozaro.gitlfs.server.internal;

import jakarta.servlet.http.HttpServletResponse;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * HTTP response writer.
 *
 * @author Artem V. Navrotskiy
 */
public interface ResponseWriter {
  void write(@Nonnull HttpServletResponse response) throws IOException;
}
