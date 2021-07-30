package ru.bozaro.gitlfs.client.internal

import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.CloseableHttpClient
import ru.bozaro.gitlfs.client.HttpExecutor
import java.io.IOException

/**
 * Simple HttpClient wrapper.
 *
 * @author Artem V. Navrotskiy
 */
class HttpClientExecutor(private val http: CloseableHttpClient) : HttpExecutor {
    @Throws(IOException::class)
    override fun executeMethod(request: HttpUriRequest): CloseableHttpResponse {
        return http.execute(request)
    }

    @Throws(IOException::class)
    override fun close() {
        http.close()
    }
}
