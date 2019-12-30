package ru.bozaro.gitlfs.client.internal;

import ru.bozaro.gitlfs.common.data.Link;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Work with same auth data.
 *
 * @author Artem V. Navrotskiy
 */
public interface Work<R> {
  R exec(@Nonnull Link auth) throws IOException;
}
