package ru.bozaro.gitlfs.server;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.common.data.*;
import ru.bozaro.gitlfs.common.data.Error;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;

/**
 * Pointer manager for local ContentManager.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class LocalPointerManager<T> implements PointerManager<T> {
  @NotNull
  private final ContentManager<T> manager;
  @NotNull
  private final String contentLocation;

  /**
   * Create pointer manager for local ContentManager.
   *
   * @param manager         Content manager.
   * @param contentLocation Absolute or relative URL to ContentServlet.
   */
  public LocalPointerManager(@NotNull ContentManager<T> manager, @NotNull String contentLocation) {
    this.manager = manager;
    this.contentLocation = contentLocation.endsWith("/") ? contentLocation : contentLocation + "/";
  }

  @Override
  public T checkAccess(@NotNull HttpServletRequest request, @NotNull Operation operation) throws IOException, ForbiddenError, UnauthorizedError {
    return manager.checkAccess(request, operation);
  }

  @NotNull
  @Override
  public BatchItem[] getLocations(T context, @NotNull URI selfUrl, @NotNull Operation operation, @NotNull Meta[] metas) throws IOException {
    final BatchItem[] result = new BatchItem[metas.length];
    for (int i = 0; i < metas.length; ++i) {
      result[i] = getLocation(context, selfUrl, operation, metas[i]);
    }
    return result;
  }

  @NotNull
  public BatchItem getLocation(T context, @NotNull URI selfUrl, @NotNull Operation operation, @NotNull Meta meta) throws IOException {
    final Meta storageMeta = manager.getMetadata(meta.getOid());
    if (storageMeta == null) {
      return new BatchItem(meta, ImmutableMap.of(LinkType.Upload, createLink(context, selfUrl, meta)));
    } else if ((meta.getSize() >= 0) && (storageMeta.getSize() != meta.getSize())) {
      return new BatchItem(meta, new Error(422, "Invalid object size"));
    } else {
      return new BatchItem(storageMeta, ImmutableMap.of(LinkType.Download, createLink(context, selfUrl, storageMeta)));
    }
  }

  public Link createLink(T context, @NotNull URI selfUrl, @NotNull Meta meta) {
    return new Link(selfUrl.resolve(contentLocation).resolve(meta.getOid()), null, null);
  }
}
