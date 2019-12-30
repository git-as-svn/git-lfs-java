package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.CheckForNull;

public final class DeleteLockReq {

  /**
   * Optional boolean specifying that the user is deleting another user's lock.
   */
  @JsonProperty(value = "force")
  @CheckForNull
  private final Boolean force;

  /**
   * Optional object describing the server ref that the locks belong to.
   */
  @JsonProperty(value = "ref")
  @CheckForNull
  private final Ref ref;

  public DeleteLockReq(
      @JsonProperty(value = "force") @CheckForNull Boolean force,
      @JsonProperty(value = "ref") @CheckForNull Ref ref) {
    this.force = force;
    this.ref = ref;
  }

  public boolean isForce() {
    return force != null && force;
  }

  @CheckForNull
  public Ref getRef() {
    return ref;
  }
}
