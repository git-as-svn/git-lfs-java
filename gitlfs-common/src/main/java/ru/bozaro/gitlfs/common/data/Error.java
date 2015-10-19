package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

/**
 * LFS error description.
 *
 * @author Artem V. Navrotskiy
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Error {
  @JsonProperty(value = "code", required = true)
  private final int code;

  @JsonProperty(value = "message", required = false)
  @Nullable
  private final String message;

  @JsonCreator
  public Error(
      @JsonProperty(value = "code")
      int code,
      @JsonProperty(value = "message")
      @Nullable
      String message
  ) {
    this.code = code;
    this.message = message;
  }

  public int getCode() {
    return code;
  }

  @Nullable
  public String getMessage() {
    return message;
  }
}
