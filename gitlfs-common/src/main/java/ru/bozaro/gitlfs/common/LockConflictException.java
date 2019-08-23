package ru.bozaro.gitlfs.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.common.data.Lock;

public final class LockConflictException extends Exception {

  @NotNull
  private final Lock lock;

  public LockConflictException(@NotNull Lock lock) {
    this("Lock exists", lock);
  }

  public LockConflictException(@Nullable String message, @NotNull Lock lock) {
    super(message);
    this.lock = lock;
  }

  @NotNull
  public Lock getLock() {
    return lock;
  }
}
