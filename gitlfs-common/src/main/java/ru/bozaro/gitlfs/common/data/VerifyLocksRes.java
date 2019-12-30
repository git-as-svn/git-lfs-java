package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.List;

public final class VerifyLocksRes {
  @JsonProperty(value = "ours", required = true)
  @Nonnull
  private final List<Lock> ours;
  @JsonProperty(value = "theirs", required = true)
  @Nonnull
  private final List<Lock> theirs;

  @JsonProperty(value = "next_cursor")
  @CheckForNull
  private final String nextCursor;

  public VerifyLocksRes(
      @JsonProperty(value = "ours", required = true) @Nonnull List<Lock> ours,
      @JsonProperty(value = "theirs", required = true) @Nonnull List<Lock> theirs,
      @JsonProperty(value = "next_cursor") @CheckForNull String nextCursor) {
    this.ours = ours;
    this.theirs = theirs;
    this.nextCursor = nextCursor;
  }

  @Nonnull
  public List<Lock> getOurs() {
    return ours;
  }

  @Nonnull
  public List<Lock> getTheirs() {
    return theirs;
  }

  @CheckForNull
  public String getNextCursor() {
    return nextCursor;
  }
}
