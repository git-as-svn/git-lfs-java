package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

public final class LockConflictRes {

  @JsonProperty(value = "lock", required = true)
  @NotNull
  private final Lock lock;

  @JsonProperty(value = "message", required = true)
  @NotNull
  private final String message;

  @JsonCreator
  public LockConflictRes(
      @JsonProperty(value = "message", required = true) @NotNull String message,
      @JsonProperty(value = "lock", required = true) @NotNull Lock lock
  ) {
    this.lock = lock;
    this.message = message;
  }

  @NotNull
  public Lock getLock() {
    return lock;
  }

  @NotNull
  public String getMessage() {
    return message;
  }
}
