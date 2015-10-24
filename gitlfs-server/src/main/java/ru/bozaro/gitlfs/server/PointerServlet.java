package ru.bozaro.gitlfs.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.common.Constants;
import ru.bozaro.gitlfs.common.JsonHelper;
import ru.bozaro.gitlfs.common.data.*;
import ru.bozaro.gitlfs.common.data.Error;
import ru.bozaro.gitlfs.server.internal.ObjectResponse;
import ru.bozaro.gitlfs.server.internal.ResponseWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Servlet for pointer storage.
 * <p>
 * This servlet is entry point for git-lfs client.
 * <p>
 * Need to be mapped by path: objects/
 * <p>
 * Supported URL paths:
 * <p>
 * * POST: /
 * * POST: /batch
 * * GET:  /:oid
 *
 * @author Artem V. Navrotskiy
 */
public class PointerServlet extends HttpServlet {
  @NotNull
  private static final Pattern PATTERN_OID = Pattern.compile("^/[0-9a-f]{64}$");
  @NotNull
  private final ObjectMapper mapper;
  @NotNull
  private final PointerManager manager;
  @NotNull
  private final AccessCheckerVisitor accessCheckerVisitor;

  /**
   * Create pointer manager for local ContentManager.
   *
   * @param manager         Content manager.
   * @param contentLocation Absolute or relative URL to ContentServlet.
   */
  public PointerServlet(@NotNull ContentManager manager, @NotNull String contentLocation) {
    this(new LocalPointerManager(manager, contentLocation));
  }

  public PointerServlet(@NotNull PointerManager manager) {
    this.manager = manager;
    this.mapper = JsonHelper.createMapper();
    this.accessCheckerVisitor = new AccessCheckerVisitor(manager);
  }

  @NotNull
  private static BatchItem[] filterLocations(@NotNull BatchItem[] items, @NotNull LocationFilter filter) throws IOException {
    final BatchItem[] result = new BatchItem[items.length];
    for (int i = 0; i < items.length; ++i) {
      if (items[i].getError() == null) {
        result[i] = filter.filter(items[i]);
      } else {
        result[i] = items[i];
      }
    }
    return result;
  }

  @NotNull
  private static BatchItem filterDownload(@NotNull BatchItem item) {
    if (item.getLinks().containsKey(LinkType.Download)) {
      return new BatchItem(item.getOid(), item.getSize(), filterLocation(item.getLinks(), LinkType.Download), null);
    }
    return new BatchItem(item.getOid(), item.getSize(), null, new Error(HttpServletResponse.SC_NOT_FOUND, "Object not found"));
  }

  private static Map<LinkType, Link> filterLocation(@NotNull Map<LinkType, Link> links, @NotNull LinkType... linkTypes) {
    final Map<LinkType, Link> result = new TreeMap<>();
    for (LinkType linkType : linkTypes) {
      final Link link = links.get(linkType);
      if (link != null) result.put(linkType, link);
    }
    return result;
  }

