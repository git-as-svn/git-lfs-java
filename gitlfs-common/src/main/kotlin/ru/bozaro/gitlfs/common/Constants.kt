package ru.bozaro.gitlfs.common

/**
 * Git-lfs constants.
 *
 * @author Artem V. Navrotskiy
 */
object Constants {
    /**
     * See [][<a href=]//www.w3.org/Protocols/rfc2616/rfc2616-sec14.html.sec14.1">HTTP/1.1 documentation">&lt;a href=&quot;http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1&quot;&gt;HTTP/1.1 documentation&lt;/a&gt;.
     */
    const val HEADER_AUTHORIZATION = "Authorization"

    const val HEADER_ACCEPT = "Accept"

    const val HEADER_LOCATION = "Location"

    const val MIME_LFS_JSON = "application/vnd.git-lfs+json"

    const val MIME_BINARY = "application/octet-stream"

    const val PATH_OBJECTS = "objects"

    const val PATH_BATCH = "objects/batch"

    const val PATH_LOCKS = "locks"

    /**
     * Minimal supported batch size.
     */
    const val BATCH_SIZE = 100
}
