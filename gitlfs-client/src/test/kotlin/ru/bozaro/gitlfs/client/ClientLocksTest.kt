package ru.bozaro.gitlfs.client

import org.testng.Assert
import org.testng.annotations.Test
import ru.bozaro.gitlfs.common.LockConflictException
import ru.bozaro.gitlfs.common.data.Ref
import java.io.IOException

class ClientLocksTest {
    @Test
    @Throws(IOException::class, LockConflictException::class)
    fun simple() {
        val ref = Ref.create("refs/heads/master")
        val replay = YamlHelper.createReplay("/ru/bozaro/gitlfs/client/locking-01.yml")
        val client = Client(FakeAuthProvider(false), replay)
        val lock = client.lock("build.gradle", ref)
        Assert.assertNotNull(lock)
        Assert.assertNotNull(lock.id)
        Assert.assertEquals(lock.path, "build.gradle")
        try {
            client.lock("build.gradle", ref)
            Assert.fail()
        } catch (e: LockConflictException) {
            Assert.assertEquals(e.lock.id, lock.id)
        }
        run {
            val locks = client.listLocks("build.gradle", null, ref)
            Assert.assertEquals(locks.size, 1)
            Assert.assertEquals(locks[0].id, lock.id)
        }
        run {
            val locks = client.verifyLocks(ref)
            Assert.assertEquals(locks.ourLocks.size, 2)
            Assert.assertEquals(locks.ourLocks[1].id, lock.id)
            Assert.assertEquals(locks.theirLocks.size, 0)
        }
        val unlock = client.unlock(lock.id, true, ref)
        Assert.assertNotNull(unlock)
        Assert.assertEquals(unlock!!.id, lock.id)
        Assert.assertNull(client.unlock(lock.id, false, ref))
        replay.close()
    }
}
