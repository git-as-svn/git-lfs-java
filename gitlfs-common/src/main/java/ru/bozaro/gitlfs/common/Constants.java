package ru.bozaro.gitlfs.common;

import org.jetbrains.annotations.NotNull;

/**
 * Git-lfs constants.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class Constants {
  @NotNull
  public static final String MIME_LFS_JSON = "application/vnd.git-lfs+json";
  @NotNull
  public static final String MIME_BINARY = "application/octet-stream";
  @NotNull
  public static final String PATH_OBJECTS = "objects";
  @NotNull
  public static final String PATH_BATCH = "objects/batch";
}
