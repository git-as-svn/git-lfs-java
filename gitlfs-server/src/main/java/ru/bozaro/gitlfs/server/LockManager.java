package ru.bozaro.gitlfs.server;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.common.LockConflictException;
import ru.bozaro.gitlfs.common.VerifyLocksResult;
import ru.bozaro.gitlfs.common.data.Lock;
import ru.bozaro.gitlfs.common.data.Ref;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

public interface LockManager {

  @NotNull
  LockRead checkDownloadAccess(@NotNull HttpServletRequest request) throws IOException, ForbiddenError, UnauthorizedError;

  @NotNull
  LockWrite checkUploadAccess(@NotNull HttpServletRequest request) throws IOException, ForbiddenError, UnauthorizedError;

  interface LockRead {
    @NotNull
    List<Lock> getLocks(@Nullable String path, @Nullable String lockId, @Nullable Ref ref) throws IOException;
  }

  interface LockWrite extends LockRead {
    @NotNull
    Lock lock(@NotNull String path, @Nullable Ref ref) throws LockConflictException, IOException;

    @Nullable
    Lock unlock(@NotNull String lockId, boolean force, @Nullable Ref ref) throws LockConflictException, IOException;

    @NotNull
    VerifyLocksResult verifyLocks(@Nullable Ref ref) throws IOException;
  }
}
