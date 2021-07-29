package ru.bozaro.gitlfs.client

import com.google.common.base.Utf8
import com.google.common.io.BaseEncoding
import org.apache.http.HttpEntityEnclosingRequest
import org.apache.http.HttpResponse
import org.apache.http.ProtocolVersion
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.message.BasicHttpResponse
import org.apache.http.protocol.HTTP
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * HTTP request-response pair for testing.
 *
 * @author Artem V. Navrotskiy
 */
class HttpRecord {
    val request: Request

    val response: Response

    constructor(request: HttpUriRequest, response: HttpResponse) {
        this.request = Request(request)
        this.response = Response(response)
    }

    private constructor() {
        request = Request()
        response = Response()
    }

    class Response {
        private val statusCode: Int
        private val statusText: String
        private val headers: TreeMap<String, String>
        private val body: ByteArray?

        internal constructor() {
            statusCode = 0
            statusText = ""
            headers = TreeMap()
            body = null
        }

        internal constructor(response: HttpResponse) {
            statusCode = response.statusLine.statusCode
            statusText = response.statusLine.reasonPhrase
            headers = TreeMap()
            for (header in response.allHeaders) {
                headers[header.name] = header.value
            }
            ByteArrayOutputStream().use { stream ->
                response.entity.writeTo(stream)
                body = stream.toByteArray()
                response.entity = ByteArrayEntity(body)
            }
        }

        fun toHttpResponse(): CloseableHttpResponse {
            val response = CloseableBasicHttpResponse(ProtocolVersion("HTTP", 1, 0), statusCode, statusText)
            for ((key, value) in headers) response.addHeader(key, value)
            if (body != null) response.entity = ByteArrayEntity(body)
            return response
        }

        override fun toString(): String {
            val sb = StringBuilder()
            sb.append("HTTP/1.0 ").append(statusCode).append(" ").append(statusText).append("\n")
            for ((key, value) in headers) {
                sb.append(key).append(": ").append(value).append("\n")
            }
            if (body != null) {
                sb.append("\n").append(asString(body))
            }
            return sb.toString()
        }
    }

    private class CloseableBasicHttpResponse(
        ver: ProtocolVersion,
        code: Int,
        reason: String
    ) : BasicHttpResponse(ver, code, reason), CloseableHttpResponse {
        override fun close() {
            // noop
        }
    }

    class Request {
        private val href: String
        private val method: String
        private val headers: TreeMap<String, String>
        private val body: ByteArray?

        internal constructor() {
            href = ""
            method = ""
            headers = TreeMap()
            body = null
        }

        internal constructor(request: HttpUriRequest) {
            href = request.uri.toString()
            method = request.method
            headers = TreeMap()
            val entityRequest = if (request is HttpEntityEnclosingRequest) request else null
            val entity = entityRequest?.entity
            if (entity != null) {
                if (entity.isChunked || entity.contentLength < 0) {
                    request.addHeader(HTTP.TRANSFER_ENCODING, HTTP.CHUNK_CODING)
                } else {
                    request.addHeader(HTTP.CONTENT_LEN, entity.contentLength.toString())
                }
                val contentType = entity.contentType
                if (contentType != null) {
                    headers[contentType.name] = contentType.value
                }
                ByteArrayOutputStream().use { buffer ->
                    entity.writeTo(buffer)
                    body = buffer.toByteArray()
                }
                entityRequest.entity = ByteArrayEntity(body)
            } else {
                body = null
            }
            for (header in request.allHeaders) {
                headers[header.name] = header.value
            }
            headers.remove(HTTP.TARGET_HOST)
            headers.remove(HTTP.USER_AGENT)
        }

        override fun toString(): String {
            val sb = StringBuilder()
            sb.append(method).append(" ").append(href).append("\n")
            for ((key, value) in headers) {
                sb.append(key).append(": ").append(value).append("\n")
            }
            if (body != null) {
                sb.append("\n").append(asString(body))
            }
            return sb.toString()
        }
    }

    companion object {
        private fun asString(data: ByteArray): String {
            return if (Utf8.isWellFormed(data)) {
                String(data, StandardCharsets.UTF_8)
            } else {
                BaseEncoding.base16().encode(data)
            }
        }
    }
}
