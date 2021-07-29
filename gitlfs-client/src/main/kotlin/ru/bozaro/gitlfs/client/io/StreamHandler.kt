package ru.bozaro.gitlfs.client.io

import java.io.IOException
import java.io.InputStream

/**
 * Interface for handle stream of downloading ru.bozaro.gitlfs.common.data.
 *
 * @author Artem V. Navrotskiy
 */
fun interface StreamHandler<T> {
    @Throws(IOException::class)
    fun accept(inputStream: InputStream): T
}
