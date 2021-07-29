package ru.bozaro.gitlfs.client.internal

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpGet
import ru.bozaro.gitlfs.common.Constants.HEADER_ACCEPT
import ru.bozaro.gitlfs.common.Constants.MIME_LFS_JSON
import ru.bozaro.gitlfs.common.data.ObjectRes
import java.io.IOException

/**
 * GET object metadata request.
 *
 * @author Artem V. Navrotskiy
 */
class MetaGet : Request<ObjectRes?> {
    override fun createRequest(mapper: ObjectMapper, url: String): LfsRequest {
        val req = HttpGet(url)
        req.addHeader(HEADER_ACCEPT, MIME_LFS_JSON)
        return LfsRequest(req, null)
    }

    @Throws(IOException::class)
    override fun processResponse(mapper: ObjectMapper, response: HttpResponse): ObjectRes? {
        return when (response.statusLine.statusCode) {
            HttpStatus.SC_OK -> mapper.readValue(response.entity.content, ObjectRes::class.java)
            HttpStatus.SC_NOT_FOUND -> null
            else -> throw IllegalStateException()
        }
    }

    override fun statusCodes(): IntArray {
        return intArrayOf(
                HttpStatus.SC_OK,
                HttpStatus.SC_NOT_FOUND
        )
    }
}
