package ru.bozaro.gitlfs.server;

import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.common.JsonHelper;
import ru.bozaro.gitlfs.common.LockConflictException;
import ru.bozaro.gitlfs.common.VerifyLocksResult;
import ru.bozaro.gitlfs.common.data.*;
import ru.bozaro.gitlfs.server.internal.ObjectResponse;
import ru.bozaro.gitlfs.server.internal.ResponseWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static ru.bozaro.gitlfs.server.PointerServlet.checkMimeTypes;

public class LocksServlet extends HttpServlet {

  @NotNull
  private final LockManager lockManager;

  public LocksServlet(@NotNull LockManager lockManager) {
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

  @NotNull
  private ResponseWriter createLock(@NotNull HttpServletRequest req, @NotNull LockManager.LockWrite lockWrite) throws IOException {
    final CreateLockReq createLockReq = JsonHelper.mapper.readValue(req.getInputStream(), CreateLockReq.class);
    try {
      final Lock lock = lockWrite.lock(createLockReq.getPath(), createLockReq.getRef());
      return new ObjectResponse(HttpServletResponse.SC_CREATED, new CreateLockRes(lock));
    } catch (LockConflictException e) {
      return new ObjectResponse(HttpServletResponse.SC_CONFLICT, new LockConflictRes(e.getMessage(), e.getLock()));
    }
  }

  @NotNull
  private ResponseWriter verifyLocks(@NotNull HttpServletRequest req, @NotNull LockManager.LockWrite lockWrite) throws IOException {
    final VerifyLocksReq verifyLocksReq = JsonHelper.mapper.readValue(req.getInputStream(), VerifyLocksReq.class);
    final VerifyLocksResult result = lockWrite.verifyLocks(verifyLocksReq.getRef());
    return new ObjectResponse(HttpServletResponse.SC_OK, new VerifyLocksRes(result.getOurLocks(), result.getTheirLocks(), null));
  }

  @NotNull
  private ResponseWriter deleteLock(@NotNull HttpServletRequest req, @NotNull LockManager.LockWrite lockWrite, @NotNull String lockId) throws IOException, ServerError {
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

  @NotNull
  private ResponseWriter listLocks(@NotNull HttpServletRequest req, @NotNull LockManager.LockRead lockRead) throws IOException {
    final String refName = req.getParameter("refspec");

    final String path = req.getParameter("path");
    final String lockId = req.getParameter("id");

    final List<Lock> locks = lockRead.getLocks(path, lockId, Ref.create(refName));
    return new ObjectResponse(HttpServletResponse.SC_OK, new LocksRes(locks, null));
  }
}
