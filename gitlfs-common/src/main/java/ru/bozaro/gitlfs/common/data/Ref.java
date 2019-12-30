package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public final class Ref {

  /**
   * Fully-qualified server refspec.
   */
  @JsonProperty(value = "name", required = true)
  @Nonnull
  private final String name;

  public Ref(@JsonProperty(value = "name", required = true) @Nonnull String name) {
    this.name = name;
  }

  @CheckForNull
  public static Ref create(@CheckForNull String ref) {
    return ref == null ? null : new Ref(ref);
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }
}
