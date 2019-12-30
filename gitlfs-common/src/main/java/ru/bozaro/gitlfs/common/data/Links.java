package ru.bozaro.gitlfs.common.data;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Object locations.
 *
 * @author Artem V. Navrotskiy
 */
public interface Links {
  @Nonnull
  Map<LinkType, Link> getLinks();
}
