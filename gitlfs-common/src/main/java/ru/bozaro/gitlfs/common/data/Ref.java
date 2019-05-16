package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Ref {

  /**
   * Fully-qualified server refspec.
   */
  @JsonProperty(value = "name", required = true)
  @NotNull
  private final String name;

  public Ref(@JsonProperty(value = "name", required = true) @NotNull String name) {
    this.name = name;
  }

  @Nullable
  public static Ref create(@Nullable String ref) {
    return ref == null ? null : new Ref(ref);
  }

  @NotNull
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }
}
