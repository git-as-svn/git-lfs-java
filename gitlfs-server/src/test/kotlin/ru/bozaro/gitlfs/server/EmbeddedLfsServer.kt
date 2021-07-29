package ru.bozaro.gitlfs.server

import ru.bozaro.gitlfs.client.auth.AuthProvider

/**
 * Embedded LFS server for servlet testing.
 *
 * @author Artem V. Navrotskiy
 */
class EmbeddedLfsServer(val storage: MemoryStorage, lockManager: LockManager?) : AutoCloseable {
    private val server: EmbeddedHttpServer = EmbeddedHttpServer()

    val authProvider: AuthProvider
        get() = storage.getAuthProvider(server.base.resolve("/foo/bar.git/info/lfs"))

    @Throws(Exception::class)
    override fun close() {
        server.close()
    }

    init {
        server.addServlet("/foo/bar.git/info/lfs/objects/*", PointerServlet(storage, "/foo/bar.git/info/lfs/storage/"))
        server.addServlet("/foo/bar.git/info/lfs/storage/*", ContentServlet(storage))
        if (lockManager != null) server.addServlet("/foo/bar.git/info/lfs/locks/*", LocksServlet(lockManager))
    }
}
