package ru.bozaro.gitlfs.pointer;

import javax.annotation.Nonnull;

/**
 * Git-lfs constants.
 *
 * @author Artem V. Navrotskiy
 */
public class Constants {
  public static final int POINTER_MAX_SIZE = 1024;
  @Nonnull
  public static final String VERSION_URL = "https://git-lfs.github.com/spec/v1";
  @Nonnull
  public static final String OID = "oid";
  @Nonnull
  public static final String SIZE = "size";
  @Nonnull
  public static final String VERSION = "version";
}
