package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * LFS object location.
 *
 * @author Artem V. Navrotskiy
 */
public final class ObjectRes implements Links {
  @JsonProperty(value = "_links", required = true)
  @NotNull
  private final Map<LinkType, Link> links;
  @Nullable
  private final Meta meta;

  @JsonCreator
  public ObjectRes(
      @JsonProperty(value = "oid") @Nullable String oid,
      @JsonProperty(value = "size") long size,
      @JsonProperty(value = "_links", required = true) @NotNull Map<LinkType, Link> links
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
  public Map<LinkType, Link> getLinks() {
    return links;
  }
}
