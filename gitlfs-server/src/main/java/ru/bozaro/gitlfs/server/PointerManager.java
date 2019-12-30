package ru.bozaro.gitlfs.server;

import ru.bozaro.gitlfs.common.data.BatchItem;
import ru.bozaro.gitlfs.common.data.Meta;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;

/**
 * Interface for lookup pointer information.
 *
 * @author Artem V. Navrotskiy
 */
public interface PointerManager {
  /**
   * Check access for upload objects.
   *
   * @param request HTTP request.
   * @param selfUrl Http URL for this request.
   * @return Location provider.
   */
  @Nonnull
  Locator checkUploadAccess(@Nonnull HttpServletRequest request, @Nonnull URI selfUrl) throws IOException, ForbiddenError, UnauthorizedError;

  /**
   * Check access for download objects.
   *
   * @param request HTTP request.
   * @param selfUrl Http URL for this request.
   * @return Location provider.
   */
  @Nonnull
  Locator checkDownloadAccess(@Nonnull HttpServletRequest request, @Nonnull URI selfUrl) throws IOException, ForbiddenError, UnauthorizedError;

  interface Locator {
    /**
     * @param metas Object hash array (note: metadata can have negative size for GET object request).
     * @return Return batch items with same order and same count as metas array.
     */
    @Nonnull
    BatchItem[] getLocations(@Nonnull Meta[] metas) throws IOException;
  }
}
