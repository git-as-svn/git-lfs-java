package ru.bozaro.gitlfs.common.data

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
class ObjectResTest {
    @Test
    @Throws(IOException::class, ParseException::class, URISyntaxException::class)
    fun parse01() {
        val res: ObjectRes = SerializeTester.deserialize("object-res-01.json", ObjectRes::class.java)
        Assert.assertNotNull(res)
        val meta = res.meta!!
        Assert.assertNotNull(meta)
        Assert.assertEquals(meta.oid, "01ba4719c80b6fe911b091a7c05124b64eeece964e09c058ef8f9805daca546b")
        Assert.assertEquals(meta.size, 130L)
        Assert.assertEquals(2, res.links.size)
        val self: Link = res.links[LinkType.Self]!!
        Assert.assertNotNull(self)
        Assert.assertEquals(self.href, URI("https://storage-server.com/info/lfs/objects/01ba4719c80b6fe911b091a7c05124b64eeece964e09c058ef8f9805daca546b"))
        Assert.assertTrue(self.header.isEmpty())
        val link: Link = res.links[LinkType.Upload]!!
        Assert.assertNotNull(link)
        Assert.assertEquals(link.href, URI("https://storage-server.com/OID"))
        Assert.assertEquals(link.header,
                ImmutableMap.builder<Any, Any>()
                        .put("Authorization", "Basic ...")
                        .build()
        )
    }

    @Test
    @Throws(IOException::class, ParseException::class, URISyntaxException::class)
    fun parse02() {
        val res: ObjectRes = SerializeTester.deserialize("object-res-02.json", ObjectRes::class.java)
        Assert.assertNotNull(res)
        Assert.assertNull(res.meta)
        Assert.assertEquals(1, res.links.size)
        val link: Link = res.links[LinkType.Upload]!!
        Assert.assertNotNull(link)
        Assert.assertEquals(link.href, URI("https://some-upload.com"))
        Assert.assertEquals(link.header,
                ImmutableMap.builder<Any, Any>()
                        .put("Authorization", "Basic ...")
                        .build()
        )
    }
}
