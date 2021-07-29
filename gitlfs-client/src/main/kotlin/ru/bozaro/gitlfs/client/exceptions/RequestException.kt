package ru.bozaro.gitlfs.client.exceptions

import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpUriRequest
import java.io.IOException

/**
 * Simple HTTP exception.
 *
 * @author Artem V. Navrotskiy
 */
open class RequestException(private val request: HttpUriRequest, private val response: HttpResponse) : IOException() {
    val statusCode: Int
        get() = response.statusLine.statusCode
    override val message: String
        get() {
            val statusLine = response.statusLine
            return request.uri.toString() + " - " + statusLine.statusCode + " (" + statusLine.reasonPhrase + ")"
        }
}
