package ru.bozaro.gitlfs.server

import jakarta.servlet.Servlet
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.ServletHandler
import org.eclipse.jetty.servlet.ServletHolder
import java.net.URI
import java.net.URISyntaxException

/**
 * Embedded HTTP server for servlet testing.
 *
 * @author Artem V. Navrotskiy
 */
class EmbeddedHttpServer : AutoCloseable {
    private val server: Server = Server()
    private val http: ServerConnector = ServerConnector(server, HttpConnectionFactory())
    private val servletHandler: ServletHandler

    val base: URI
        get() = try {
            URI("http", null, http.host, http.localPort, null, null, null)
        } catch (e: URISyntaxException) {
            throw IllegalStateException(e)
        }

    fun addServlet(pathSpec: String, servlet: Servlet) {
        servletHandler.addServletWithMapping(ServletHolder(servlet), pathSpec)
    }

    @Throws(Exception::class)
    override fun close() {
        server.stop()
        server.join()
    }

    init {
        http.port = 0
        http.host = "127.0.1.1"
        server.addConnector(http)
        servletHandler = ServletHandler()
        server.handler = servletHandler
        server.start()
    }
}
