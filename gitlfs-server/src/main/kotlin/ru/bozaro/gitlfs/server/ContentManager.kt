package ru.bozaro.gitlfs.server

import jakarta.servlet.http.HttpServletRequest
import ru.bozaro.gitlfs.common.data.Meta
import java.io.IOException
import java.io.InputStream

/**
 * Interface for store object content.
 *
 * @author Artem V. Navrotskiy
 */
interface ContentManager {
    /**
     * Check access for requested operation and return some user information.
     *
     * @param request HTTP request.
     * @return Object for send object.
     */
    @Throws(IOException::class, ForbiddenError::class, UnauthorizedError::class)
    fun checkDownloadAccess(request: HttpServletRequest): Downloader

    /**
     * Check access for requested operation and return some user information.
     *
     * @param request HTTP request.
     * @return Object for receive object.
     */
    @Throws(IOException::class, ForbiddenError::class, UnauthorizedError::class)
    fun checkUploadAccess(request: HttpServletRequest): Uploader

    /**
     * Get metadata of uploaded object.
     *
     * @param hash Object metadata (hash and size).
     * @return Return metadata of uploaded object.
     */
    @Throws(IOException::class)
    fun getMetadata(hash: String): Meta?

    interface HeaderProvider {
        /**
         * Generate pointer header information (for example: replace transit Basic auth by Toker auth).
         *
         * @param header Default header. Can be modified.
         * @return Pointer header information.
         */
        fun createHeader(header: Map<String, String>): Map<String, String> {
            return header
        }
    }

    interface Downloader : HeaderProvider {
        /**
         * Get object from storage.
         *
         * @param hash Object metadata (hash and size).
         * @return Return object stream.
         */
        @Throws(IOException::class)
        fun openObject(hash: String): InputStream

        /**
         * Get gzip-compressed object from storage.
         *
         * @param hash Object metadata (hash and size).
         * @return Return gzip-compressed object stream. If gzip-stream is not available return null.
         */
        @Throws(IOException::class)
        fun openObjectGzipped(hash: String): InputStream?
    }

    interface Uploader : HeaderProvider {
        /**
         * Save object to storage.
         *
         * @param meta    Object metadata (hash and size).
         * @param content Stream with object ru.bozaro.gitlfs.common.data.
         */
        @Throws(IOException::class)
        fun saveObject(meta: Meta, content: InputStream)
    }
}
