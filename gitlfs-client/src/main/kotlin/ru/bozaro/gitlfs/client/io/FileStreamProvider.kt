package ru.bozaro.gitlfs.client.io

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

/**
 * Create stream from file.
 *
 * @author Artem V. Navrotskiy
 */
class FileStreamProvider(private val file: File) : StreamProvider {
    @get:Throws(IOException::class)
    override val stream: InputStream
        get() = FileInputStream(file)
}
