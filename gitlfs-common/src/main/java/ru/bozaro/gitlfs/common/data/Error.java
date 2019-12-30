package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.CheckForNull;

/**
 * LFS error description.
 *
 * @author Artem V. Navrotskiy
 */
public final class Error {
  @JsonProperty(value = "code", required = true)
  private final int code;

  @JsonProperty(value = "message")
  @CheckForNull
  private final String message;

  @JsonCreator
  public Error(
      @JsonProperty(value = "code") int code,
      @JsonProperty(value = "message") @CheckForNull String message
  ) {
    this.code = code;
    this.message = message;
  }

  public int getCode() {
    return code;
  }

  @CheckForNull
  public String getMessage() {
    return message;
  }
}
