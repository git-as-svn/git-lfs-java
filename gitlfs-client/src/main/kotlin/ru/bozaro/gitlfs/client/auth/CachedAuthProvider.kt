package ru.bozaro.gitlfs.client.auth

import ru.bozaro.gitlfs.common.data.Link
import ru.bozaro.gitlfs.common.data.Operation
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Get authentication ru.bozaro.gitlfs.common.data from external application.
 * This AuthProvider is EXPERIMENTAL and it can only be used at your own risk.
 *
 * @author Artem V. Navrotskiy
 */
abstract class CachedAuthProvider : AuthProvider {
    private val authCache: ConcurrentMap<Operation?, Link?>

    private val locks: EnumMap<Operation, Any> = createLocks()

    @Throws(IOException::class)
    override fun getAuth(operation: Operation): Link {
        var auth = authCache[operation]
        if (auth == null) {
            synchronized(locks[operation]!!) {
                auth = authCache[operation]
                if (auth == null) {
                    try {
                        auth = getAuthUncached(operation)
                        authCache[operation] = auth
                    } catch (e: InterruptedException) {
                        throw IOException(e)
                    }
                }
            }
        }
        return auth!!
    }

    @Throws(IOException::class, InterruptedException::class)
    protected abstract fun getAuthUncached(operation: Operation): Link

    override fun invalidateAuth(operation: Operation, auth: Link) {
        authCache.remove(operation, auth)
    }

    companion object {
        private fun createLocks(): EnumMap<Operation, Any> {
            val result = EnumMap<Operation, Any>(Operation::class.java)
            for (value in Operation.values()) {
                result[value] = Any()
            }
            return result
        }
    }

    init {
        authCache = ConcurrentHashMap(Operation.values().size)
    }
}
