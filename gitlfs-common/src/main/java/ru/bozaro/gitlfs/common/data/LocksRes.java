package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class LocksRes {
  @JsonProperty(value = "locks", required = true)
  @NotNull
  private final List<Lock> locks;

  @JsonProperty(value = "next_cursor")
  @Nullable
  private final String nextCursor;

  public LocksRes(
      @JsonProperty(value = "locks", required = true) @NotNull List<Lock> locks,
      @JsonProperty(value = "next_cursor") @Nullable String nextCursor) {
    this.locks = locks;
    this.nextCursor = nextCursor;
  }

  @NotNull
  public List<Lock> getLocks() {
    return locks;
  }

  @Nullable
  public String getNextCursor() {
    return nextCursor;
  }
}
