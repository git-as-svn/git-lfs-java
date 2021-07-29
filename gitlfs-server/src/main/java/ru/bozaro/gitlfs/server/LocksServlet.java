package ru.bozaro.gitlfs.server;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.bozaro.gitlfs.common.JsonHelper;
import ru.bozaro.gitlfs.common.LockConflictException;
import ru.bozaro.gitlfs.common.VerifyLocksResult;
import ru.bozaro.gitlfs.common.data.*;
import ru.bozaro.gitlfs.server.internal.ObjectResponse;
import ru.bozaro.gitlfs.server.internal.ResponseWriter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

import static ru.bozaro.gitlfs.server.PointerServlet.checkMimeTypes;

public class LocksServlet extends HttpServlet {

  @Nonnull
  private final LockManager lockManager;

  public LocksServlet(@Nonnull LockManager lockManager) {
    this.lockManager = lockManager;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      final LockManager.LockRead lockRead = lockManager.checkDownloadAccess(req);
      if (req.getPathInfo() == null) {
        listLocks(req, lockRead).write(resp);
        return;
      }
    } catch (ServerError e) {
      PointerServlet.sendError(resp, e);
      return;
    }

    super.doGet(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      checkMimeTypes(req);

      final LockManager.LockWrite lockWrite = lockManager.checkUploadAccess(req);
      if (req.getPathInfo() == null) {
        createLock(req, lockWrite).write(resp);
        return;
      } else if ("/verify".equals(req.getPathInfo())) {
        verifyLocks(req, lockWrite).write(resp);
        return;
      } else if (req.getPathInfo().endsWith("/unlock")) {
        deleteLock(req, lockWrite, req.getPathInfo().substring(1, req.getPathInfo().length() - 7)).write(resp);
        return;
      }
    } catch (ServerError e) {
      PointerServlet.sendError(resp, e);
      return;
    }

    super.doPost(req, resp);
  }

  @Nonnull
  private ResponseWriter createLock(@Nonnull HttpServletRequest req, @Nonnull LockManager.LockWrite lockWrite) throws IOException {
    final CreateLockReq createLockReq = JsonHelper.mapper.readValue(req.getInputStream(), CreateLockReq.class);
    try {
      final Lock lock = lockWrite.lock(createLockReq.getPath(), createLockReq.getRef());
      return new ObjectResponse(HttpServletResponse.SC_CREATED, new CreateLockRes(lock));
    } catch (LockConflictException e) {
      return new ObjectResponse(HttpServletResponse.SC_CONFLICT, new LockConflictRes(e.getMessage(), e.getLock()));
    }
  }

  @Nonnull
  private ResponseWriter verifyLocks(@Nonnull HttpServletRequest req, @Nonnull LockManager.LockWrite lockWrite) throws IOException {
    final VerifyLocksReq verifyLocksReq = JsonHelper.mapper.readValue(req.getInputStream(), VerifyLocksReq.class);
    final VerifyLocksResult result = lockWrite.verifyLocks(verifyLocksReq.getRef());
    return new ObjectResponse(HttpServletResponse.SC_OK, new VerifyLocksRes(result.getOurLocks(), result.getTheirLocks(), null));
  }

  @Nonnull
  private ResponseWriter deleteLock(@Nonnull HttpServletRequest req, @Nonnull LockManager.LockWrite lockWrite, @Nonnull String lockId) throws IOException, ServerError {
    final DeleteLockReq deleteLockReq = JsonHelper.mapper.readValue(req.getInputStream(), DeleteLockReq.class);
    try {
      final Lock lock = lockWrite.unlock(lockId, deleteLockReq.isForce(), deleteLockReq.getRef());
      if (lock == null)
        throw new ServerError(HttpServletResponse.SC_NOT_FOUND, String.format("Lock %s not found", lockId));

      return new ObjectResponse(HttpServletResponse.SC_OK, new CreateLockRes(lock));
    } catch (LockConflictException e) {
      return new ObjectResponse(HttpServletResponse.SC_FORBIDDEN, new CreateLockRes(e.getLock()));
    }
  }

  @Nonnull
  private ResponseWriter listLocks(@Nonnull HttpServletRequest req, @Nonnull LockManager.LockRead lockRead) throws IOException {
    final String refName = req.getParameter("refspec");

    final String path = req.getParameter("path");
    final String lockId = req.getParameter("id");

    final List<Lock> locks = lockRead.getLocks(path, lockId, Ref.create(refName));
    return new ObjectResponse(HttpServletResponse.SC_OK, new LocksRes(locks, null));
  }
}
