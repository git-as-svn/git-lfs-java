package ru.bozaro.gitlfs.client.internal

import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.AbstractHttpEntity
import org.apache.http.protocol.HTTP

class LfsRequest internal constructor(private val request: HttpUriRequest, private val entity: AbstractHttpEntity?) {
    fun addHeaders(headers: Map<String, String>): HttpUriRequest {
        for ((key, value) in headers) {
            if (HTTP.TRANSFER_ENCODING == key) {
                /*
                  See https://github.com/bozaro/git-as-svn/issues/365
                  LFS-server can ask us to respond with chunked body via setting "Transfer-Encoding: chunked" HTTP header in LFS link
                  Unfortunately, we cannot pass it as-is to response HTTP headers, see RequestContent#process.
                  If it sees that Transfer-Encoding header was set, it throws exception immediately.
                  So instead, we suppress addition of Transfer-Encoding header and set entity to be chunked here.
                  RequestContent#process will see that HttpEntity#isChunked returns true and will set correct Transfer-Encoding header.
                */
                if (entity != null) {
                    val chunked = HTTP.CHUNK_CODING == value
                    entity.isChunked = chunked
                }
            } else {
                request.addHeader(key, value)
            }
        }
        return request
    }
}
