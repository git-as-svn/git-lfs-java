package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.client.io.StreamProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static ru.bozaro.gitlfs.client.Constants.MIME_BINARY;

/**
 * PUT object request.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class ObjectPut implements Request<Void> {
  private final StreamProvider streamProvider;
  private final long size;

  public ObjectPut(StreamProvider streamProvider, long size) {
    this.streamProvider = streamProvider;
    this.size = size;
  }

  @NotNull
  @Override
  public HttpMethod createRequest(@NotNull ObjectMapper mapper, @NotNull String url) throws IOException {
    final PutMethod req = new PutMethod(url);
    req.setRequestEntity(new RequestEntity() {
      @Override
      public boolean isRepeatable() {
        return true;
      }

      @Override
      public void writeRequest(OutputStream out) throws IOException {
        try (InputStream stream = streamProvider.getStream()) {
          byte[] buffer = new byte[4096];
          while (true) {
            int read = stream.read(buffer);
            if (read < 0) break;
            out.write(buffer, 0, read);
          }
        }
      }

      @Override
      public long getContentLength() {
        return size;
      }

      @Override
      public String getContentType() {
        return MIME_BINARY;
      }
    });
    return req;
  }

  @Override
  public Void processResponse(@NotNull ObjectMapper mapper, @NotNull HttpMethod request) throws IOException {
    return null;
  }
}
