package ru.bozaro.gitlfs.server

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import ru.bozaro.gitlfs.common.Constants
import ru.bozaro.gitlfs.common.JsonHelper
import ru.bozaro.gitlfs.common.data.Meta
import ru.bozaro.gitlfs.common.io.InputStreamValidator
import ru.bozaro.gitlfs.server.internal.ObjectResponse
import ru.bozaro.gitlfs.server.internal.ResponseWriter
import java.io.IOException
import java.util.regex.Pattern

/**
 * Servlet for content storage.
 *
 * @author Artem V. Navrotskiy
 */
class ContentServlet(private val manager: ContentManager) : HttpServlet() {
    @Throws(ServletException::class, IOException::class)
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        try {
            if (req.pathInfo != null && PATTERN_OID.matcher(req.pathInfo).matches()) {
                processGet(req, req.pathInfo.substring(1)).write(resp)
                return
            }
        } catch (e: ServerError) {
            PointerServlet.sendError(resp, e)
            return
        }
        super.doGet(req, resp)
    }

    companion object {
        val PATTERN_OID: Pattern = Pattern.compile("^/[0-9a-f]{64}$")
    }

    @Throws(IOException::class, ServletException::class)
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        try {
            if (req.pathInfo != null && PATTERN_OID.matcher(req.pathInfo).matches()) {
                processObjectVerify(req, req.pathInfo.substring(1)).write(resp)
                return
            }
        } catch (e: ServerError) {
            PointerServlet.sendError(resp, e)
            return
        }
        super.doPost(req, resp)
    }

    @Throws(IOException::class, ServerError::class)
    private fun processObjectVerify(req: HttpServletRequest, oid: String): ResponseWriter {
        manager.checkUploadAccess(req)
        val expectedMeta = JsonHelper.mapper.readValue(req.inputStream, Meta::class.java)
        val actualMeta = manager.getMetadata(oid)
        if (!expectedMeta.equals(actualMeta)) throw ServerError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                String.format("LFS verification failure: server=%s client=%s", expectedMeta, actualMeta),
                null
        )
        return ResponseWriter { response: HttpServletResponse -> response.status = HttpServletResponse.SC_OK }
    }

    @Throws(ServletException::class, IOException::class)
    override fun doPut(req: HttpServletRequest, resp: HttpServletResponse) {
        try {
            if (req.pathInfo != null && PATTERN_OID.matcher(req.pathInfo).matches()) {
                processPut(req, req.pathInfo.substring(1)).write(resp)
                return
            }
        } catch (e: ServerError) {
            PointerServlet.sendError(resp, e)
            return
        }
        super.doPut(req, resp)
    }

    @Throws(ServerError::class, IOException::class)
    private fun processPut(req: HttpServletRequest, oid: String): ResponseWriter {
        val uploader = manager.checkUploadAccess(req)
        val meta = Meta(oid, -1)
        uploader.saveObject(meta, InputStreamValidator(req.inputStream, meta))
        return ObjectResponse(HttpServletResponse.SC_OK, meta)
    }

    @Throws(ServerError::class, IOException::class)
    private fun processGet(req: HttpServletRequest, oid: String): ResponseWriter {
        val downloader = manager.checkDownloadAccess(req)
        val stream = downloader.openObject(oid)
        return ResponseWriter { response: HttpServletResponse ->
            response.status = HttpServletResponse.SC_OK
            response.contentType = Constants.MIME_BINARY
            stream.use { stream ->
                val buffer = ByteArray(0x10000)
                while (true) {
                    val read = stream.read(buffer)
                    if (read < 0) break
                    response.outputStream.write(buffer, 0, read)
                }
            }
        }
    }
}
