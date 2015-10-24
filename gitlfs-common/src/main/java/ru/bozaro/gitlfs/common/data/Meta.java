package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

/**
 * LFS object location.
 *
 * @author Artem V. Navrotskiy
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Meta {
  @JsonProperty(value = "oid", required = true)
  @NotNull
  private final String oid;

  @JsonProperty(value = "size", required = true)
  private final long size;

  @JsonCreator
  public Meta(
      @JsonProperty(value = "oid", required = true)
      @NotNull
      String oid,
      @JsonProperty(value = "size", required = true)
      long size
  ) {
    this.oid = oid;
    this.size = size;
  }

  @NotNull
  public String getOid() {
    return oid;
  }

  public long getSize() {
    return size;
  }
}
