package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * LFS object location.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public final class Meta {
  @JsonProperty(value = "oid", required = true)
  @Nullable
  private String oid = "";

  @JsonProperty(value = "size", required = true)
  @Nullable
  private Long size = 0L;

  @JsonProperty(value = "_links", required = true)
  @Nullable
  private Map<String, Link> links = new TreeMap<>();

  protected Meta() {
  }

  public Meta(@Nullable String oid, @Nullable Long size, @Nullable Map<String, Link> links) {
    this.oid = oid;
    this.size = size;
    this.links = links;
  }

  @Nullable
  public String getOid() {
    return oid;
  }

  @Nullable
  public Long getSize() {
    return size;
  }

  @NotNull
  public Map<String, Link> getLinks() {
    return links == null ? Collections.<String, Link>emptyMap() : links;
  }
}
