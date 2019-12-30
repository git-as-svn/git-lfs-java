package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public final class CreateLockReq {

  /**
   * String path name of the locked file.
   */
  @JsonProperty(value = "path", required = true)
  @Nonnull
  private final String path;

  /**
   * Optional object describing the server ref that the locks belong to.
   */
  @JsonProperty(value = "ref")
  @CheckForNull
  private final Ref ref;

  public CreateLockReq(
      @JsonProperty(value = "path", required = true) @Nonnull String path,
      @JsonProperty(value = "ref") @CheckForNull Ref ref) {
    this.path = path;
    this.ref = ref;
  }

  @Nonnull
  public String getPath() {
    return path;
  }

  @CheckForNull
  public Ref getRef() {
    return ref;
  }
}
