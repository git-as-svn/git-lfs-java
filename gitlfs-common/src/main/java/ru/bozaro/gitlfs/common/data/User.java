package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

public final class User {

  @JsonProperty(value = "name", required = true)
  @NotNull
  private final String name;

  public User(
      @JsonProperty(value = "name", required = true) @NotNull String name
  ) {
    this.name = name;
  }

  @NotNull
  public String getName() {
    return name;
  }
}
