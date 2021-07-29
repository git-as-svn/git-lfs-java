package ru.bozaro.gitlfs.client.internal

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import ru.bozaro.gitlfs.common.Constants.HEADER_ACCEPT
import ru.bozaro.gitlfs.common.Constants.MIME_LFS_JSON
import java.io.IOException

/**
 * POST simple JSON request.
 *
 * @author Artem V. Navrotskiy
 */
class JsonPost<Req, Res>(private val req: Req, private val type: Class<Res>) : Request<Res> {
    @Throws(JsonProcessingException::class)
    override fun createRequest(mapper: ObjectMapper, url: String): LfsRequest {
        val method = HttpPost(url)
        method.addHeader(HEADER_ACCEPT, MIME_LFS_JSON)
        val entity = ByteArrayEntity(mapper.writeValueAsBytes(req))
        entity.setContentType(MIME_LFS_JSON)
        method.entity = entity
        return LfsRequest(method, entity)
    }

    @Throws(IOException::class)
    override fun processResponse(mapper: ObjectMapper, response: HttpResponse): Res {
        return mapper.readValue(response.entity.content, type)
    }
}
