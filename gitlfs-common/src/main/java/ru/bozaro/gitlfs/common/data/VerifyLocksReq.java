package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.CheckForNull;

public final class VerifyLocksReq {

  /**
   * Optional cursor to allow pagination.
   */
  @JsonProperty(value = "cursor")
  @CheckForNull
  private final String cursor;

  /**
   * Optional object describing the server ref that the locks belong to.
   */
  @JsonProperty(value = "ref")
  @CheckForNull
  private final Ref ref;

  /**
   * Optional limit to how many locks to return.
   */
  @JsonProperty(value = "limit")
  @CheckForNull
  private final Integer limit;

  public VerifyLocksReq(
      @JsonProperty(value = "cursor") @CheckForNull String cursor,
      @JsonProperty(value = "ref") @CheckForNull Ref ref,
      @JsonProperty(value = "limit") @CheckForNull Integer limit) {
    this.cursor = cursor;
    this.ref = ref;
    this.limit = limit;
  }

  @CheckForNull
  public String getCursor() {
    return cursor;
  }

  @CheckForNull
  public Ref getRef() {
    return ref;
  }

  @CheckForNull
  public Integer getLimit() {
    return limit;
  }
}
