package ru.bozaro.gitlfs.common.io

import ru.bozaro.gitlfs.common.data.Meta
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Wrapper for validating hash and size of uploading object.
 *
 * @author Artem V. Navrotskiy
 */
class InputStreamValidator(stream: InputStream, meta: Meta) : InputStream() {
    private var digest: MessageDigest? = null

    private val stream: InputStream

    private val meta: Meta
    private var eof: Boolean
    private var totalSize: Long

    @Throws(IOException::class)
    override fun read(): Int {
        if (eof) {
            return -1
        }
        val data = stream.read()
        if (data >= 0) {
            digest!!.update(data.toByte())
            checkSize(1)
        } else {
            checkSize(-1)
        }
        return data
    }

    @Throws(IOException::class)
    private fun checkSize(size: Int) {
        if (size > 0) {
            totalSize += size.toLong()
        }
        if (meta.size in 1 until totalSize) {
            throw IOException("Input stream too big")
        }
        if (size < 0) {
            eof = true
            if (meta.size >= 0 && totalSize != meta.size) {
                throw IOException("Unexpected end of stream")
            }
            val hash = toHexString(digest!!.digest())
            if (meta.oid != hash) {
                throw IOException("Invalid stream hash")
            }
        }
    }

    @Throws(IOException::class)
    override fun read(buffer: ByteArray, off: Int, len: Int): Int {
        if (eof) {
            return -1
        }
        val size = stream.read(buffer, off, len)
        if (size > 0) {
            digest!!.update(buffer, off, size)
        }
        checkSize(size)
        return size
    }

    @Throws(IOException::class)
    override fun close() {
        stream.close()
    }

    companion object {
        private val hexDigits = "0123456789abcdef".toCharArray()
        private fun toHexString(bytes: ByteArray): String {
            val sb = StringBuilder(2 * bytes.size)
            for (b in bytes) {
                sb.append(hexDigits[b.toInt() shr 4 and 0xf]).append(hexDigits[b.toInt() and 0xf])
            }
            return sb.toString()
        }
    }

    init {
        try {
            digest = MessageDigest.getInstance("SHA-256")
        } catch (e: NoSuchAlgorithmException) {
            throw IOException(e)
        }
        this.stream = stream
        this.meta = meta
        eof = false
        totalSize = 0
    }
}
