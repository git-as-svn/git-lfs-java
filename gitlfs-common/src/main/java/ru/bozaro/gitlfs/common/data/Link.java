package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * LFS reference.
 *
 * @author Artem V. Navrotskiy
 */
public class Link {
  @JsonProperty(value = "href", required = true)
  @Nonnull
  private final URI href;
  @JsonProperty("header")
  @Nonnull
  private final Map<String, String> header;
  @JsonProperty("expires_at")
  @CheckForNull
  private final Date expiresAt;

  @JsonCreator
  public Link(
      @JsonProperty(value = "href", required = true) @Nonnull URI href,
      @JsonProperty("header") @CheckForNull Map<String, String> header,
      @JsonProperty("expires_at") @CheckForNull Date expiresAt
  ) {
    this.href = href;
    this.header = header == null ? Collections.emptyMap() : new TreeMap<>(header);
    this.expiresAt = expiresAt != null ? new Date(expiresAt.getTime()) : null;
  }

  @Nonnull
  public URI getHref() {
    return href;
  }

  @Nonnull
  public Map<String, String> getHeader() {
    return header;
  }

  @CheckForNull
  public Date getExpiresAt() {
    return expiresAt != null ? new Date(expiresAt.getTime()) : null;
  }

}
