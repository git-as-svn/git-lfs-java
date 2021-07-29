package ru.bozaro.gitlfs.client.auth

import ru.bozaro.gitlfs.common.JsonHelper
import ru.bozaro.gitlfs.common.data.Link
import ru.bozaro.gitlfs.common.data.Operation
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.MalformedURLException

/**
 * Get authentication ru.bozaro.gitlfs.common.data from external application.
 * This AuthProvider is EXPERIMENTAL and it can only be used at your own risk.
 *
 * @author Artem V. Navrotskiy
 */
open class ExternalAuthProvider : CachedAuthProvider {
    private val authority: String
    private val path: String

    /**
     * Create authentication wrapper for git-lfs-authenticate command.
     *
     * @param gitUrl Git URL like: git@github.com:bozaro/git-lfs-java.git
     */
    constructor(gitUrl: String) {
        val separator = gitUrl.indexOf(':')
        if (separator < 0) {
            throw MalformedURLException("Can't find separator ':' in gitUrl: $gitUrl")
        }
        authority = gitUrl.substring(0, separator)
        path = gitUrl.substring(separator + 1)
    }

    /**
     * Create authentication wrapper for git-lfs-authenticate command.
     *
     * @param authority SSH server authority with user name
     * @param path      Repostiry path
     */
    constructor(authority: String, path: String) {
        this.authority = authority
        this.path = path
    }

    @Throws(IOException::class, InterruptedException::class)
    override fun getAuthUncached(operation: Operation): Link {
        val builder = ProcessBuilder()
            .command(*getCommand(operation))
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
        val process = builder.start()
        process.outputStream.close()
        val stdoutStream = process.inputStream
        val stdoutData = ByteArrayOutputStream()
        val buffer = ByteArray(0x10000)
        while (true) {
            val read = stdoutStream.read(buffer)
            if (read <= 0) break
            stdoutData.write(buffer, 0, read)
        }
        val exitValue = process.waitFor()
        if (exitValue != 0) {
            throw IOException("Command returned with non-zero exit code " + exitValue + ": " + builder.command().toTypedArray().contentToString())
        }
        return JsonHelper.mapper.readValue(stdoutData.toByteArray(), Link::class.java)
    }

    private fun getCommand(operation: Operation): Array<String> {
        return arrayOf(
            "ssh",
            authority,
            "-oBatchMode=yes",
            "-C",
            "git-lfs-authenticate",
            path,
            operation.toValue()
        )
    }
}
