package ru.bozaro.gitlfs.client.io

import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * Create stream by bytes.
 *
 * @author Artem V. Navrotskiy
 */
open class ByteArrayStreamProvider(private val data: ByteArray) : StreamProvider {
    override val stream: InputStream
        get() = ByteArrayInputStream(data)
}
