package ru.bozaro.gitlfs.client

import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpUriRequest
import java.io.Closeable
import java.io.IOException

/**
 * Abstract class for HTTP connection execution.
 *
 * @author Artem V. Navrotskiy
 */
interface HttpExecutor : Closeable {
    @Throws(IOException::class)
    fun executeMethod(request: HttpUriRequest): CloseableHttpResponse
}
