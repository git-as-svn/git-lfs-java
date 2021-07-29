package ru.bozaro.gitlfs.server;

import jakarta.servlet.http.HttpServletRequest;
import ru.bozaro.gitlfs.common.data.Meta;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Interface for store object content.
 *
 * @author Artem V. Navrotskiy
 */
public interface ContentManager {
  /**
   * Check access for requested operation and return some user information.
   *
   * @param request HTTP request.
   * @return Object for send object.
   */
  @Nonnull
  Downloader checkDownloadAccess(@Nonnull HttpServletRequest request) throws IOException, ForbiddenError, UnauthorizedError;

  /**
   * Check access for requested operation and return some user information.
   *
   * @param request HTTP request.
   * @return Object for receive object.
   */
  @Nonnull
  Uploader checkUploadAccess(@Nonnull HttpServletRequest request) throws IOException, ForbiddenError, UnauthorizedError;

  /**
   * Get metadata of uploaded object.
   *
   * @param hash Object metadata (hash and size).
   * @return Return metadata of uploaded object.
   */
  @CheckForNull
  Meta getMetadata(@Nonnull String hash) throws IOException;

  interface HeaderProvider {
    /**
     * Generate pointer header information (for example: replace transit Basic auth by Toker auth).
     *
     * @param header Default header. Can be modified.
     * @return Pointer header information.
     */
    @Nonnull
    default Map<String, String> createHeader(@Nonnull Map<String, String> header) {
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
    @Nonnull
    InputStream openObject(@Nonnull String hash) throws IOException;

    /**
     * Get gzip-compressed object from storage.
     *
     * @param hash Object metadata (hash and size).
     * @return Return gzip-compressed object stream. If gzip-stream is not available return null.
     */
    @CheckForNull
    InputStream openObjectGzipped(@Nonnull String hash) throws IOException;
  }

  interface Uploader extends HeaderProvider {

    /**
     * Save object to storage.
     *
     * @param meta    Object metadata (hash and size).
     * @param content Stream with object data.
     */
    void saveObject(@Nonnull Meta meta, @Nonnull InputStream content) throws IOException;
  }
}
