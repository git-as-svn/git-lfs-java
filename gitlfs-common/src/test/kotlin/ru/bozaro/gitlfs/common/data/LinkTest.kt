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
 * Test Link deserialization.
 *
 * @author Artem V. Navrotskiy
 */
class LinkTest {
    @Test
    @Throws(IOException::class, ParseException::class, URISyntaxException::class)
    fun parse01() {
        val link: Link = SerializeTester.deserialize("link-01.json", Link::class.java)
        Assert.assertNotNull(link)
        Assert.assertEquals(link.href, URI("https://storage-server.com/OID"))
        Assert.assertEquals(link.header,
                ImmutableMap.builder<Any?, Any?>()
                        .put("Authorization", "Basic ...")
                        .build()
        )
    }

    @Test
    @Throws(IOException::class, ParseException::class, URISyntaxException::class)
    fun parse02() {
        val link: Link = SerializeTester.deserialize("link-02.json", Link::class.java)
        Assert.assertNotNull(link)
        Assert.assertEquals(link.href, URI("https://api.github.com/lfs/bozaro/git-lfs-java"))
        Assert.assertEquals(link.header,
                ImmutableMap.builder<Any?, Any?>()
                        .put("Authorization", "RemoteAuth secret")
                        .build()
        )
        Assert.assertEquals(link.expiresAt, StdDateFormat.instance.parse("2015-09-17T19:17:31.000+00:00"))
    }
}
