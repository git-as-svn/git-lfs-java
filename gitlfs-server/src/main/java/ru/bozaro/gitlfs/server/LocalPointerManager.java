package ru.bozaro.gitlfs.server;

import ru.bozaro.gitlfs.common.Constants;
import ru.bozaro.gitlfs.common.data.Error;
import ru.bozaro.gitlfs.common.data.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Pointer manager for local ContentManager.
 *
 * @author Artem V. Navrotskiy
 */
public class LocalPointerManager implements PointerManager {
  @Nonnull
  private final ContentManager manager;
  @Nonnull
  private final String contentLocation;

  /**
   * Create pointer manager for local ContentManager.
   *
   * @param manager         Content manager.
   * @param contentLocation Absolute or relative URL to ContentServlet.
   */
  public LocalPointerManager(@Nonnull ContentManager manager, @Nonnull String contentLocation) {
    this.manager = manager;
    this.contentLocation = contentLocation.endsWith("/") ? contentLocation : contentLocation + "/";
  }

  @Nonnull
  @Override
  public Locator checkUploadAccess(@Nonnull HttpServletRequest request, @Nonnull URI selfUrl) throws IOException, ForbiddenError, UnauthorizedError {
    final ContentManager.HeaderProvider headerProvider = manager.checkUploadAccess(request);
    return createLocator(request, headerProvider, selfUrl);
  }

  @Nonnull
  @Override
  public Locator checkDownloadAccess(@Nonnull HttpServletRequest request, @Nonnull URI selfUrl) throws IOException, ForbiddenError, UnauthorizedError {
    final ContentManager.HeaderProvider headerProvider = manager.checkDownloadAccess(request);
    return createLocator(request, headerProvider, selfUrl);
  }

  protected Locator createLocator(@Nonnull HttpServletRequest request, @Nonnull ContentManager.HeaderProvider headerProvider, @Nonnull final URI selfUrl) {
    final Map<String, String> header = headerProvider.createHeader(createDefaultHeader(request));
    return new Locator() {
      @Nonnull
      @Override
      public BatchItem[] getLocations(@Nonnull Meta[] metas) throws IOException {
        final BatchItem[] result = new BatchItem[metas.length];
        for (int i = 0; i < metas.length; ++i) {
          result[i] = getLocation(header, selfUrl, metas[i]);
        }
        return result;
      }

      @Nonnull
      public BatchItem getLocation(@CheckForNull Map<String, String> header, @Nonnull URI selfUrl, @Nonnull Meta meta) throws IOException {
        final Meta storageMeta = manager.getMetadata(meta.getOid());

        if (storageMeta != null && meta.getSize() >= 0 && storageMeta.getSize() != meta.getSize())
          return new BatchItem(meta, new Error(422, "Invalid object size"));

        final Map<LinkType, Link> links = new EnumMap<>(LinkType.class);
        final Link link = new Link(selfUrl.resolve(contentLocation).resolve(meta.getOid()), header, null);

        if (storageMeta == null)
          links.put(LinkType.Upload, link);
        else
          links.put(LinkType.Download, link);

        links.put(LinkType.Verify, link);

        return new BatchItem(storageMeta == null ? meta : storageMeta, links);
      }
    };
  }

  @Nonnull
  protected Map<String, String> createDefaultHeader(@Nonnull HttpServletRequest request) {
    final String auth = request.getHeader(Constants.HEADER_AUTHORIZATION);
    final Map<String, String> header = new HashMap<>();
    if (auth != null) {
      header.put(Constants.HEADER_AUTHORIZATION, auth);
    }
    return header;
  }
}
