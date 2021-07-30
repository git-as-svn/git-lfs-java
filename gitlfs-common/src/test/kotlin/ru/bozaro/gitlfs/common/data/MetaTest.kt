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
class MetaTest {
    @Test
    @Throws(IOException::class, ParseException::class, URISyntaxException::class)
    fun parse01() {
        val meta: Meta = SerializeTester.deserialize("meta-01.json", Meta::class.java)
        Assert.assertNotNull(meta)
        Assert.assertEquals(meta.oid, "01ba4719c80b6fe911b091a7c05124b64eeece964e09c058ef8f9805daca546b")
        Assert.assertEquals(meta.size, 130L)
    }
}
