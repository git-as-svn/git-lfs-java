package ru.bozaro.gitlfs.server;

import com.google.common.base.Strings;
import ru.bozaro.gitlfs.common.LockConflictException;
import ru.bozaro.gitlfs.common.VerifyLocksResult;
import ru.bozaro.gitlfs.common.data.Lock;
import ru.bozaro.gitlfs.common.data.Ref;
import ru.bozaro.gitlfs.common.data.User;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
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
  @Nonnull
  private final AtomicInteger nextId = new AtomicInteger(1);

  @Nonnull
  private final List<Lock> locks = new ArrayList<>();
  @Nonnull
  private final ContentManager contentManager;

  public MemoryLockManager(@Nonnull ContentManager contentManager) {
    this.contentManager = contentManager;
  }

  @Override
  @Nonnull
  public LockRead checkDownloadAccess(@Nonnull HttpServletRequest request) throws IOException, ForbiddenError, UnauthorizedError {
    contentManager.checkDownloadAccess(request);
    return this;
  }

  @Override
  @Nonnull
  public LockWrite checkUploadAccess(@Nonnull HttpServletRequest request) throws IOException, ForbiddenError, UnauthorizedError {
    contentManager.checkUploadAccess(request);
    return this;
  }

  @Override
  @Nonnull
  public List<Lock> getLocks(@CheckForNull String path, @CheckForNull String lockId, @CheckForNull Ref ref) {
    Stream<Lock> stream = locks.stream();

    if (!Strings.isNullOrEmpty(path))
      stream = stream.filter(lock -> lock.getPath().equals(path));

    if (!Strings.isNullOrEmpty(lockId))
      stream = stream.filter(lock -> lock.getId().equals(lockId));

    return stream.collect(Collectors.toList());
  }

  @Override
  @Nonnull
  public Lock lock(@Nonnull String path, @CheckForNull Ref ref) throws LockConflictException {
    for (Lock lock : locks)
      if (lock.getPath().equals(path))
        throw new LockConflictException(lock);

    final Lock lock = new Lock(String.valueOf(nextId.incrementAndGet()), path, new Date(), new User("Jane Doe"));
    locks.add(lock);
    return lock;
  }

  @Override
  @CheckForNull
  public Lock unlock(@Nonnull String lockId, boolean force, @CheckForNull Ref ref) {
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
  public @Nonnull
  VerifyLocksResult verifyLocks(@CheckForNull Ref ref) {
    return new VerifyLocksResult(locks, Collections.emptyList());
  }
}
