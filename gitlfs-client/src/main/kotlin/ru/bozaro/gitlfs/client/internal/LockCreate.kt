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
import ru.bozaro.gitlfs.common.data.*
import java.io.IOException

class LockCreate(private val path: String, private val ref: Ref?) : Request<LockCreate.Res> {
    @Throws(JsonProcessingException::class)
    override fun createRequest(mapper: ObjectMapper, url: String): LfsRequest {
        val req = HttpPost(url)
        req.addHeader(HEADER_ACCEPT, MIME_LFS_JSON)
        val createLockReq = CreateLockReq(path, ref)
        val entity: AbstractHttpEntity = ByteArrayEntity(mapper.writeValueAsBytes(createLockReq))
        entity.setContentType(MIME_LFS_JSON)
        req.entity = entity
        return LfsRequest(req, entity)
    }

    @Throws(IOException::class)
    override fun processResponse(mapper: ObjectMapper, response: HttpResponse): Res {
        return when (response.statusLine.statusCode) {
            HttpStatus.SC_CREATED -> Res(true, mapper.readValue(response.entity.content, CreateLockRes::class.java).lock, null)
            HttpStatus.SC_CONFLICT -> {
                val res = mapper.readValue(response.entity.content, LockConflictRes::class.java)
                Res(false, res.lock, res.message)
            }
            else -> throw IllegalStateException()
        }
    }

    override fun statusCodes(): IntArray {
        return intArrayOf(
                HttpStatus.SC_CREATED,
                HttpStatus.SC_CONFLICT)
    }

    class Res(val isSuccess: Boolean, val lock: Lock, val message: String?)
}
