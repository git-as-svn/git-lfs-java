package ru.bozaro.gitlfs.server

import com.google.common.base.Strings
import jakarta.servlet.http.HttpServletRequest
import ru.bozaro.gitlfs.common.LockConflictException
import ru.bozaro.gitlfs.common.VerifyLocksResult
import ru.bozaro.gitlfs.common.data.Lock
import ru.bozaro.gitlfs.common.data.Ref
import ru.bozaro.gitlfs.common.data.User
import ru.bozaro.gitlfs.server.LockManager.LockRead
import ru.bozaro.gitlfs.server.LockManager.LockWrite
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors

class MemoryLockManager(private val contentManager: ContentManager) : LockManager, LockWrite {
    private val nextId = AtomicInteger(1)
    private val locks = ArrayList<Lock>()

    @Throws(IOException::class, ForbiddenError::class, UnauthorizedError::class)
    override fun checkDownloadAccess(request: HttpServletRequest): LockRead {
        contentManager.checkDownloadAccess(request)
        return this
    }

    @Throws(IOException::class, ForbiddenError::class, UnauthorizedError::class)
    override fun checkUploadAccess(request: HttpServletRequest): LockWrite {
        contentManager.checkUploadAccess(request)
        return this
    }

    override fun getLocks(path: String?, lockId: String?, ref: Ref?): List<Lock> {
        var stream = locks.stream()
        if (!Strings.isNullOrEmpty(path)) stream = stream.filter { lock: Lock -> lock.path == path }
        if (!Strings.isNullOrEmpty(lockId)) stream = stream.filter { lock: Lock -> lock.id == lockId }
        return stream.collect(Collectors.toList())
    }

    @Throws(LockConflictException::class)
    override fun lock(path: String, ref: Ref?): Lock {
        for (lock in locks) if (lock.path == path) throw LockConflictException(lock)
        val lock = Lock(nextId.incrementAndGet().toString(), path, Date(), User("Jane Doe"))
        locks.add(lock)
        return lock
    }

    override fun unlock(lockId: String, force: Boolean, ref: Ref?): Lock? {
        var lock: Lock? = null
        for (l in locks) {
            if (l.id == lockId) {
                lock = l
                break
            }
        }
        if (lock == null) return null
        locks.remove(lock)
        return lock
    }

    override fun verifyLocks(ref: Ref?): VerifyLocksResult {
        return VerifyLocksResult(locks, emptyList())
    }
}
