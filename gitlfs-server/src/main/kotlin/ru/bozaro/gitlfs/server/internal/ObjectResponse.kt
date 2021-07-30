package ru.bozaro.gitlfs.server.internal

import jakarta.servlet.http.HttpServletResponse
import ru.bozaro.gitlfs.common.Constants.MIME_LFS_JSON
import ru.bozaro.gitlfs.common.JsonHelper
import java.io.IOException

/**
 * Response with simple JSON output.
 *
 * @author Artem V. Navrotskiy
 */
class ObjectResponse(private val status: Int, private val value: Any) : ResponseWriter {
    @Throws(IOException::class)
    override fun write(response: HttpServletResponse) {
        response.status = status
        response.contentType = MIME_LFS_JSON
        JsonHelper.mapper.writeValue(response.outputStream, value)
    }
}
