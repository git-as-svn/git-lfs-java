package ru.bozaro.gitlfs.client.internal

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import ru.bozaro.gitlfs.common.Constants.HEADER_ACCEPT
import ru.bozaro.gitlfs.common.Constants.MIME_LFS_JSON
import ru.bozaro.gitlfs.common.data.LocksRes
import java.io.IOException

class LocksList : Request<LocksRes> {
    override fun createRequest(mapper: ObjectMapper, url: String): LfsRequest {
        val req = HttpGet(url)
        req.addHeader(HEADER_ACCEPT, MIME_LFS_JSON)
        return LfsRequest(req, null)
    }

    @Throws(IOException::class)
    override fun processResponse(mapper: ObjectMapper, response: HttpResponse): LocksRes {
        return mapper.readValue(response.entity.content, LocksRes::class.java)
    }
}
