package ru.bozaro.gitlfs.common.data

import com.fasterxml.jackson.databind.util.StdDateFormat
import com.google.common.collect.ImmutableMap
import org.testng.Assert
import org.testng.annotations.Test
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.text.ParseException

/**
 * Test Meta deserialization.
 *
 * @author Artem V. Navrotskiy
 */
class BatchResTest {
    @Test
    @Throws(IOException::class, ParseException::class, URISyntaxException::class)
    fun parse01() {
        val data: BatchRes = SerializeTester.deserialize("batch-res-01.json", BatchRes::class.java)
        Assert.assertNotNull(data)
        Assert.assertEquals(2, data.objects.size)
        run {
            val item: BatchItem = data.objects[0]
            Assert.assertNotNull(item)
            Assert.assertEquals(item.oid, "1111111")
            Assert.assertEquals(item.size, 123L)
            Assert.assertNull(item.error)
            Assert.assertEquals(1, item.links.size)
            val link: Link = item.links[LinkType.Download]!!
            Assert.assertNotNull(link)
            Assert.assertEquals(link.href, URI("https://some-download.com"))
            Assert.assertEquals(
                link.header,
                ImmutableMap.builder<Any, Any>()
                    .put("Authorization", "Basic ...")
                    .build()
            )
            Assert.assertEquals(link.expiresAt, StdDateFormat.instance.parse("2015-07-27T21:15:01.000+00:00"))
        }
        run {
            val item: BatchItem = data.objects[1]
            Assert.assertNotNull(item)
            Assert.assertEquals(item.oid, "2222222")
            Assert.assertEquals(item.size, 234L)
            Assert.assertTrue(item.links.isEmpty())
            val error = item.error!!
            Assert.assertNotNull(error)
            Assert.assertEquals(error.code, 404)
            Assert.assertEquals(error.message, "Object does not exist on the server")
        }
    }
}
