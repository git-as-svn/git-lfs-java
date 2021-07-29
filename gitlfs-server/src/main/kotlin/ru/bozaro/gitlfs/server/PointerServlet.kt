package ru.bozaro.gitlfs.server

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import ru.bozaro.gitlfs.common.Constants
import ru.bozaro.gitlfs.common.JsonHelper
import ru.bozaro.gitlfs.common.data.*
import ru.bozaro.gitlfs.server.PointerServlet.AccessChecker
import ru.bozaro.gitlfs.server.internal.ObjectResponse
import ru.bozaro.gitlfs.server.internal.ResponseWriter
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.util.*
import java.util.regex.Pattern

/**
 * Servlet for pointer storage.
 *
 *
 * This servlet is entry point for git-lfs client.
 *
 *
 * Need to be mapped by path: objects/
 *
 *
 * Supported URL paths:
 *
 *
 * * POST: /
 * * POST: /batch
 * * GET:  /:oid
 *
 * @author Artem V. Navrotskiy
 */
class PointerServlet(private val manager: PointerManager) : HttpServlet() {
    private val accessCheckerVisitor: AccessCheckerVisitor = AccessCheckerVisitor(manager)

    /**
     * Create pointer manager for local ContentManager.
     *
     * @param manager         Content manager.
     * @param contentLocation Absolute or relative URL to ContentServlet.
     */
    constructor(manager: ContentManager, contentLocation: String) : this(
        LocalPointerManager(
            manager,
            contentLocation
        )
    )

