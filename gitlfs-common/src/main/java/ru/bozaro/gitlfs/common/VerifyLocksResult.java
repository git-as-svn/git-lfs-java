package ru.bozaro.gitlfs.common;

import ru.bozaro.gitlfs.common.data.Lock;

import javax.annotation.Nonnull;
import java.util.List;

public final class VerifyLocksResult {
  @Nonnull
  private final List<Lock> ourLocks;
  @Nonnull
  private final List<Lock> theirLocks;

  public VerifyLocksResult(@Nonnull List<Lock> ourLocks, @Nonnull List<Lock> theirLocks) {
    this.ourLocks = ourLocks;
    this.theirLocks = theirLocks;
  }

  @Nonnull
  public List<Lock> getOurLocks() {
    return ourLocks;
  }

  @Nonnull
  public List<Lock> getTheirLocks() {
    return theirLocks;
  }
}
