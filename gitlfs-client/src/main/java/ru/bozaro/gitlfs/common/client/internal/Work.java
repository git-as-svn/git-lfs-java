package ru.bozaro.gitlfs.common.client.internal;

import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.common.data.Auth;

import java.io.IOException;

/**
 * Work with same auth data.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public interface Work<R> {
  R exec(@NotNull Auth auth) throws IOException;
}
