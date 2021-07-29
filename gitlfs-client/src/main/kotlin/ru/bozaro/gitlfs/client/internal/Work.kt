package ru.bozaro.gitlfs.client.internal

import ru.bozaro.gitlfs.common.data.Link
import java.io.IOException

/**
 * Work with same auth ru.bozaro.gitlfs.common.data.
 *
 * @author Artem V. Navrotskiy
 */
fun interface Work<R> {
    @Throws(IOException::class)
    fun exec(auth: Link): R
}
