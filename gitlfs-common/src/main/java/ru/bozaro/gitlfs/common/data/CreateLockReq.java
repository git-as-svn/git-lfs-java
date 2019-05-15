package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CreateLockReq {

  /**
   * String path name of the locked file.
   */
  @JsonProperty(value = "path", required = true)
  @NotNull
  private final String path;

  /**
   * Optional object describing the server ref that the locks belong to.
   */
  @JsonProperty(value = "ref")
  @Nullable
  private final Ref ref;

  public CreateLockReq(
      @JsonProperty(value = "path", required = true) @NotNull String path,
      @JsonProperty(value = "ref") @Nullable Ref ref) {
    this.path = path;
    this.ref = ref;
  }

  @NotNull
  public String getPath() {
    return path;
  }

  @Nullable
  public Ref getRef() {
    return ref;
  }
}
