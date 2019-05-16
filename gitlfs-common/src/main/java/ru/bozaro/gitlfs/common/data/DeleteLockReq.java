package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

public final class DeleteLockReq {

  /**
   * Optional boolean specifying that the user is deleting another user's lock.
   */
  @JsonProperty(value = "force")
  @Nullable
  private final Boolean force;

  /**
   * Optional object describing the server ref that the locks belong to.
   */
  @JsonProperty(value = "ref")
  @Nullable
  private final Ref ref;

  public DeleteLockReq(
      @JsonProperty(value = "force") @Nullable Boolean force,
      @JsonProperty(value = "ref") @Nullable Ref ref) {
    this.force = force;
    this.ref = ref;
  }

  public boolean isForce() {
    return force != null && force;
  }

  @Nullable
  public Ref getRef() {
    return ref;
  }
}
