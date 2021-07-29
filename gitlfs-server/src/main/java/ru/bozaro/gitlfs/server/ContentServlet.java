package ru.bozaro.gitlfs.server;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.bozaro.gitlfs.common.Constants;
import ru.bozaro.gitlfs.common.JsonHelper;
import ru.bozaro.gitlfs.common.data.Meta;
import ru.bozaro.gitlfs.common.io.InputStreamValidator;
import ru.bozaro.gitlfs.server.internal.ObjectResponse;
import ru.bozaro.gitlfs.server.internal.ResponseWriter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import static ru.bozaro.gitlfs.server.PointerServlet.sendError;

/**
 * Servlet for content storage.
 *
 * @author Artem V. Navrotskiy
 */
public class ContentServlet extends HttpServlet {
  @Nonnull
  private final Pattern PATTERN_OID = Pattern.compile("^/[0-9a-f]{64}$");
  @Nonnull
  private final ContentManager manager;

  public ContentServlet(@Nonnull ContentManager manager) {
    this.manager = manager;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      if ((req.getPathInfo() != null) && PATTERN_OID.matcher(req.getPathInfo()).matches()) {
        processGet(req, req.getPathInfo().substring(1)).write(resp);
        return;
      }
    } catch (ServerError e) {
      sendError(resp, e);
      return;
    }
    super.doGet(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    try {
      if (req.getPathInfo() != null && PATTERN_OID.matcher(req.getPathInfo()).matches()) {
        processObjectVerify(req, req.getPathInfo().substring(1)).write(resp);
        return;
      }
    } catch (ServerError e) {
      sendError(resp, e);
      return;
    }

    super.doPost(req, resp);
  }

  @Nonnull
  private ResponseWriter processObjectVerify(@Nonnull HttpServletRequest req, @Nonnull String oid) throws IOException, ServerError {
    manager.checkUploadAccess(req);
    final Meta expectedMeta = JsonHelper.mapper.readValue(req.getInputStream(), Meta.class);
    final Meta actualMeta = manager.getMetadata(oid);
    if (!expectedMeta.equals(actualMeta))
      throw new ServerError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, String.format("LFS verification failure: server=%s client=%s", expectedMeta, actualMeta), null);

    return response -> response.setStatus(HttpServletResponse.SC_OK);
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      if ((req.getPathInfo() != null) && PATTERN_OID.matcher(req.getPathInfo()).matches()) {
        processPut(req, req.getPathInfo().substring(1)).write(resp);
        return;
      }
    } catch (ServerError e) {
      sendError(resp, e);
      return;
    }
    super.doPut(req, resp);
  }

  @Nonnull
  private ResponseWriter processPut(@Nonnull HttpServletRequest req, @Nonnull String oid) throws ServerError, IOException {
    final ContentManager.Uploader uploader = manager.checkUploadAccess(req);
    final Meta meta = new Meta(oid, -1);
    uploader.saveObject(meta, new InputStreamValidator(req.getInputStream(), meta));
    return new ObjectResponse(HttpServletResponse.SC_OK, meta);
  }

  @Nonnull
  private ResponseWriter processGet(@Nonnull HttpServletRequest req, @Nonnull String oid) throws ServerError, IOException {
    final ContentManager.Downloader downloader = manager.checkDownloadAccess(req);
    final InputStream stream = downloader.openObject(oid);
    return response -> {
      response.setStatus(HttpServletResponse.SC_OK);
      response.setContentType(Constants.MIME_BINARY);
      try {
        byte[] buffer = new byte[0x10000];
        while (true) {
          final int read = stream.read(buffer);
          if (read < 0) break;
          response.getOutputStream().write(buffer, 0, read);
        }
      } finally {
        stream.close();
      }
    };
  }
}
