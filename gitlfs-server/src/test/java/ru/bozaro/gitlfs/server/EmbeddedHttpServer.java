package ru.bozaro.gitlfs.server;

import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.annotation.Nonnull;
import javax.servlet.Servlet;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Embedded HTTP server for servlet testing.
 *
 * @author Artem V. Navrotskiy
 */
public class EmbeddedHttpServer implements AutoCloseable {
  @Nonnull
  private final Server server;
  @Nonnull
  private final ServerConnector http;
  @Nonnull
  private final ServletHandler servletHandler;

  public EmbeddedHttpServer() throws Exception {
    this.server = new Server();
    this.http = new ServerConnector(server, new HttpConnectionFactory());
    http.setPort(0);
    http.setHost("127.0.1.1");
    server.addConnector(http);

    this.servletHandler = new ServletHandler();
    server.setHandler(servletHandler);

    server.start();
  }

  @Nonnull
  public URI getBase() {
    try {
      return new URI("http", null, http.getHost(), http.getLocalPort(), null, null, null);
    } catch (URISyntaxException e) {
      throw new IllegalStateException(e);
    }
  }

  public void addServlet(@Nonnull String pathSpec, @Nonnull Servlet servlet) {
    servletHandler.addServletWithMapping(new ServletHolder(servlet), pathSpec);
  }

  @Override
  public void close() throws Exception {
    server.stop();
    server.join();
  }
}
