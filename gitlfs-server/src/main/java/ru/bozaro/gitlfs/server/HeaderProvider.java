package ru.bozaro.gitlfs.server;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Header provider for authorization data.
 *
 * @author Artem V. Navrotskiy
 */
public interface HeaderProvider {
  @Nullable
  Map<String, String> createHeader();
}
