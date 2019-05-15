package ru.bozaro.gitlfs.common;

import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.common.data.Lock;

public final class LockConflictException extends Exception {

  public LockConflictException(@NotNull Lock lock) {
    this.lock = lock;
  }

  @NotNull
  private final Lock lock;

  @NotNull
  public Lock getLock() {
    return lock;
  }
}
