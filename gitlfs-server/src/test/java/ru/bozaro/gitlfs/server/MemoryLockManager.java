package ru.bozaro.gitlfs.server;

import com.google.common.base.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.common.LockConflictException;
import ru.bozaro.gitlfs.common.VerifyLocksResult;
import ru.bozaro.gitlfs.common.data.Lock;
import ru.bozaro.gitlfs.common.data.Ref;
import ru.bozaro.gitlfs.common.data.User;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MemoryLockManager implements LockManager, LockManager.LockWrite {
  @NotNull
  private final AtomicInteger nextId = new AtomicInteger(1);

  @NotNull
  private final List<Lock> locks = new ArrayList<>();
  @NotNull
  private final ContentManager contentManager;

  public MemoryLockManager(@NotNull ContentManager contentManager) {
    this.contentManager = contentManager;
  }

  @Override
  @NotNull
  public LockRead checkDownloadAccess(@NotNull HttpServletRequest request) throws IOException, ForbiddenError, UnauthorizedError {
    contentManager.checkDownloadAccess(request);
    return this;
  }

  @Override
  @NotNull
  public LockWrite checkUploadAccess(@NotNull HttpServletRequest request) throws IOException, ForbiddenError, UnauthorizedError {
    contentManager.checkUploadAccess(request);
    return this;
  }

  @Override
  @NotNull
  public List<Lock> getLocks(@Nullable String path, @Nullable String lockId, @Nullable Ref ref) {
    Stream<Lock> stream = locks.stream();

    if (!Strings.isNullOrEmpty(path))
      stream = stream.filter(lock -> lock.getPath().equals(path));

    if (!Strings.isNullOrEmpty(lockId))
      stream = stream.filter(lock -> lock.getId().equals(lockId));

    return stream.collect(Collectors.toList());
  }

  @Override
  @NotNull
  public Lock lock(@NotNull String path, @Nullable Ref ref) throws LockConflictException {
    for (Lock lock : locks)
      if (lock.getPath().equals(path))
        throw new LockConflictException(lock);

    final Lock lock = new Lock(String.valueOf(nextId.incrementAndGet()), path, new Date(), new User("Jane Doe"));
    locks.add(lock);
    return lock;
  }

  @Override
  @Nullable
  public Lock unlock(@NotNull String lockId, boolean force, @Nullable Ref ref) throws LockConflictException {
    Lock lock = null;
    for (Lock l : locks) {
      if (l.getId().equals(lockId)) {
        lock = l;
        break;
      }
    }

    if (lock == null)
      return null;

    locks.remove(lock);
    return lock;
  }

  @Override
  public @NotNull VerifyLocksResult verifyLocks(@Nullable Ref ref) {
    return new VerifyLocksResult(locks, Collections.emptyList());
  }
}
