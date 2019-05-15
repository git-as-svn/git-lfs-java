package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.common.data.CreateLockRes;
import ru.bozaro.gitlfs.common.data.DeleteLockReq;
import ru.bozaro.gitlfs.common.data.Lock;
import ru.bozaro.gitlfs.common.data.Ref;

import java.io.IOException;

import static ru.bozaro.gitlfs.common.Constants.HEADER_ACCEPT;
import static ru.bozaro.gitlfs.common.Constants.MIME_LFS_JSON;

public final class LockDelete implements Request<Lock> {
  private final boolean force;
  @Nullable
  private final Ref ref;

  public LockDelete(boolean force, @Nullable Ref ref) {
    this.force = force;
    this.ref = ref;
  }

  @NotNull
  @Override
  public HttpUriRequest createRequest(@NotNull ObjectMapper mapper, @NotNull String url) throws JsonProcessingException {
    final HttpPost req = new HttpPost(url);
    req.addHeader(HEADER_ACCEPT, MIME_LFS_JSON);

    final DeleteLockReq createLockReq = new DeleteLockReq(force, ref);
    final AbstractHttpEntity entity = new ByteArrayEntity(mapper.writeValueAsBytes(createLockReq));
    entity.setContentType(MIME_LFS_JSON);

    req.setEntity(entity);
    return req;
  }

  @Override
  public Lock processResponse(@NotNull ObjectMapper mapper, @NotNull HttpResponse response) throws IOException {
    switch (response.getStatusLine().getStatusCode()) {
      case HttpStatus.SC_OK:
        return mapper.readValue(response.getEntity().getContent(), CreateLockRes.class).getLock();
      case HttpStatus.SC_NOT_FOUND:
        return null;
      default:
        throw new IllegalStateException();
    }
  }

  @NotNull
  @Override
  public int[] statusCodes() {
    return new int[]{
        HttpStatus.SC_OK,
        HttpStatus.SC_NOT_FOUND,
    };
  }
}
