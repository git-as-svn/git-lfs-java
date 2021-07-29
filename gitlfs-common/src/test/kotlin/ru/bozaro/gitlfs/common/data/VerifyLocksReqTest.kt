package ru.bozaro.gitlfs.common.data

import org.testng.Assert
import org.testng.annotations.Test
import java.io.IOException

class VerifyLocksReqTest {
    @Test
    @Throws(IOException::class)
    fun parse01() {
        val data: VerifyLocksReq = SerializeTester.deserialize("verify-locks-req-01.json", VerifyLocksReq::class.java)
        Assert.assertNotNull(data)
        Assert.assertEquals(data.cursor, "optional cursor")
        Assert.assertEquals(100, data.limit)
        Assert.assertNotNull(data.ref)
        Assert.assertEquals(data.ref!!.name, "refs/heads/my-feature")
    }
}
