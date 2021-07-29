package ru.bozaro.gitlfs.client

import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpUriRequest
import org.testng.Assert
import java.io.IOException
import java.util.*

/**
 * Replay recorded HTTP requests.
 *
 * @author Artem V. Navrotskiy
 */
class HttpReplay(records: List<HttpRecord>) : HttpExecutor {
    private val records: Deque<HttpRecord>

    @Throws(IOException::class)
    override fun executeMethod(request: HttpUriRequest): CloseableHttpResponse {
        val record = records.pollFirst()
        Assert.assertNotNull(record)
        val expected = record.request.toString()
        val actual = HttpRecord.Request(request).toString()
        Assert.assertEquals(actual, expected)
        return record.response.toHttpResponse()
    }

    override fun close() {
        Assert.assertTrue(records.isEmpty())
    }

    init {
        this.records = ArrayDeque(records)
    }
}
