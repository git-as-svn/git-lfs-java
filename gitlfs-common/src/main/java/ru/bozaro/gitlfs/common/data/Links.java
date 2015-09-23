package ru.bozaro.gitlfs.common.data;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Object locations.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public interface Links {
  @NotNull
  Map<String, Link> getLinks();
}
