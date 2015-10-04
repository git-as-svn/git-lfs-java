package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.common.data.Meta;

import java.io.IOException;

import static ru.bozaro.gitlfs.client.Constants.HEADER_ACCEPT;
import static ru.bozaro.gitlfs.client.Constants.MIME_LFS_JSON;

/**
 * Object verification after upload request.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class ObjectVerify implements Request<Void> {
  @NotNull
  private final Meta meta;

  public ObjectVerify(@NotNull Meta meta) {
    this.meta = meta;
  }

  @NotNull
  @Override
  public HttpMethod createRequest(@NotNull ObjectMapper mapper, @NotNull String url) throws IOException {
    final PostMethod req = new PostMethod(url);
    req.addRequestHeader(HEADER_ACCEPT, MIME_LFS_JSON);
    final byte[] content = mapper.writeValueAsBytes(meta);
    req.setRequestEntity(new ByteArrayRequestEntity(content, MIME_LFS_JSON));
    return req;
  }

  @Nullable
  @Override
  public int[] statusCodes() {
    return null;
  }

  @Override
  public Void processResponse(@NotNull ObjectMapper mapper, @NotNull HttpMethod request) throws IOException {
    return null;
  }
}
