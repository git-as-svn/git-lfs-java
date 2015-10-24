package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class Link {
  @JsonProperty(value = "href", required = true)
  @NotNull
  private final URI href;
  @JsonProperty("header")
  @NotNull
  private final Map<String, String> header;
  @JsonProperty("expires_at")
  @Nullable
  private final Date expiresAt;

  @JsonCreator
  public Link(
      @JsonProperty(value = "href", required = true)
      @NotNull
      URI href,
      @JsonProperty("header")
      @Nullable
      Map<String, String> header,
      @JsonProperty("expires_at")
      @Nullable
      Date expiresAt
  ) {
    this.href = href;
    this.header = header == null ? Collections.<String, String>emptyMap() : new TreeMap<>(header);
    this.expiresAt = expiresAt != null ? new Date(expiresAt.getTime()) : null;
  }

  @NotNull
  public URI getHref() {
    return href;
  }

  @NotNull
  public Map<String, String> getHeader() {
    return header;
  }

  @Nullable
  public Date getExpiresAt() {
    return expiresAt != null ? new Date(expiresAt.getTime()) : null;
  }

}
