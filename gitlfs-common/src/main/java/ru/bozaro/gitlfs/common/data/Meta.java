package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

/**
 * LFS object location.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class Meta {
  @JsonProperty(value = "oid", required = true)
  @Nullable
  private String oid = "";

  @JsonProperty(value = "size", required = true)
  @Nullable
  private Long size = 0L;

  protected Meta() {
  }

  public Meta(@Nullable String oid, @Nullable Long size) {
    this.oid = oid;
    this.size = size;
  }

  @Nullable
  public String getOid() {
    return oid;
  }

  @Nullable
  public Long getSize() {
    return size;
  }
}
