package ru.bozaro.gitlfs.server

import org.testng.Assert
import org.testng.annotations.Test
import ru.bozaro.gitlfs.client.Client
import ru.bozaro.gitlfs.common.LockConflictException
import ru.bozaro.gitlfs.common.data.Ref

class LocksTest {
    @Test
    @Throws(Exception::class)
    fun simple() {
        val storage = MemoryStorage(-1)
        EmbeddedLfsServer(storage, MemoryLockManager(storage)).use { server ->
            val auth = server.authProvider
            val client = Client(auth)
            val ref = Ref.create("ref/heads/master")
            val lock = client.lock("qwe", ref)
            Assert.assertNotNull(lock)
            try {
                client.lock("qwe", ref)
                Assert.fail()
            } catch (e: LockConflictException) {
                Assert.assertEquals(lock.id, e.lock.id)
            }
            run {
                val locks = client.listLocks("qwe", null, ref)
                Assert.assertEquals(locks.size, 1)
                Assert.assertEquals(locks[0].id, lock.id)
            }
            run {
                val locks = client.verifyLocks(ref)
                Assert.assertEquals(locks.ourLocks.size, 1)
                Assert.assertEquals(locks.ourLocks[0].id, lock.id)
                Assert.assertEquals(locks.theirLocks.size, 0)
            }
            val unlock = client.unlock(lock.id, true, ref)
            Assert.assertNotNull(unlock)
            Assert.assertEquals(unlock!!.id, lock.id)
            Assert.assertNull(client.unlock(lock.id, false, ref))
        }
    }
}
