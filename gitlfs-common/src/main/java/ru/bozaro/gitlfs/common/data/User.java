package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;

public final class User {

  @JsonProperty(value = "name", required = true)
  @Nonnull
  private final String name;

  public User(
      @JsonProperty(value = "name", required = true) @Nonnull String name
  ) {
    this.name = name;
  }

  @Nonnull
  public String getName() {
    return name;
  }
}
