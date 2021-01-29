package ru.bozaro.gitlfs.client.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import ru.bozaro.gitlfs.common.data.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;

import static ru.bozaro.gitlfs.common.Constants.HEADER_ACCEPT;
import static ru.bozaro.gitlfs.common.Constants.MIME_LFS_JSON;

public final class LockCreate implements Request<LockCreate.Res> {
  @Nonnull
  private final String path;
  @CheckForNull
  private final Ref ref;

  public LockCreate(@Nonnull String path, @CheckForNull Ref ref) {
    this.path = path;
    this.ref = ref;
  }

  @Nonnull
  @Override
  public LfsRequest createRequest(@Nonnull ObjectMapper mapper, @Nonnull String url) throws JsonProcessingException {
    final HttpPost req = new HttpPost(url);
    req.addHeader(HEADER_ACCEPT, MIME_LFS_JSON);

    final CreateLockReq createLockReq = new CreateLockReq(path, ref);
    final AbstractHttpEntity entity = new ByteArrayEntity(mapper.writeValueAsBytes(createLockReq));
    entity.setContentType(MIME_LFS_JSON);
    req.setEntity(entity);
    return new LfsRequest(req, entity);
  }

  @Override
  public LockCreate.Res processResponse(@Nonnull ObjectMapper mapper, @Nonnull HttpResponse response) throws IOException {
    switch (response.getStatusLine().getStatusCode()) {
      case HttpStatus.SC_CREATED:
        return new Res(true, mapper.readValue(response.getEntity().getContent(), CreateLockRes.class).getLock(), null);
      case HttpStatus.SC_CONFLICT:
        final LockConflictRes res = mapper.readValue(response.getEntity().getContent(), LockConflictRes.class);
        return new Res(false, res.getLock(), res.getMessage());
      default:
        throw new IllegalStateException();
    }
  }

  @Nonnull
  @Override
  public int[] statusCodes() {
    return new int[]{
        HttpStatus.SC_CREATED,
        HttpStatus.SC_CONFLICT,
    };
  }

  public static final class Res {

    private final boolean success;
    @CheckForNull
    private final String message;
    @Nonnull
    private final Lock lock;

    private Res(boolean success, @Nonnull Lock lock, @CheckForNull String message) {
      this.success = success;
      this.lock = lock;
      this.message = message;
    }

    public boolean isSuccess() {
      return success;
    }

    @Nonnull
    public Lock getLock() {
      return lock;
    }

    @CheckForNull
    public String getMessage() {
      return message;
    }
  }
}
