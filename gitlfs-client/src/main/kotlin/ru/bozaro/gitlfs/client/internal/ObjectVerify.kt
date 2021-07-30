package ru.bozaro.gitlfs.client.internal

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.AbstractHttpEntity
import org.apache.http.entity.ByteArrayEntity
import ru.bozaro.gitlfs.common.Constants.HEADER_ACCEPT
import ru.bozaro.gitlfs.common.Constants.MIME_LFS_JSON
import ru.bozaro.gitlfs.common.data.Meta
import java.io.IOException

/**
 * Object verification after upload request.
 *
 * @author Artem V. Navrotskiy
 */
class ObjectVerify(private val meta: Meta) : Request<Void?> {
    @Throws(IOException::class)
    override fun createRequest(mapper: ObjectMapper, url: String): LfsRequest {
        val req = HttpPost(url)
        req.addHeader(HEADER_ACCEPT, MIME_LFS_JSON)
        val content = mapper.writeValueAsBytes(meta)
        val entity: AbstractHttpEntity = ByteArrayEntity(content)
        entity.setContentType(MIME_LFS_JSON)
        req.entity = entity
        return LfsRequest(req, entity)
    }

    override fun processResponse(mapper: ObjectMapper, response: HttpResponse): Void? {
        return null
    }
}
