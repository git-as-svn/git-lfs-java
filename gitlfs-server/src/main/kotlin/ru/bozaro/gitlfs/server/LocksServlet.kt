package ru.bozaro.gitlfs.server

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import ru.bozaro.gitlfs.common.JsonHelper
import ru.bozaro.gitlfs.common.LockConflictException
import ru.bozaro.gitlfs.common.data.*
import ru.bozaro.gitlfs.server.LockManager.LockRead
import ru.bozaro.gitlfs.server.LockManager.LockWrite
import ru.bozaro.gitlfs.server.internal.ObjectResponse
import ru.bozaro.gitlfs.server.internal.ResponseWriter
import java.io.IOException

class LocksServlet(private val lockManager: LockManager) : HttpServlet() {
    @Throws(ServletException::class, IOException::class)
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        try {
            val lockRead = lockManager.checkDownloadAccess(req)
            if (req.pathInfo == null) {
                listLocks(req, lockRead).write(resp)
                return
            }
        } catch (e: ServerError) {
            PointerServlet.sendError(resp, e)
            return
        }
        super.doGet(req, resp)
    }

    @Throws(ServletException::class, IOException::class)
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        try {
            PointerServlet.checkMimeTypes(req)
            val lockWrite = lockManager.checkUploadAccess(req)
            when {
                req.pathInfo == null -> {
                    createLock(req, lockWrite).write(resp)
                    return
                }
                "/verify" == req.pathInfo -> {
                    verifyLocks(req, lockWrite).write(resp)
                    return
                }
                req.pathInfo.endsWith("/unlock") -> {
                    deleteLock(req, lockWrite, req.pathInfo.substring(1, req.pathInfo.length - 7)).write(resp)
                    return
                }
            }
        } catch (e: ServerError) {
            PointerServlet.sendError(resp, e)
            return
        }
        super.doPost(req, resp)
    }

    @Throws(IOException::class)
    private fun createLock(req: HttpServletRequest, lockWrite: LockWrite): ResponseWriter {
        val createLockReq = JsonHelper.mapper.readValue(req.inputStream, CreateLockReq::class.java)
        return try {
            val lock = lockWrite.lock(createLockReq.path, createLockReq.ref)
            ObjectResponse(HttpServletResponse.SC_CREATED, CreateLockRes(lock))
        } catch (e: LockConflictException) {
            ObjectResponse(HttpServletResponse.SC_CONFLICT, LockConflictRes(e.message!!, e.lock))
        }
    }

    @Throws(IOException::class)
    private fun verifyLocks(req: HttpServletRequest, lockWrite: LockWrite): ResponseWriter {
        val verifyLocksReq = JsonHelper.mapper.readValue(req.inputStream, VerifyLocksReq::class.java)
        val result = lockWrite.verifyLocks(verifyLocksReq.ref)
        return ObjectResponse(HttpServletResponse.SC_OK, VerifyLocksRes(result.ourLocks, result.theirLocks, null))
    }

    @Throws(IOException::class, ServerError::class)
    private fun deleteLock(
            req: HttpServletRequest,
            lockWrite: LockWrite,
            lockId: String
    ): ResponseWriter {
        val deleteLockReq = JsonHelper.mapper.readValue(req.inputStream, DeleteLockReq::class.java)
        return try {
            val lock = lockWrite.unlock(lockId, deleteLockReq.isForce(), deleteLockReq.ref)
                    ?: throw ServerError(HttpServletResponse.SC_NOT_FOUND, String.format("Lock %s not found", lockId))
            ObjectResponse(HttpServletResponse.SC_OK, CreateLockRes(lock))
        } catch (e: LockConflictException) {
            ObjectResponse(HttpServletResponse.SC_FORBIDDEN, CreateLockRes(e.lock))
        }
    }

    @Throws(IOException::class)
    private fun listLocks(req: HttpServletRequest, lockRead: LockRead): ResponseWriter {
        val refName = req.getParameter("refspec")
        val path = req.getParameter("path")
        val lockId = req.getParameter("id")
        val locks = lockRead.getLocks(path, lockId, Ref.create(refName))
        return ObjectResponse(HttpServletResponse.SC_OK, LocksRes(locks, null))
    }
}
