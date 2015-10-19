package ru.bozaro.gitlfs.common;

import org.jetbrains.annotations.NotNull;

/**
 * Git-lfs constants.
 *
 * @author Artem V. Navrotskiy
 */
public class Constants {
  /**
   * See {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1">HTTP/1.1 documentation</a>}.
   */
  @NotNull
  public static final String HEADER_AUTHORIZATION = "Authorization";
  @NotNull
  public static final String HEADER_ACCEPT = "Accept";
  @NotNull
  public static final String HEADER_LOCATION = "Location";

  @NotNull
  public static final String MIME_LFS_JSON = "application/vnd.git-lfs+json";
  @NotNull
  public static final String MIME_BINARY = "application/octet-stream";
  @NotNull
  public static final String PATH_OBJECTS = "objects";
  @NotNull
  public static final String PATH_BATCH = "objects/batch";
}
