package ru.bozaro.gitlfs.server;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Header provider for authorization data.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public interface HeaderProvider {
  @Nullable
  Map<String, String> createHeader();
}
