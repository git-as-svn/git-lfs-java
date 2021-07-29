package ru.bozaro.gitlfs.common.data

import org.testng.Assert
import org.testng.annotations.Test
import java.io.IOException

class CreateLockReqTest {
    @Test
    @Throws(IOException::class)
    fun parse01() {
        val data: CreateLockReq = SerializeTester.deserialize("create-lock-req-01.json", CreateLockReq::class.java)
        Assert.assertNotNull(data)
        Assert.assertEquals(data.path, "foo/bar.zip")
        Assert.assertNotNull(data.ref)
        Assert.assertEquals(data.ref!!.name, "refs/heads/my-feature")
    }
}
