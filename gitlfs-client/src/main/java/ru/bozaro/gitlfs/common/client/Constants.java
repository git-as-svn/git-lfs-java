package ru.bozaro.gitlfs.common.client;

import org.jetbrains.annotations.NotNull;

/**
 * Git-lfs constants.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class Constants {
  /**
   * See {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1">HTTP/1.1 documentation</a>}.
   */
  @NotNull
  public static final String HEADER_ACCEPT = "Accept";
  @NotNull
  public static final String HEADER_LOCATION = "Location";
  @NotNull
  public static final String MIME_TYPE = "application/vnd.git-lfs+json";
  @NotNull
  public static final String OBJECTS = "/objects";
}
