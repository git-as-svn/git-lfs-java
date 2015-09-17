package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * LFS reference.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class Link {
  @JsonProperty(value = "href", required = true)
  @Nullable
  private URI href;
  @JsonProperty("header")
  @Nullable
  private Map<String, String> header = new TreeMap<>();

  protected Link() {
  }

  public Link(@Nullable URI href, @Nullable Map<String, String> header) {
    this.href = href;
    this.header = header;
  }

  @Nullable
  public URI getHref() {
    return href;
  }

  @NotNull
  public Map<String, String> getHeader() {
    return header == null ? Collections.<String, String>emptyMap() : header;
  }
}
