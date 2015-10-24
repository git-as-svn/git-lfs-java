package ru.bozaro.gitlfs.pointer;

import org.jetbrains.annotations.NotNull;

/**
 * Git-lfs constants.
 *
 * @author Artem V. Navrotskiy
 */
public class Constants {
  public static final int POINTER_MAX_SIZE = 1024;
  @NotNull
  public static final String VERSION_URL = "https://git-lfs.github.com/spec/v1";
  @NotNull
  public static final String OID = "oid";
  @NotNull
  public static final String SIZE = "size";
  @NotNull
  public static final String VERSION = "version";
}
