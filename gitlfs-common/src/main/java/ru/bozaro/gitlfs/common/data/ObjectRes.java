package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public final class ObjectRes implements Links {
  @JsonProperty(value = "_links", required = true)
  @NotNull
  private final Map<String, Link> links;
  @Nullable
  private final Meta meta;

  @JsonCreator
  public ObjectRes(
      @JsonProperty(value = "oid", required = false)
      @Nullable
      String oid,
      @JsonProperty(value = "size", required = false)
      long size,
      @JsonProperty(value = "_links", required = true)
      @NotNull
      Map<String, Link> links
  ) {
    this.meta = oid == null ? null : new Meta(oid, size);
    this.links = Collections.unmodifiableMap(new TreeMap<>(links));
  }

  @Nullable
  public Meta getMeta() {
    return meta;
  }

  @Override
  @NotNull
  public Map<String, Link> getLinks() {
    return links;
  }
}
