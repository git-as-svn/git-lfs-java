package ru.bozaro.gitlfs.server;

import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.common.data.BatchItem;
import ru.bozaro.gitlfs.common.data.Meta;
import ru.bozaro.gitlfs.common.data.Operation;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;

/**
 * Interface for lookup pointer information.
 *
 * @param <T> Some authentication information.
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public interface PointerManager<T> {
  /**
   * Check access for requested operation and return some user information.
   *
   * @param request   HTTP request.
   * @param operation Requested operation.
   * @return Return some user information.
   */
  T checkAccess(@NotNull HttpServletRequest request, @NotNull Operation operation) throws IOException, ForbiddenError, UnauthorizedError;

  /**
   * @param context   Some user information.
   * @param selfUrl   Http URL for this request.
   * @param operation Requested operation.
   * @param metas     Object hash array (note: metadata can have negative size for GET object request).
   * @return Return batch items with same order and same count as metas array.
   * @throws IOException
   */
  @NotNull
  BatchItem[] getLocations(T context, @NotNull URI selfUrl, @NotNull Operation operation, @NotNull Meta[] metas) throws IOException;
}
