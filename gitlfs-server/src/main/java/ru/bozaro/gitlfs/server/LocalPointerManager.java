package ru.bozaro.gitlfs.server;

import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.common.data.*;
import ru.bozaro.gitlfs.common.data.Error;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.TreeMap;

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
  public BatchItem[] getLocations(T context, @NotNull HttpServletRequest req, @NotNull Operation operation, @NotNull Meta[] metas) throws IOException {
    final BatchItem[] result = new BatchItem[metas.length];
    for (int i = 0; i < metas.length; ++i) {
      result[i] = getLocation(context, req, operation, metas[i]);
    }
    return result;
  }

  @NotNull
  public BatchItem getLocation(T context, @NotNull HttpServletRequest req, @NotNull Operation operation, @NotNull Meta meta) throws IOException {
    final TreeMap<LinkType, Link> links = new TreeMap<>();
    links.put(LinkType.Self, new Link(getSelfRef(req), null, null));
    final Meta storageMeta = manager.getMetadata(meta.getOid());
    if (storageMeta == null) {
      links.put(LinkType.Upload, createLink(context, req, meta));
    } else if ((meta.getSize() >= 0) && (storageMeta.getSize() != meta.getSize())) {
      return new BatchItem(meta, new Error(422, "Invalid object size"));
    } else {
      links.put(LinkType.Download, createLink(context, req, storageMeta));
    }
    return new BatchItem(meta, links);
  }

  public Link createLink(T context, @NotNull HttpServletRequest req, @NotNull Meta meta) {
    final URI baseUri = getSelfRef(req).resolve(contentLocation);
    return new Link(baseUri.resolve(meta.getOid()), null, null);
  }

  @NotNull
  public URI getSelfRef(@NotNull HttpServletRequest req) {
    try {
      return new URI(req.getScheme(), null, req.getServerName(), req.getServerPort(), req.getServletPath(), null, null);
    } catch (URISyntaxException e) {
      throw new IllegalStateException("Can't create request URL", e);
    }
  }
}