    @Throws(ServletException::class, IOException::class)
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        try {
            if (req.pathInfo != null && PATTERN_OID.matcher(req.pathInfo).matches()) {
                processObjectGet(req, req.pathInfo.substring(1)).write(resp)
                return
            }
        } catch (e: ServerError) {
            sendError(resp, e)
            return
        }
        super.doGet(req, resp)
    }

    @Throws(ServletException::class, IOException::class)
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        try {
            checkMimeTypes(req)
            if (req.pathInfo == null) {
                processObjectPost(req).write(resp)
                return
            }
            if ("/batch" == req.pathInfo) {
                processBatchPost(req).write(resp)
                return
            }
        } catch (e: ServerError) {
            sendError(resp, e)
            return
        }
        super.doPost(req, resp)
    }

    @Throws(ServerError::class, IOException::class)
    private fun processObjectPost(req: HttpServletRequest): ResponseWriter {
        val selfUrl = getSelfUrl(req)
        val locator = manager.checkUploadAccess(req, selfUrl)
        val meta = JsonHelper.mapper.readValue(req.inputStream, Meta::class.java)
        val location = getLocation(locator, meta)
        val error = location.error
        if (error != null) {
            throw ServerError(error.code, error.message, null)
        }
        val links: MutableMap<LinkType, Link> = TreeMap(location.links)
        links[LinkType.Self] = Link(selfUrl, null, null)
        if (links.containsKey(LinkType.Download)) {
            return ObjectResponse(
                HttpServletResponse.SC_OK,
                ObjectRes(location.oid, location.size, addSelfLink(req, links))
            )
        }
        if (links.containsKey(LinkType.Upload)) {
            return ObjectResponse(
                HttpServletResponse.SC_ACCEPTED,
                ObjectRes(location.oid, location.size, addSelfLink(req, links))
            )
        }
        throw ServerError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid locations list", null)
    }

    @Throws(ServerError::class, IOException::class)
    private fun processBatchPost(req: HttpServletRequest): ResponseWriter {
        val batchReq = JsonHelper.mapper.readValue(req.inputStream, BatchReq::class.java)
        val locator = batchReq.operation.visit(accessCheckerVisitor).checkAccess(req, getSelfUrl(req))
        val locations = getLocations(locator, batchReq.objects.toTypedArray())
        return ObjectResponse(HttpServletResponse.SC_OK, BatchRes(listOf(*locations)))
    }

    private fun getSelfUrl(req: HttpServletRequest): URI {
        return try {
            URI(req.scheme, null, req.serverName, req.serverPort, req.servletPath, null, null)
        } catch (e: URISyntaxException) {
            throw IllegalStateException("Can't create request URL", e)
        }
    }

    @Throws(IOException::class, ServerError::class)
    private fun getLocation(locator: PointerManager.Locator, meta: Meta): BatchItem {
        return getLocations(locator, arrayOf(meta))[0]
    }

    @Throws(ServerError::class, IOException::class)
    private fun getLocations(
        locator: PointerManager.Locator,
        metas: Array<Meta>
    ): Array<BatchItem> {
        val locations = locator.getLocations(metas)
        // Invalid locations list.
        if (locations.size != metas.size) {
            throw ServerError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected locations array size", null)
        }
        for (i in locations.indices) {
            if (metas[i].oid != locations[i].oid) {
                throw ServerError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Metadata mismatch", null)
            }
        }
        return locations
    }

    @Throws(ServerError::class, IOException::class)
    private fun processObjectGet(req: HttpServletRequest, oid: String): ResponseWriter {
        val locator = manager.checkDownloadAccess(req, getSelfUrl(req))
        val location = getLocation(locator, Meta(oid, -1))
        // Return error information.
        val error = location.error
        if (error != null) {
            throw ServerError(error.code, error.message, null)
        }
        if (location.links.containsKey(LinkType.Download)) {
            return ObjectResponse(
                HttpServletResponse.SC_OK,
                ObjectRes(location.oid, location.size, addSelfLink(req, location.links))
            )
        }
        throw ServerError(HttpServletResponse.SC_NOT_FOUND, "Object not found", null)
    }

    private fun interface AccessChecker {
        @Throws(IOException::class, ForbiddenError::class, UnauthorizedError::class)
        fun checkAccess(request: HttpServletRequest, selfUrl: URI): PointerManager.Locator
    }

    private fun interface LocationFilter {
        @Throws(IOException::class)
        fun filter(item: BatchItem): BatchItem
    }

    private class AccessCheckerVisitor(private val manager: PointerManager) : Operation.Visitor<AccessChecker> {
        override fun visitDownload(): AccessChecker {
            return wrapChecker({ request: HttpServletRequest, selfUrl: URI ->
                manager.checkDownloadAccess(
                    request,
                    selfUrl
                )
            }, { item: BatchItem -> filterDownload(item) })
        }

        override fun visitUpload(): AccessChecker {
            return wrapChecker({ request: HttpServletRequest, selfUrl: URI ->
                manager.checkUploadAccess(
                    request,
                    selfUrl
                )
            }, { item: BatchItem -> filterUpload(item) })
        }

        private fun wrapChecker(checker: AccessChecker, filter: LocationFilter): AccessChecker {
            return AccessChecker { request: HttpServletRequest, selfUrl: URI ->
                val locator = checker.checkAccess(request, selfUrl)
                PointerManager.Locator { metas: Array<Meta> -> filterLocations(locator.getLocations(metas), filter) }
            }
        }
    }

    companion object {
        val PATTERN_OID: Pattern = Pattern.compile("^/[0-9a-f]{64}$")

        @Throws(IOException::class)
        private fun filterLocations(
            items: Array<BatchItem>,
            filter: LocationFilter
        ): Array<BatchItem> {
            return items.map {
                if (it.error == null) {
                    filter.filter(it)
                } else {
                    it
                }
            }.toTypedArray()
        }

        private fun filterDownload(item: BatchItem): BatchItem {
            return if (item.links.containsKey(LinkType.Download)) BatchItem(
                item.oid,
                item.size,
                filterLocation(item.links, LinkType.Download),
                null,
                null
            ) else BatchItem(
                item.oid,
                item.size,
                null,
                null,
                Error(HttpServletResponse.SC_NOT_FOUND, "Object not found")
            )
        }

        private fun filterLocation(
            links: Map<LinkType, Link>,
            vararg linkTypes: LinkType
        ): Map<LinkType, Link> {
            val result = TreeMap<LinkType, Link>()
            for (linkType in linkTypes) {
                val link = links[linkType]
                if (link != null) result[linkType] = link
            }
            return result
        }

        @Throws(IOException::class)
        private fun filterUpload(item: BatchItem): BatchItem {
            if (item.links.containsKey(LinkType.Download)) return BatchItem(
                item.oid,
                item.size,
                filterLocation(item.links, LinkType.Verify),
                null,
                null
            )
            if (item.links.containsKey(LinkType.Upload)) return BatchItem(
                item.oid,
                item.size,
                filterLocation(item.links, LinkType.Upload, LinkType.Verify),
                null,
                null
            )
            throw IOException("Upload link not found")
        }

        @Throws(ServerError::class)
        fun checkMimeTypes(request: HttpServletRequest) {
            checkMimeType(request.contentType)
            checkMimeType(request.getHeader(Constants.HEADER_ACCEPT))
        }

        @Throws(IOException::class)
        fun sendError(resp: HttpServletResponse, e: ServerError) {
            resp.status = e.statusCode
            resp.contentType = Constants.MIME_LFS_JSON
            JsonHelper.mapper.writeValue(resp.outputStream, Error(e.statusCode, e.message))
        }

        @Throws(ServerError::class)
        private fun checkMimeType(contentType: String?) {
            var actualType = contentType
            if (actualType != null) {
                var separator = actualType.indexOf(';')
                if (separator >= 0) {
                    while (separator > 1 && actualType[separator - 1] == ' ') {
                        separator--
                    }
                    actualType = actualType.substring(0, separator)
                }
            }
            if (Constants.MIME_LFS_JSON != actualType) {
                throw ServerError(HttpServletResponse.SC_NOT_ACCEPTABLE, "Not Acceptable", null)
            }
        }

        private fun addSelfLink(
            req: HttpServletRequest,
            links: Map<LinkType, Link>
        ): Map<LinkType, Link> {
            val result: MutableMap<LinkType, Link> = TreeMap(links)
            result[LinkType.Self] = Link(URI.create(req.requestURL.toString()), null, null)
            return result
        }
    }

}
