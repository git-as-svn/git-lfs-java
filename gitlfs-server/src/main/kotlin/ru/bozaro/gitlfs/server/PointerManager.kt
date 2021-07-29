package ru.bozaro.gitlfs.server

import jakarta.servlet.http.HttpServletRequest
import ru.bozaro.gitlfs.common.data.BatchItem
import ru.bozaro.gitlfs.common.data.Meta
import java.io.IOException
import java.net.URI

/**
 * Interface for lookup pointer information.
 *
 * @author Artem V. Navrotskiy
 */
interface PointerManager {
    /**
     * Check access for upload objects.
     *
     * @param request HTTP request.
     * @param selfUrl Http URL for this request.
     * @return Location provider.
     */
    @Throws(IOException::class, ForbiddenError::class, UnauthorizedError::class)
    fun checkUploadAccess(request: HttpServletRequest, selfUrl: URI): Locator

    /**
     * Check access for download objects.
     *
     * @param request HTTP request.
     * @param selfUrl Http URL for this request.
     * @return Location provider.
     */
    @Throws(IOException::class, ForbiddenError::class, UnauthorizedError::class)
    fun checkDownloadAccess(request: HttpServletRequest, selfUrl: URI): Locator

    fun interface Locator {
        /**
         * @param metas Object hash array (note: metadata can have negative size for GET object request).
         * @return Return batch items with same order and same count as metas array.
         */
        @Throws(IOException::class)
        fun getLocations(metas: Array<Meta>): Array<BatchItem>
    }
}
