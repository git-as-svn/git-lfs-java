package ru.bozaro.gitlfs.common;

import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.common.data.Lock;

import java.util.List;

public final class VerifyLocksResult {
  @NotNull
  private final List<Lock> ourLocks;
  @NotNull
  private final List<Lock> theirLocks;

  public VerifyLocksResult(@NotNull List<Lock> ourLocks, @NotNull List<Lock> theirLocks) {
    this.ourLocks = ourLocks;
    this.theirLocks = theirLocks;
  }

  @NotNull
  public List<Lock> getOurLocks() {
    return ourLocks;
  }

  @NotNull
  public List<Lock> getTheirLocks() {
    return theirLocks;
  }
}
