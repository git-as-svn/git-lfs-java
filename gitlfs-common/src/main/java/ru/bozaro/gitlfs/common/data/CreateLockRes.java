package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;

public final class CreateLockRes {

  @JsonProperty(value = "lock", required = true)
  @Nonnull
  private final Lock lock;

  public CreateLockRes(
      @JsonProperty(value = "lock", required = true) @Nonnull Lock lock
  ) {
    this.lock = lock;
  }

  @Nonnull
  public Lock getLock() {
    return lock;
  }
}
