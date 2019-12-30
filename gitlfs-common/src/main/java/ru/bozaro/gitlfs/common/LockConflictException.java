package ru.bozaro.gitlfs.common;

import ru.bozaro.gitlfs.common.data.Lock;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public final class LockConflictException extends Exception {

  @Nonnull
  private final Lock lock;

  public LockConflictException(@Nonnull Lock lock) {
    this("Lock exists", lock);
  }

  public LockConflictException(@CheckForNull String message, @Nonnull Lock lock) {
    super(message);
    this.lock = lock;
  }

  @Nonnull
  public Lock getLock() {
    return lock;
  }
}
