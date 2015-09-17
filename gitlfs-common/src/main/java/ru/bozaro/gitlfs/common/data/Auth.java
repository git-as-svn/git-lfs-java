package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.Date;
import java.util.Map;

/**
 * Auth structure.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public final class Auth extends Link {
  @JsonProperty("expires_at")
  @Nullable
  private Date expiresAt;

  protected Auth() {
  }

  public Auth(@NotNull URI href, @Nullable Map<String, String> header, @Nullable Date expiresAt) {
    super(href, header);
    this.expiresAt = expiresAt;
  }

  @Nullable
  public Date getExpiresAt() {
    return expiresAt;
  }
}
