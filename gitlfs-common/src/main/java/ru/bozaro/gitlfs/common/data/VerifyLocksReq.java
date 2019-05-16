package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

public final class VerifyLocksReq {

  /**
   * Optional cursor to allow pagination.
   */
  @JsonProperty(value = "cursor")
  @Nullable
  private final String cursor;

  /**
   * Optional object describing the server ref that the locks belong to.
   */
  @JsonProperty(value = "ref")
  @Nullable
  private final Ref ref;

  /**
   * Optional limit to how many locks to return.
   */
  @JsonProperty(value = "limit")
  @Nullable
  private final Integer limit;

  public VerifyLocksReq(
      @JsonProperty(value = "cursor") @Nullable String cursor,
      @JsonProperty(value = "ref") @Nullable Ref ref,
      @JsonProperty(value = "limit") @Nullable Integer limit) {
    this.cursor = cursor;
    this.ref = ref;
    this.limit = limit;
  }

  @Nullable
  public String getCursor() {
    return cursor;
  }

  @Nullable
  public Ref getRef() {
    return ref;
  }

  @Nullable
  public Integer getLimit() {
    return limit;
  }
}
