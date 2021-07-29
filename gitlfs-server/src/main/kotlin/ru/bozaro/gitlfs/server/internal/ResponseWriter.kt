package ru.bozaro.gitlfs.server.internal

import jakarta.servlet.http.HttpServletResponse
import java.io.IOException

/**
 * HTTP response writer.
 *
 * @author Artem V. Navrotskiy
 */
fun interface ResponseWriter {
    @Throws(IOException::class)
    fun write(response: HttpServletResponse)
}
