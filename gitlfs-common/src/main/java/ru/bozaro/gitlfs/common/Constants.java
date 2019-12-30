package ru.bozaro.gitlfs.common;

import javax.annotation.Nonnull;

/**
 * Git-lfs constants.
 *
 * @author Artem V. Navrotskiy
 */
public final class Constants {
  /**
   * See {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1">HTTP/1.1 documentation</a>}.
   */
  @Nonnull
  public static final String HEADER_AUTHORIZATION = "Authorization";
  @Nonnull
  public static final String HEADER_ACCEPT = "Accept";
  @Nonnull
  public static final String HEADER_LOCATION = "Location";

  @Nonnull
  public static final String MIME_LFS_JSON = "application/vnd.git-lfs+json";
  @Nonnull
  public static final String MIME_BINARY = "application/octet-stream";
  @Nonnull
  public static final String PATH_OBJECTS = "objects";
  @Nonnull
  public static final String PATH_BATCH = "objects/batch";
  @Nonnull
  public static final String PATH_LOCKS = "locks";

  /**
   * Minimal supported batch size.
   */
  public static final int BATCH_SIZE = 100;
}
