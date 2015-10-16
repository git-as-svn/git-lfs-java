package ru.bozaro.gitlfs.server;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.common.data.Meta;
import ru.bozaro.gitlfs.common.data.Operation;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for store object content.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public interface ContentManager<T> {
  /**
   * Check access for requested operation and return some user information.
   *
   * @param request   HTTP request.
   * @param operation Requested operation.
   * @return Return some user information.
   */
  T checkAccess(@NotNull HttpServletRequest request, @NotNull Operation operation) throws IOException, ForbiddenError, UnauthorizedError;

  /**
   * Get metadata of uploaded object.
   *
   * @param hash Object metadata (hash and size).
   * @return Return metadata of uploaded object.
   */
  @Nullable
  Meta getMetadata(@NotNull String hash) throws IOException;

  /**
   * Get object from storage.
   *
   * @param context Some user information.
   * @param hash    Object metadata (hash and size).
   * @return Return object stream.
   */
  @NotNull
  InputStream openObject(T context, @NotNull String hash) throws IOException;

  /**
   * Get gzip-compressed object from storage.
   *
   * @param context Some user information.
   * @param hash    Object metadata (hash and size).
   * @return Return gzip-compressed object stream. If gzip-stream is not available return null.
   */
  @Nullable
  InputStream openObjectGzipped(T context, @NotNull String hash) throws IOException;

  /**
   * Save object to storage.
   *
   * @param context Some user information.
   * @param meta    Object metadata (hash and size).
   * @param content Stream with object data.
   */
  void saveObject(T context, @NotNull Meta meta, @NotNull InputStream content) throws IOException;
}
