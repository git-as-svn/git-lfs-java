package ru.bozaro.gitlfs.client.internal

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import java.io.IOException

/**
 * Single HTTP request.
 *
 * @author Artem V. Navrotskiy
 */
interface Request<R> {
    @Throws(IOException::class)
    fun createRequest(mapper: ObjectMapper, url: String): LfsRequest

    @Throws(IOException::class)
    fun processResponse(mapper: ObjectMapper, response: HttpResponse): R

    /**
     * Success status codes.
     *
     * @return Success status codes.
     */
    fun statusCodes(): IntArray {
        return intArrayOf(HttpStatus.SC_OK)
    }
}
