package ru.bozaro.gitlfs.client

import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpUriRequest
import java.io.IOException

/**
 * Client with recording all request.
 *
 * @author Artem V. Navrotskiy
 */
class HttpRecorder(private val executor: HttpExecutor) : HttpExecutor {
    val records = ArrayList<HttpRecord>()

    @Throws(IOException::class)
    override fun executeMethod(request: HttpUriRequest): CloseableHttpResponse {
        val response = executor.executeMethod(request)
        records.add(HttpRecord(request, response))
        return response
    }

    @Throws(IOException::class)
    override fun close() {
        executor.close()
    }
}
