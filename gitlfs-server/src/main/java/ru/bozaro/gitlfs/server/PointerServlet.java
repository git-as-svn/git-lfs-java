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
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Servlet for pointer storage.
 * <p/>
 * This servlet is entry point for git-lfs client.
 * <p/>
 * Need to be mapped by path: objects/
 * <p/>
 * Supported URL paths:
 * <p/>
 * * POST: /
 * * POST: /batch
 * * GET:  /:oid
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class PointerServlet extends HttpServlet {
  @NotNull
  private final Pattern PATTERN_OID = Pattern.compile("^/[0-9a-f]{64}$");
  @NotNull
  private final ObjectMapper mapper;
  @NotNull
  private final PointerManager manager;

  public PointerServlet(@NotNull PointerManager manager) {
    this.manager = manager;
    this.mapper = JsonHelper.createMapper();
  }

  /**
   * Create pointer manager for local ContentManager.
   *
   * @param manager         Content manager.
   * @param contentLocation Absolute or relative URL to ContentServlet.
   */
  public PointerServlet(@NotNull ContentManager manager, @NotNull String contentLocation) {
    this(new LocalPointerManager(manager, contentLocation));
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

  @NotNull
  private ResponseWriter processObjectGet(@NotNull HttpServletRequest req, @NotNull String oid) throws ServerError, IOException {
    final PointerManager.Locator locator = manager.checkAccess(req, getSelfUrl(req), Operation.Download);
    final BatchItem[] locations = locator.getLocations(new Meta[]{new Meta(oid, -1)});
    // Invalid locations list.
    if (locations.length != 1) {
      throw new ServerError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected locations array size", null);
    }
    // Return error information.
    final BatchItem location = locations[0];
    final Error error = location.getError();
    if (error != null) {
      throw new ServerError(error.getCode(), error.getMessage(), null);
    }
    if (location.getLinks().containsKey(LinkType.Download)) {
      return new ObjectResponse(HttpServletResponse.SC_OK, new ObjectRes(location.getOid(), location.getSize(), filterLocation(req, location.getLinks(), LinkType.Download)));
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
  private ResponseWriter processObjectPost(@NotNull HttpServletRequest req) throws ServerError, IOException {
    final URI selfUrl = getSelfUrl(req);
    final PointerManager.Locator locator = manager.checkAccess(req, selfUrl, Operation.Upload);
    final Meta meta = mapper.readValue(req.getInputStream(), Meta.class);
    final BatchItem[] locations = locator.getLocations(new Meta[]{meta});
    // Invalid locations list.
    if (locations.length != 1) {
      throw new ServerError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected locations array size", null);
    }
    // Return error information.
    final BatchItem location = locations[0];
    final Error error = location.getError();
    if (error != null) {
      throw new ServerError(error.getCode(), error.getMessage(), null);
    }
    final Map<LinkType, Link> links = new TreeMap<>(location.getLinks());
    links.put(LinkType.Self, new Link(selfUrl, null, null));
    if (links.containsKey(LinkType.Download)) {
      return new ObjectResponse(HttpServletResponse.SC_OK, new ObjectRes(location.getOid(), location.getSize(), filterLocation(req, links, LinkType.Download)));
    }
    if (links.containsKey(LinkType.Upload)) {
      return new ObjectResponse(HttpServletResponse.SC_ACCEPTED, new ObjectRes(location.getOid(), location.getSize(), filterLocation(req, links, LinkType.Upload, LinkType.Verify)));
    }
    throw new ServerError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid locations list", null);
  }

  @NotNull
  private ResponseWriter processBatchPost(@NotNull HttpServletRequest req) throws ServerError, IOException {
    final BatchReq batchReq = mapper.readValue(req.getInputStream(), BatchReq.class);
    final PointerManager.Locator locator = manager.checkAccess(req, getSelfUrl(req), batchReq.getOperation());
    final BatchItem[] locations = locator.getLocations(batchReq.getObjects().toArray(new Meta[batchReq.getObjects().size()]));
    // Invalid locations list.
    if (locations.length != batchReq.getObjects().size()) {
      throw new ServerError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected locations array size", null);
    }
    return new ObjectResponse(HttpServletResponse.SC_OK, new BatchRes(Arrays.asList(locations)));
  }

  private static Map<LinkType, Link> filterLocation(@NotNull HttpServletRequest req, @NotNull Map<LinkType, Link> links, @NotNull LinkType... linkTypes) {
    final Map<LinkType, Link> result = new TreeMap<>();
    for (LinkType linkType : linkTypes) {
      final Link link = links.get(linkType);
      if (link != null) result.put(linkType, link);
    }
    result.put(LinkType.Self, new Link(URI.create(String.valueOf(req.getRequestURL())), null, null));
    return result;
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
}
