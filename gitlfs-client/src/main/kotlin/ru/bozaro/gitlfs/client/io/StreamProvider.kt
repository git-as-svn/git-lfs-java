package ru.bozaro.gitlfs.client.io

import java.io.IOException
import java.io.InputStream

/**
 * Stream provider.
 *
 * @author Artem V. Navrotskiy
 */
interface StreamProvider {
    @get:Throws(IOException::class)
    val stream: InputStream
}
