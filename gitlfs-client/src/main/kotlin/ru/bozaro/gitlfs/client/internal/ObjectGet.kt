package ru.bozaro.gitlfs.client.internal

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import ru.bozaro.gitlfs.client.io.StreamHandler
import java.io.IOException

/**
 * GET object request.
 *
 * @author Artem V. Navrotskiy
 */
class ObjectGet<T>(private val handler: StreamHandler<T>) : Request<T> {
    override fun createRequest(mapper: ObjectMapper, url: String): LfsRequest {
        val req = HttpGet(url)
        return LfsRequest(req, null)
    }

    @Throws(IOException::class)
    override fun processResponse(mapper: ObjectMapper, response: HttpResponse): T {
        return handler.accept(response.entity.content)
    }
}
