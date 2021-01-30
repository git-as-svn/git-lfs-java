package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import ru.bozaro.gitlfs.common.data.DeleteLockReq;
import ru.bozaro.gitlfs.common.data.DeleteLockRes;
import ru.bozaro.gitlfs.common.data.Lock;
import ru.bozaro.gitlfs.common.data.Ref;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;

import static ru.bozaro.gitlfs.common.Constants.HEADER_ACCEPT;
import static ru.bozaro.gitlfs.common.Constants.MIME_LFS_JSON;

public final class LockDelete implements Request<Lock> {
  private final boolean force;
  @CheckForNull
  private final Ref ref;

  public LockDelete(boolean force, @CheckForNull Ref ref) {
    this.force = force;
    this.ref = ref;
  }

  @Nonnull
  public LfsRequest createRequest(@Nonnull ObjectMapper mapper, @Nonnull String url) throws JsonProcessingException {
    final HttpPost req = new HttpPost(url);
    req.addHeader(HEADER_ACCEPT, MIME_LFS_JSON);

    final DeleteLockReq createLockReq = new DeleteLockReq(force, ref);
    final AbstractHttpEntity entity = new ByteArrayEntity(mapper.writeValueAsBytes(createLockReq));
    entity.setContentType(MIME_LFS_JSON);

    req.setEntity(entity);
    return new LfsRequest(req, entity);
  }

  @Override
  public Lock processResponse(@Nonnull ObjectMapper mapper, @Nonnull HttpResponse response) throws IOException {
    switch (response.getStatusLine().getStatusCode()) {
      case HttpStatus.SC_OK:
        return mapper.readValue(response.getEntity().getContent(), DeleteLockRes.class).getLock();
      case HttpStatus.SC_NOT_FOUND:
        return null;
      default:
        throw new IllegalStateException();
    }
  }

  @Nonnull
  @Override
  public int[] statusCodes() {
    return new int[]{
        HttpStatus.SC_OK,
        HttpStatus.SC_NOT_FOUND,
    };
  }
}
