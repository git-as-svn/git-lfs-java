package ru.bozaro.gitlfs.common.data

import org.testng.Assert
import org.testng.annotations.Test
import java.io.IOException
import java.net.URISyntaxException
import java.text.ParseException

/**
 * Test Meta deserialization.
 *
 * @author Artem V. Navrotskiy
 */
class BatchReqTest {
    @Test
    @Throws(IOException::class, ParseException::class, URISyntaxException::class)
    fun parse01() {
        val data: BatchReq = SerializeTester.deserialize("batch-req-01.json", BatchReq::class.java)
        Assert.assertNotNull(data)
        Assert.assertEquals(data.operation, Operation.Upload)
        Assert.assertEquals(1, data.objects.size)
        val meta: Meta = data.objects[0]
        Assert.assertNotNull(meta)
        Assert.assertEquals(meta.oid, "1111111")
        Assert.assertEquals(meta.size, 123L)
    }
}
