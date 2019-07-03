package ru.bozaro.gitlfs.server;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.common.Constants;
import ru.bozaro.gitlfs.common.data.Error;
import ru.bozaro.gitlfs.common.data.*;

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
  @NotNull
  private final ContentManager manager;
  @NotNull
  private final String contentLocation;

  /**
   * Create pointer manager for local ContentManager.
   *
   * @param manager         Content manager.
   * @param contentLocation Absolute or relative URL to ContentServlet.
   */
  public LocalPointerManager(@NotNull ContentManager manager, @NotNull String contentLocation) {
    this.manager = manager;
    this.contentLocation = contentLocation.endsWith("/") ? contentLocation : contentLocation + "/";
  }

  @NotNull
  @Override
  public Locator checkUploadAccess(@NotNull HttpServletRequest request, @NotNull URI selfUrl) throws IOException, ForbiddenError, UnauthorizedError {
    final ContentManager.HeaderProvider headerProvider = manager.checkUploadAccess(request);
    return createLocator(request, headerProvider, selfUrl);
  }

  @NotNull
  @Override
  public Locator checkDownloadAccess(@NotNull HttpServletRequest request, @NotNull URI selfUrl) throws IOException, ForbiddenError, UnauthorizedError {
    final ContentManager.HeaderProvider headerProvider = manager.checkDownloadAccess(request);
    return createLocator(request, headerProvider, selfUrl);
  }

  protected Locator createLocator(@NotNull HttpServletRequest request, @NotNull ContentManager.HeaderProvider headerProvider, @NotNull final URI selfUrl) {
    final Map<String, String> header = headerProvider.createHeader(createDefaultHeader(request));
    return new Locator() {
      @NotNull
      @Override
      public BatchItem[] getLocations(@NotNull Meta[] metas) throws IOException {
        final BatchItem[] result = new BatchItem[metas.length];
        for (int i = 0; i < metas.length; ++i) {
          result[i] = getLocation(header, selfUrl, metas[i]);
        }
        return result;
      }

      @NotNull
      public BatchItem getLocation(@Nullable Map<String, String> header, @NotNull URI selfUrl, @NotNull Meta meta) throws IOException {
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

  @NotNull
  protected Map<String, String> createDefaultHeader(@NotNull HttpServletRequest request) {
    final String auth = request.getHeader(Constants.HEADER_AUTHORIZATION);
    final Map<String, String> header = new HashMap<>();
    if (auth != null) {
      header.put(Constants.HEADER_AUTHORIZATION, auth);
    }
    return header;
  }
}
