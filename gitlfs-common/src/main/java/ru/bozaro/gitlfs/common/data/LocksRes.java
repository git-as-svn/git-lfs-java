package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.List;

public final class LocksRes {
  @JsonProperty(value = "locks", required = true)
  @Nonnull
  private final List<Lock> locks;

  @JsonProperty(value = "next_cursor")
  @CheckForNull
  private final String nextCursor;

  public LocksRes(
      @JsonProperty(value = "locks", required = true) @Nonnull List<Lock> locks,
      @JsonProperty(value = "next_cursor") @CheckForNull String nextCursor) {
    this.locks = locks;
    this.nextCursor = nextCursor;
  }

  @Nonnull
  public List<Lock> getLocks() {
    return locks;
  }

  @CheckForNull
  public String getNextCursor() {
    return nextCursor;
  }
}
