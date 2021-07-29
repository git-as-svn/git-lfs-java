package ru.bozaro.gitlfs.client

import com.google.common.collect.ImmutableMap
import ru.bozaro.gitlfs.client.auth.AuthProvider
import ru.bozaro.gitlfs.common.data.Link
import ru.bozaro.gitlfs.common.data.Operation
import java.net.URI
import java.util.concurrent.atomic.AtomicInteger

/**
 * Fake authenticator.
 *
 * @author Artem V. Navrotskiy
 */
class FakeAuthProvider(private val chunkedUpload: Boolean) : AuthProvider {
    private val lock = Any()

    private val id = AtomicInteger(0)
    private var auth: Array<Link>? = null

    override fun getAuth(operation: Operation): Link {
        synchronized(lock) {
            if (auth == null) {
                auth = createAuth()
            }
            return auth!![operation.ordinal]
        }
    }

    override fun invalidateAuth(operation: Operation, auth: Link) {
        synchronized(lock) {
            if (this.auth != null && (this.auth!![0] == auth || this.auth!![1] == auth)) {
                this.auth = null
            }
        }
    }

    private fun createAuth(): Array<Link> {
        val uri = URI.create("http://gitlfs.local/test.git/info/lfs")
        val headers = ImmutableMap.builder<String, String>()
                .put("Authorization", "RemoteAuth Token-" + id.incrementAndGet())
        val downloadAuth = Link(uri, headers.build(), null)
        if (chunkedUpload) {
            headers.put("Transfer-Encoding", "chunked")
        }
        val uploadAuth = Link(uri, headers.build(), null)
        return arrayOf(downloadAuth, uploadAuth)
    }
}
