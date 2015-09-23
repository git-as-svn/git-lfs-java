package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;

/**
 * LFS object location.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public final class ObjectRes extends Meta implements Links {
  @JsonProperty(value = "_links", required = true)
  @NotNull
  private Map<String, Link> links = new TreeMap<>();

  protected ObjectRes() {
  }

  public ObjectRes(@Nullable String oid, @Nullable Long size, @NotNull Map<String, Link> links) {
    super(oid, size);
    this.links = links;
  }

  @Override
  @NotNull
  public Map<String, Link> getLinks() {
    return links;
  }
}
