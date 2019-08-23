package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

public final class DeleteLockRes {

  @JsonProperty(value = "lock", required = true)
  @NotNull
  private final Lock lock;

  public DeleteLockRes(
      @JsonProperty(value = "lock", required = true) @NotNull Lock lock
  ) {
    this.lock = lock;
  }

  @NotNull
  public Lock getLock() {
    return lock;
  }
}
