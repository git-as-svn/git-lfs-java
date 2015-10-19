package ru.bozaro.gitlfs.server.internal;

import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.common.JsonHelper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static ru.bozaro.gitlfs.common.Constants.MIME_LFS_JSON;

/**
 * Response with simple JSON output.
 *
 * @author Artem V. Navrotskiy
 */
public class ObjectResponse implements ResponseWriter {
  private int status;
  @NotNull
  private Object value;

  public ObjectResponse(int status, @NotNull Object value) {
    this.status = status;
    this.value = value;
  }

  @Override
  public void write(@NotNull HttpServletResponse response) throws IOException {
    response.setStatus(status);
    response.setContentType(MIME_LFS_JSON);
    JsonHelper.createMapper().writeValue(response.getOutputStream(), value);
  }
}
