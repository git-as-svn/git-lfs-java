package ru.bozaro.gitlfs.server.internal;

import jakarta.servlet.http.HttpServletResponse;
import ru.bozaro.gitlfs.common.JsonHelper;

import javax.annotation.Nonnull;
import java.io.IOException;

import static ru.bozaro.gitlfs.common.Constants.MIME_LFS_JSON;

/**
 * Response with simple JSON output.
 *
 * @author Artem V. Navrotskiy
 */
public class ObjectResponse implements ResponseWriter {
  private int status;
  @Nonnull
  private Object value;

  public ObjectResponse(int status, @Nonnull Object value) {
    this.status = status;
    this.value = value;
  }

  @Override
  public void write(@Nonnull HttpServletResponse response) throws IOException {
    response.setStatus(status);
    response.setContentType(MIME_LFS_JSON);
    JsonHelper.mapper.writeValue(response.getOutputStream(), value);
  }
}
