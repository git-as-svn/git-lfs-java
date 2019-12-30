package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;

public final class LockConflictRes {

  @JsonProperty(value = "lock", required = true)
  @Nonnull
  private final Lock lock;

  @JsonProperty(value = "message", required = true)
  @Nonnull
  private final String message;

  @JsonCreator
  public LockConflictRes(
      @JsonProperty(value = "message", required = true) @Nonnull String message,
      @JsonProperty(value = "lock", required = true) @Nonnull Lock lock
  ) {
    this.lock = lock;
    this.message = message;
  }

  @Nonnull
  public Lock getLock() {
    return lock;
  }

  @Nonnull
  public String getMessage() {
    return message;
  }
}
