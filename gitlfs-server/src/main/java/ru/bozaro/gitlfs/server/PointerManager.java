package ru.bozaro.gitlfs.server;

import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.common.data.BatchItem;
import ru.bozaro.gitlfs.common.data.Meta;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;

/**
 * Interface for lookup pointer information.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public interface PointerManager {
  interface Locator {
    /**
     * @param metas Object hash array (note: metadata can have negative size for GET object request).
     * @return Return batch items with same order and same count as metas array.
     * @throws IOException
     */
    @NotNull
    BatchItem[] getLocations(@NotNull Meta[] metas) throws IOException;
  }

  /**
   * Check access for upload objects.
   *
   * @param request HTTP request.
   * @param selfUrl Http URL for this request.
   * @return Location provider.
   */
  @NotNull
  Locator checkUploadAccess(@NotNull HttpServletRequest request, @NotNull URI selfUrl) throws IOException, ForbiddenError, UnauthorizedError;

  /**
   * Check access for download objects.
   *
   * @param request HTTP request.
   * @param selfUrl Http URL for this request.
   * @return Location provider.
   */
  @NotNull
  Locator checkDownloadAccess(@NotNull HttpServletRequest request, @NotNull URI selfUrl) throws IOException, ForbiddenError, UnauthorizedError;
}
