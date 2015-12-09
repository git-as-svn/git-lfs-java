package ru.bozaro.gitlfs.server;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.common.data.Meta;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Interface for store object content.
 *
 * @author Artem V. Navrotskiy
 */
public interface ContentManager {
  interface HeaderProvider {
    /**
     * Generate pointer header information (for example: replace transit Basic auth by Toker auth).
     *
     * @param header Default header. Can be modified.
     * @return Pointer header information.
     */
    @NotNull
    default Map<String, String> createHeader(@NotNull Map<String, String> header) {
      return header;
    }
  }

  interface Downloader extends HeaderProvider {
    /**
     * Get object from storage.
     *
     * @param hash Object metadata (hash and size).
     * @return Return object stream.
     */
    @NotNull
    InputStream openObject(@NotNull String hash) throws IOException;

    /**
     * Get gzip-compressed object from storage.
     *
     * @param hash Object metadata (hash and size).
     * @return Return gzip-compressed object stream. If gzip-stream is not available return null.
     */
    @Nullable
    InputStream openObjectGzipped(@NotNull String hash) throws IOException;
  }

  interface Uploader extends HeaderProvider {

    /**
     * Save object to storage.
     *
     * @param meta    Object metadata (hash and size).
     * @param content Stream with object data.
     */
    void saveObject(@NotNull Meta meta, @NotNull InputStream content) throws IOException;
  }

  /**
   * Check access for requested operation and return some user information.
   *
   * @param request HTTP request.
   * @return Object for send object.
   */
  @NotNull
  Downloader checkDownloadAccess(@NotNull HttpServletRequest request) throws IOException, ForbiddenError, UnauthorizedError;

  /**
   * Check access for requested operation and return some user information.
   *
   * @param request HTTP request.
   * @return Object for receive object.
   */
  @NotNull
  Uploader checkUploadAccess(@NotNull HttpServletRequest request) throws IOException, ForbiddenError, UnauthorizedError;

  /**
   * Get metadata of uploaded object.
   *
   * @param hash Object metadata (hash and size).
   * @return Return metadata of uploaded object.
   */
  @Nullable
  Meta getMetadata(@NotNull String hash) throws IOException;
}
