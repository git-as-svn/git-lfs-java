package ru.bozaro.gitlfs.common.data;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Object locations.
 *
 * @author Artem V. Navrotskiy
 */
public interface Links {
  @NotNull
  Map<LinkType, Link> getLinks();
}
