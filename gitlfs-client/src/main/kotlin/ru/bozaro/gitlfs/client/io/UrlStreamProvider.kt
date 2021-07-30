package ru.bozaro.gitlfs.client.io

import java.io.IOException
import java.io.InputStream
import java.net.URL

/**
 * Create stream by URL.
 *
 * @author Artem V. Navrotskiy
 */
class UrlStreamProvider(private val url: URL) : StreamProvider {
    @get:Throws(IOException::class)
    override val stream: InputStream
        get() = url.openStream()
}
