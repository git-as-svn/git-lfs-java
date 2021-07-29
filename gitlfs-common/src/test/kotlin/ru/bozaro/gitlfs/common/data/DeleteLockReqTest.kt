package ru.bozaro.gitlfs.common.data

import org.testng.Assert
import org.testng.annotations.Test
import java.io.IOException

class DeleteLockReqTest {
    @Test
    @Throws(IOException::class)
    fun parse01() {
        val data: DeleteLockReq = SerializeTester.deserialize("delete-lock-req-01.json", DeleteLockReq::class.java)
        Assert.assertNotNull(data)
        Assert.assertTrue(data.isForce())
        Assert.assertNotNull(data.ref)
        Assert.assertEquals(data.ref!!.name, "refs/heads/my-feature")
    }
}
