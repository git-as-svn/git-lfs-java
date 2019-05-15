package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * LFS batch object.
 *
 * @author Artem V. Navrotskiy
 */
public final class BatchItem extends Meta implements Links {
  @JsonProperty(value = "actions")
  @NotNull
  private final Map<LinkType, Link> links;
  @JsonProperty(value = "error")
  @Nullable
  private final Error error;

  public BatchItem(@NotNull Meta meta, @NotNull Map<LinkType, Link> links) {
    this(meta.getOid(), meta.getSize(), links, null, null);
  }

  public BatchItem(
      @JsonProperty(value = "oid", required = true) @NotNull String oid,
      @JsonProperty(value = "size", required = true) long size,
      @JsonProperty(value = "actions") @Nullable Map<LinkType, Link> links1,
      @JsonProperty(value = "_links") @Nullable Map<LinkType, Link> links2,
      @JsonProperty(value = "error") @Nullable Error error
  ) {
    super(oid, size);
    this.links = combine(links1, links2);
    this.error = error;
  }

  @NotNull
  private static <K, V> Map<K, V> combine(@Nullable Map<K, V> a, @Nullable Map<K, V> b) {
    Map<K, V> r = null;
    if (a != null && !a.isEmpty()) {
      r = a;
    }
    if (b != null && !b.isEmpty()) {
      if (r == null) {
        r = b;
      } else {
        r = new TreeMap<>(r);
        r.putAll(b);
      }
    }
    return r == null ? Collections.emptyMap() : Collections.unmodifiableMap(r);
  }

  public BatchItem(@NotNull Meta meta, @NotNull Error error) {
    this(meta.getOid(), meta.getSize(), null, null, error);
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
