package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class VerifyLocksRes {
  @JsonProperty(value = "ours", required = true)
  @NotNull
  private final List<Lock> ours;
  @JsonProperty(value = "theirs", required = true)
  @NotNull
  private final List<Lock> theirs;

  @JsonProperty(value = "next_cursor")
  @Nullable
  private final String nextCursor;

  public VerifyLocksRes(
      @JsonProperty(value = "ours", required = true) @NotNull List<Lock> ours,
      @JsonProperty(value = "theirs", required = true) @NotNull List<Lock> theirs,
      @JsonProperty(value = "next_cursor") @Nullable String nextCursor) {
    this.ours = ours;
    this.theirs = theirs;
    this.nextCursor = nextCursor;
  }

  @NotNull
  public List<Lock> getOurs() {
    return ours;
  }

  @NotNull
  public List<Lock> getTheirs() {
    return theirs;
  }

  @Nullable
  public String getNextCursor() {
    return nextCursor;
  }
}
