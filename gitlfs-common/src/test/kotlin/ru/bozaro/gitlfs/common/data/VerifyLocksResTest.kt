package ru.bozaro.gitlfs.common.data

import com.fasterxml.jackson.databind.util.StdDateFormat
import org.testng.Assert
import org.testng.annotations.Test
import java.io.IOException
import java.text.ParseException

class VerifyLocksResTest {
    @Test
    @Throws(IOException::class, ParseException::class)
    fun parse01() {
        val data: VerifyLocksRes = SerializeTester.deserialize("verify-locks-res-01.json", VerifyLocksRes::class.java)
        Assert.assertNotNull(data)
        Assert.assertEquals(data.nextCursor, "optional next ID")
        Assert.assertEquals(data.ours.size, 1)
        val lock = data.ours[0]
        Assert.assertEquals(lock.id, "some-uuid")
        Assert.assertEquals(lock.path, "/path/to/file")
        Assert.assertEquals(lock.lockedAt, StdDateFormat.instance.parse("2016-05-17T15:49:06+00:00"))
        Assert.assertNotNull(lock.owner)
        Assert.assertEquals(lock.owner!!.name, "Jane Doe")
    }
}
