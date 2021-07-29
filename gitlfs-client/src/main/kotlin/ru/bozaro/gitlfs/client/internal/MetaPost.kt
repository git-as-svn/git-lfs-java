package ru.bozaro.gitlfs.client.internal

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.AbstractHttpEntity
import org.apache.http.entity.ByteArrayEntity
import ru.bozaro.gitlfs.common.Constants.HEADER_ACCEPT
import ru.bozaro.gitlfs.common.Constants.MIME_LFS_JSON
import ru.bozaro.gitlfs.common.data.Meta
import ru.bozaro.gitlfs.common.data.ObjectRes
import java.io.IOException

/**
 * POST object metadata request.
 *
 * @author Artem V. Navrotskiy
 */
class MetaPost(private val meta: Meta) : Request<ObjectRes?> {
    @Throws(JsonProcessingException::class)
    override fun createRequest(mapper: ObjectMapper, url: String): LfsRequest {
        val req = HttpPost(url)
        req.addHeader(HEADER_ACCEPT, MIME_LFS_JSON)
        val entity: AbstractHttpEntity = ByteArrayEntity(mapper.writeValueAsBytes(meta))
        entity.setContentType(MIME_LFS_JSON)
        req.entity = entity
        return LfsRequest(req, entity)
    }

    @Throws(IOException::class)
    override fun processResponse(mapper: ObjectMapper, response: HttpResponse): ObjectRes? {
        return when (response.statusLine.statusCode) {
            HttpStatus.SC_OK -> null
            HttpStatus.SC_ACCEPTED -> mapper.readValue(response.entity.content, ObjectRes::class.java)
            else -> throw IllegalStateException()
        }
    }

    override fun statusCodes(): IntArray {
        return intArrayOf(
                HttpStatus.SC_OK,
                HttpStatus.SC_ACCEPTED
        )
    }
}