  @NotNull
  private static BatchItem filterUpload(@NotNull BatchItem item) throws IOException {
    if (item.getLinks().containsKey(LinkType.Download)) {
      return new BatchItem(item.getOid(), item.getSize(), filterLocation(item.getLinks(), LinkType.Download), null);
    }
    if (item.getLinks().containsKey(LinkType.Upload)) {
      return new BatchItem(item.getOid(), item.getSize(), filterLocation(item.getLinks(), LinkType.Upload, LinkType.Verify), null);
    }
    throw new IOException("Upload link not found");
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      if ((req.getPathInfo() != null) && PATTERN_OID.matcher(req.getPathInfo()).matches()) {
        processObjectGet(req, req.getPathInfo().substring(1)).write(resp);
        return;
      }
    } catch (ServerError e) {
      resp.setStatus(e.getStatusCode());
      resp.getWriter().println(e.getMessage());
      return;
    }
    super.doGet(req, resp);
  }

  @Override
  protected void doPost(@NotNull HttpServletRequest req, @NotNull HttpServletResponse resp) throws ServletException, IOException {
    try {
      checkMimeType(req.getContentType(), Constants.MIME_LFS_JSON);
      if (req.getPathInfo() == null) {
        processObjectPost(req).write(resp);
        return;
      }
      if ("/batch".equals(req.getPathInfo())) {
        processBatchPost(req).write(resp);
        return;
      }
    } catch (ServerError e) {
      resp.setStatus(e.getStatusCode());
      resp.setContentType(Constants.MIME_LFS_JSON);
      JsonHelper.createMapper().writeValue(resp.getOutputStream(), new Error(e.getStatusCode(), e.getMessage()));
      return;
    }
    super.doPost(req, resp);
  }

  public static void checkMimeType(@Nullable String contentType, @NotNull String mimeType) throws ServerError {
    String actualType = contentType;
    if (actualType != null) {
      int separator = actualType.indexOf(';');
      if (separator >= 0) {
        while (separator > 1 && actualType.charAt(separator - 1) == ' ') {
          separator--;
        }
        actualType = actualType.substring(0, separator);
      }
    }
    if (!mimeType.equals(actualType)) {
      throw new ServerError(HttpServletResponse.SC_NOT_ACCEPTABLE, "Not Acceptable", null);
    }
  }

  @NotNull
  private ResponseWriter processObjectPost(@NotNull HttpServletRequest req) throws ServerError, IOException {
    final URI selfUrl = getSelfUrl(req);
    final PointerManager.Locator locator = manager.checkUploadAccess(req, selfUrl);
    final Meta meta = mapper.readValue(req.getInputStream(), Meta.class);
    final BatchItem location = getLocation(locator, meta);
    final Error error = location.getError();
    if (error != null) {
      throw new ServerError(error.getCode(), error.getMessage(), null);
    }
    final Map<LinkType, Link> links = new TreeMap<>(location.getLinks());
    links.put(LinkType.Self, new Link(selfUrl, null, null));
    if (links.containsKey(LinkType.Download)) {
      return new ObjectResponse(HttpServletResponse.SC_OK, new ObjectRes(location.getOid(), location.getSize(), addSelfLink(req, links)));
    }
    if (links.containsKey(LinkType.Upload)) {
      return new ObjectResponse(HttpServletResponse.SC_ACCEPTED, new ObjectRes(location.getOid(), location.getSize(), addSelfLink(req, links)));
    }
    throw new ServerError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid locations list", null);
  }

  @NotNull
  private ResponseWriter processBatchPost(@NotNull HttpServletRequest req) throws ServerError, IOException {
    final BatchReq batchReq = mapper.readValue(req.getInputStream(), BatchReq.class);
    final PointerManager.Locator locator = batchReq.getOperation().visit(accessCheckerVisitor).checkAccess(req, getSelfUrl(req));
    final BatchItem[] locations = getLocations(locator, batchReq.getObjects().toArray(new Meta[batchReq.getObjects().size()]));
    return new ObjectResponse(HttpServletResponse.SC_OK, new BatchRes(Arrays.asList(locations)));
  }

  @NotNull
  private ResponseWriter processObjectGet(@NotNull HttpServletRequest req, @NotNull String oid) throws ServerError, IOException {
    final PointerManager.Locator locator = manager.checkDownloadAccess(req, getSelfUrl(req));
    final BatchItem location = getLocation(locator, new Meta(oid, -1));
    // Return error information.
    final Error error = location.getError();
    if (error != null) {
      throw new ServerError(error.getCode(), error.getMessage(), null);
    }
    if (location.getLinks().containsKey(LinkType.Download)) {
      return new ObjectResponse(HttpServletResponse.SC_OK, new ObjectRes(location.getOid(), location.getSize(), addSelfLink(req, location.getLinks())));
    }
    throw new ServerError(HttpServletResponse.SC_NOT_FOUND, "Object not found", null);
  }

  @NotNull
  protected URI getSelfUrl(@NotNull HttpServletRequest req) {
    try {
      return new URI(req.getScheme(), null, req.getServerName(), req.getServerPort(), req.getServletPath(), null, null);
    } catch (URISyntaxException e) {
      throw new IllegalStateException("Can't create request URL", e);
    }
  }

  @NotNull
  private BatchItem getLocation(@NotNull PointerManager.Locator locator, @NotNull Meta meta) throws IOException, ServerError {
    return getLocations(locator, new Meta[]{meta})[0];
  }

  private static Map<LinkType, Link> addSelfLink(@NotNull HttpServletRequest req, @NotNull Map<LinkType, Link> links) {
    final Map<LinkType, Link> result = new TreeMap<>(links);
    result.put(LinkType.Self, new Link(URI.create(String.valueOf(req.getRequestURL())), null, null));
    return result;
  }

  @NotNull
  private BatchItem[] getLocations(@NotNull PointerManager.Locator locator, @NotNull Meta[] metas) throws ServerError, IOException {
    final BatchItem[] locations = locator.getLocations(metas);
    // Invalid locations list.
    if (locations.length != metas.length) {
      throw new ServerError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected locations array size", null);
    }
    for (int i = 0; i < locations.length; ++i) {
      if (!Objects.equals(metas[i].getOid(), locations[i].getOid())) {
        throw new ServerError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Metadata mismatch", null);
      }
    }
    return locations;
  }

  @FunctionalInterface
  protected interface AccessChecker {
    @NotNull
    PointerManager.Locator checkAccess(@NotNull HttpServletRequest request, @NotNull URI selfUrl) throws IOException, ForbiddenError, UnauthorizedError;
  }

  @FunctionalInterface
  protected interface LocationFilter {
    @NotNull
    BatchItem filter(@NotNull BatchItem item) throws IOException;
  }

  private static class AccessCheckerVisitor implements Operation.Visitor<AccessChecker> {
    @NotNull
    private final PointerManager manager;

    public AccessCheckerVisitor(@NotNull PointerManager manager) {
      this.manager = manager;
    }

    @Override
    public AccessChecker visitDownload() {
      return wrapChecker(manager::checkDownloadAccess, PointerServlet::filterDownload);
    }

    @Override
    public AccessChecker visitUpload() {
      return wrapChecker(manager::checkUploadAccess, PointerServlet::filterUpload);
    }

    private AccessChecker wrapChecker(@NotNull AccessChecker checker, @NotNull LocationFilter filter) {
      return (request, selfUrl) -> {
        PointerManager.Locator locator = checker.checkAccess(request, selfUrl);
        return metas -> filterLocations(locator.getLocations(metas), filter);
      };
    }
  }
}
