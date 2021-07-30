package ru.bozaro.gitlfs.server

import jakarta.servlet.http.HttpServletRequest
import ru.bozaro.gitlfs.common.Constants
import ru.bozaro.gitlfs.common.data.*
import ru.bozaro.gitlfs.server.ContentManager.HeaderProvider
import java.io.IOException
import java.net.URI
import java.util.*

/**
 * Pointer manager for local ContentManager.
 *
 * @author Artem V. Navrotskiy
 */
class LocalPointerManager(private val manager: ContentManager, contentLocation: String) : PointerManager {
    private val contentLocation: String = if (contentLocation.endsWith("/")) contentLocation else "$contentLocation/"

    @Throws(IOException::class, ForbiddenError::class, UnauthorizedError::class)
    override fun checkUploadAccess(request: HttpServletRequest, selfUrl: URI): PointerManager.Locator {
        val headerProvider: HeaderProvider = manager.checkUploadAccess(request)
        return createLocator(request, headerProvider, selfUrl)
    }

    @Throws(IOException::class, ForbiddenError::class, UnauthorizedError::class)
    override fun checkDownloadAccess(request: HttpServletRequest, selfUrl: URI): PointerManager.Locator {
        val headerProvider: HeaderProvider = manager.checkDownloadAccess(request)
        return createLocator(request, headerProvider, selfUrl)
    }

    private fun createLocator(
            request: HttpServletRequest,
            headerProvider: HeaderProvider,
            selfUrl: URI
    ): PointerManager.Locator {
        val header = headerProvider.createHeader(createDefaultHeader(request))
        return object : PointerManager.Locator {
            @Throws(IOException::class)
            override fun getLocations(metas: Array<Meta>): Array<BatchItem> {
                return metas.map { getLocation(header, selfUrl, it) }.toTypedArray()
            }

            @Throws(IOException::class)
            fun getLocation(header: Map<String, String>, selfUrl: URI, meta: Meta): BatchItem {
                val storageMeta = manager.getMetadata(meta.oid)
                if (storageMeta != null && meta.size >= 0 && storageMeta.size != meta.size) return BatchItem(
                        meta,
                        Error(422, "Invalid object size")
                )
                val links: MutableMap<LinkType, Link> = EnumMap(LinkType::class.java)
                val link = Link(selfUrl.resolve(contentLocation).resolve(meta.oid), header, null)
                if (storageMeta == null) links[LinkType.Upload] = link else links[LinkType.Download] = link
                links[LinkType.Verify] = link
                return BatchItem(storageMeta ?: meta, links)
            }
        }
    }

    private fun createDefaultHeader(request: HttpServletRequest): Map<String, String> {
        val auth = request.getHeader(Constants.HEADER_AUTHORIZATION)
        val header = HashMap<String, String>()
        if (auth != null) {
            header[Constants.HEADER_AUTHORIZATION] = auth
        }
        return header
    }
}
