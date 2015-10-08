package ru.bozaro.gitlfs.server;

import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.common.Constants;
import ru.bozaro.gitlfs.common.data.Meta;
import ru.bozaro.gitlfs.common.data.Operation;
import ru.bozaro.gitlfs.server.internal.ObjectResponse;
import ru.bozaro.gitlfs.server.internal.ResponseWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * Servlet for content storage.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class ContentServlet<T> extends HttpServlet {
  @NotNull
  private final Pattern PATTERN_OID = Pattern.compile("^\\/[0-9a-f]{64}$");
  @NotNull
  private final ContentManager<T> manager;

  public ContentServlet(@NotNull ContentManager<T> manager) {
    this.manager = manager;
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      if ((req.getPathInfo() != null) && PATTERN_OID.matcher(req.getPathInfo()).matches()) {
        processPut(req, req.getPathInfo().substring(1)).write(resp);
        return;
      }
    } catch (ServerError e) {
      resp.setStatus(e.getStatusCode());
      resp.getWriter().println(e.getMessage());
      return;
    }
    super.doPut(req, resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      if ((req.getPathInfo() != null) && PATTERN_OID.matcher(req.getPathInfo()).matches()) {
        processGet(req, req.getPathInfo().substring(1)).write(resp);
        return;
      }
    } catch (ServerError e) {
      resp.setStatus(e.getStatusCode());
      resp.getWriter().println(e.getMessage());
      return;
    }
    super.doGet(req, resp);
  }

  @NotNull
  private ResponseWriter processPut(@NotNull HttpServletRequest req, @NotNull String oid) throws ServerError, IOException {
    final T access = manager.checkAccess(req, Operation.Upload);
    final Meta meta = new Meta(oid, -1);
    manager.saveObject(access, meta, req.getInputStream());
    return new ObjectResponse(HttpServletResponse.SC_OK, meta);
  }

  @NotNull
  private ResponseWriter processGet(@NotNull HttpServletRequest req, @NotNull String oid) throws ServerError, IOException {
    final T access = manager.checkAccess(req, Operation.Upload);
    final InputStream stream = manager.openObject(access, oid);
    return new ResponseWriter() {
      @Override
      public void write(@NotNull HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(Constants.MIME_BINARY);
        //noinspection TryFinallyCanBeTryWithResources
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
      }
    };
  }
}
