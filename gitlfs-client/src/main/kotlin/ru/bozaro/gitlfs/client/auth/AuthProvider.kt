package ru.bozaro.gitlfs.client.auth

import ru.bozaro.gitlfs.common.data.Link
import ru.bozaro.gitlfs.common.data.Operation
import java.io.IOException

/**
 * Authentication provider.
 *
 * @author Artem V. Navrotskiy
 */
interface AuthProvider {
    /**
     * Get auth ru.bozaro.gitlfs.common.data.
     * Auth ru.bozaro.gitlfs.common.data can be cached in this method.
     *
     * @param operation Operation type.
     * @return ru.bozaro.gitlfs.common.data.
     */
    @Throws(IOException::class)
    fun getAuth(operation: Operation): Link

    /**
     * Set auth as expired.
     *
     * @param operation Operation type.
     * @param auth      Expired auth ru.bozaro.gitlfs.common.data.
     */
    fun invalidateAuth(operation: Operation, auth: Link)
}
