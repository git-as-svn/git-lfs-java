package ru.bozaro.gitlfs.server

import jakarta.servlet.http.HttpServletRequest
import ru.bozaro.gitlfs.common.LockConflictException
import ru.bozaro.gitlfs.common.VerifyLocksResult
import ru.bozaro.gitlfs.common.data.Lock
import ru.bozaro.gitlfs.common.data.Ref
import java.io.IOException

interface LockManager {
    @Throws(IOException::class, ForbiddenError::class, UnauthorizedError::class)
    fun checkDownloadAccess(request: HttpServletRequest): LockRead

    @Throws(IOException::class, ForbiddenError::class, UnauthorizedError::class)
    fun checkUploadAccess(request: HttpServletRequest): LockWrite
    interface LockRead {
        @Throws(IOException::class)
        fun getLocks(path: String?, lockId: String?, ref: Ref?): List<Lock>
    }

    interface LockWrite : LockRead {
        @Throws(LockConflictException::class, IOException::class)
        fun lock(path: String, ref: Ref?): Lock

        @Throws(LockConflictException::class, IOException::class)
        fun unlock(lockId: String, force: Boolean, ref: Ref?): Lock?

        @Throws(IOException::class)
        fun verifyLocks(ref: Ref?): VerifyLocksResult
    }
}
