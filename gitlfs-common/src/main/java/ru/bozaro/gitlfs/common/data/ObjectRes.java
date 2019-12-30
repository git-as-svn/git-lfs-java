package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
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
  @Nonnull
  private final Map<LinkType, Link> links;
  @CheckForNull
  private final Meta meta;

  @JsonCreator
  public ObjectRes(
      @JsonProperty(value = "oid") @CheckForNull String oid,
      @JsonProperty(value = "size") long size,
      @JsonProperty(value = "_links", required = true) @Nonnull Map<LinkType, Link> links
  ) {
    this.meta = oid == null ? null : new Meta(oid, size);
    this.links = Collections.unmodifiableMap(new TreeMap<>(links));
  }

  @CheckForNull
  public Meta getMeta() {
    return meta;
  }

  @Override
  @Nonnull
  public Map<LinkType, Link> getLinks() {
    return links;
  }
}
