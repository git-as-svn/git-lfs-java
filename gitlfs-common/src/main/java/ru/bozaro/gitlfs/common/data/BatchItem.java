package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * LFS batch object.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class BatchItem extends Meta implements Links {
  @JsonProperty(value = "actions")
  @NotNull
  private final Map<LinkType, Link> links;
  @JsonProperty(value = "error")
  @Nullable
  private final Error error;

  public BatchItem(@NotNull Meta meta, @NotNull Map<LinkType, Link> links) {
    this(meta.getOid(), meta.getSize(), links, null);
  }

  public BatchItem(@NotNull Meta meta, @NotNull Error error) {
    this(meta.getOid(), meta.getSize(), null, error);
  }

  public BatchItem(
      @JsonProperty(value = "oid", required = true)
      @NotNull
      String oid,
      @JsonProperty(value = "size", required = true)
      long size,
      @JsonProperty(value = "actions")
      @Nullable
      Map<LinkType, Link> links,
      @JsonProperty(value = "error")
      @Nullable
      Error error
  ) {
    super(oid, size);
    this.links = links == null ? Collections.<LinkType, Link>emptyMap() : Collections.unmodifiableMap(new TreeMap<>(links));
    this.error = error;
  }

  @Override
  @NotNull
  public Map<LinkType, Link> getLinks() {
    return links;
  }

  @Nullable
  public Error getError() {
    return error;
  }
}
