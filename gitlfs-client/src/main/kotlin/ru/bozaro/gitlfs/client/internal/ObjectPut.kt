package ru.bozaro.gitlfs.client.internal

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpPut
import org.apache.http.entity.AbstractHttpEntity
import org.apache.http.entity.InputStreamEntity
import ru.bozaro.gitlfs.client.io.StreamProvider
import ru.bozaro.gitlfs.common.Constants.MIME_BINARY
import java.io.IOException

/**
 * PUT object request.
 *
 * @author Artem V. Navrotskiy
 */
class ObjectPut(private val streamProvider: StreamProvider, private val size: Long) : Request<Void?> {
    @Throws(IOException::class)
    override fun createRequest(mapper: ObjectMapper, url: String): LfsRequest {
        val req = HttpPut(url)
        val entity: AbstractHttpEntity = InputStreamEntity(streamProvider.stream, size)
        entity.setContentType(MIME_BINARY)
        req.entity = entity
        return LfsRequest(req, entity)
    }

    override fun processResponse(mapper: ObjectMapper, response: HttpResponse): Void? {
        return null
    }

    override fun statusCodes(): IntArray {
        return intArrayOf(
                HttpStatus.SC_OK,
                HttpStatus.SC_CREATED
        )
    }
}
