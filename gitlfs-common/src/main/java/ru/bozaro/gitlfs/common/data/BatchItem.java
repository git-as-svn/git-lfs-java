package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
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
  @Nonnull
  private final Map<LinkType, Link> links;
  @JsonProperty(value = "error")
  @CheckForNull
  private final Error error;

  public BatchItem(@Nonnull Meta meta, @Nonnull Map<LinkType, Link> links) {
    this(meta.getOid(), meta.getSize(), links, null, null);
  }

  public BatchItem(
      @JsonProperty(value = "oid", required = true) @Nonnull String oid,
      @JsonProperty(value = "size", required = true) long size,
      @JsonProperty(value = "actions") @CheckForNull Map<LinkType, Link> links1,
      @JsonProperty(value = "_links") @CheckForNull Map<LinkType, Link> links2,
      @JsonProperty(value = "error") @CheckForNull Error error
  ) {
    super(oid, size);
    this.links = combine(links1, links2);
    this.error = error;
  }

  @Nonnull
  private static <K, V> Map<K, V> combine(@CheckForNull Map<K, V> a, @CheckForNull Map<K, V> b) {
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

  public BatchItem(@Nonnull Meta meta, @Nonnull Error error) {
    this(meta.getOid(), meta.getSize(), null, null, error);
  }

  @Override
  @Nonnull
  public Map<LinkType, Link> getLinks() {
    return links;
  }

  @CheckForNull
  public Error getError() {
    return error;
  }
}
