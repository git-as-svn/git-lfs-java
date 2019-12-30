package ru.bozaro.gitlfs.server;

import ru.bozaro.gitlfs.common.LockConflictException;
import ru.bozaro.gitlfs.common.VerifyLocksResult;
import ru.bozaro.gitlfs.common.data.Lock;
import ru.bozaro.gitlfs.common.data.Ref;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

public interface LockManager {

  @Nonnull
  LockRead checkDownloadAccess(@Nonnull HttpServletRequest request) throws IOException, ForbiddenError, UnauthorizedError;

  @Nonnull
  LockWrite checkUploadAccess(@Nonnull HttpServletRequest request) throws IOException, ForbiddenError, UnauthorizedError;

  interface LockRead {
    @Nonnull
    List<Lock> getLocks(@CheckForNull String path, @CheckForNull String lockId, @CheckForNull Ref ref) throws IOException;
  }

  interface LockWrite extends LockRead {
    @Nonnull
    Lock lock(@Nonnull String path, @CheckForNull Ref ref) throws LockConflictException, IOException;

    @CheckForNull
    Lock unlock(@Nonnull String lockId, boolean force, @CheckForNull Ref ref) throws LockConflictException, IOException;

    @Nonnull
    VerifyLocksResult verifyLocks(@CheckForNull Ref ref) throws IOException;
  }
}
