package ru.bozaro.gitlfs.pointer

import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.regex.Pattern

/**
 * Class for read/writer pointer blobs.
 * https://github.com/github/git-lfs/blob/master/docs/spec.md
 *
 * @author Artem V. Navrotskiy
 */
object Pointer {
    private val PREFIX = (Constants.VERSION + ' ').toByteArray(StandardCharsets.UTF_8)

    private val REQUIRED = arrayOf(
        RequiredKey(Constants.OID, Pattern.compile("^[0-9a-z]+:[0-9a-f]+$")),
        RequiredKey(Constants.SIZE, Pattern.compile("^\\d+$"))
    )

    /**
     * Serialize pointer map.
     *
     * @param pointer Pointer ru.bozaro.gitlfs.common.data.
     * @return Pointer content bytes.
     */
    fun serializePointer(pointer: Map<String, String>): ByteArray {
        val data: MutableMap<String?, String> = TreeMap(pointer)
        val buffer = StringBuilder()
        // Write version.
        run {
            var version = data.remove(Constants.VERSION)
            if (version == null) {
                version = Constants.VERSION_URL
            }
            buffer.append(Constants.VERSION).append(' ').append(version).append('\n')
        }
        for ((key, value) in data) {
            buffer.append(key).append(' ').append(value).append('\n')
        }
        return buffer.toString().toByteArray(StandardCharsets.UTF_8)
    }

    /**
     * Create pointer with oid and size.
     *
     * @param oid  Object oid.
     * @param size Object size.
     * @return Return pointer ru.bozaro.gitlfs.common.data.
     */
    fun createPointer(oid: String, size: Long): Map<String, String> {
        val pointer = TreeMap<String, String>()
        pointer[Constants.VERSION] = Constants.VERSION_URL
        pointer[Constants.OID] = oid
        pointer[Constants.SIZE] = size.toString()
        return pointer
    }

    /**
     * Read pointer ru.bozaro.gitlfs.common.data.
     *
     * @param stream Input stream.
     * @return Return pointer info or null if blob is not a pointer ru.bozaro.gitlfs.common.data.
     */
    @Throws(IOException::class)
    fun parsePointer(stream: InputStream): Map<String, String>? {
        val buffer = ByteArray(Constants.POINTER_MAX_SIZE)
        var size = 0
        while (size < buffer.size) {
            val len = stream.read(buffer, size, buffer.size - size)
            if (len <= 0) {
                return parsePointer(buffer, 0, size)
            }
            size += len
        }
        return null
    }

    /**
     * Read @{link ru.bozaro.gitlfs.common.data.Pointer}
     *
     * @return Return pointer info or null if blob is not a {@link ru.bozaro.gitlfs.common.data.Pointer}
     */
    fun parsePointer(blob: ByteArray, offset: Int = 0, length: Int = blob.size): Map<String, String>? {
        // Check prefix
        if (length < PREFIX.size) return null
        for (i in PREFIX.indices) {
            if (blob[i] != PREFIX[i]) return null
        }
        // Reading key value pairs
        val result = TreeMap<String, String>()
        val decoder = StandardCharsets.UTF_8.newDecoder()
        var lastKey: String? = null
        var keyOffset = offset
        var required = 0
        while (keyOffset < length) {
            var valueOffset = keyOffset
            // Key
            while (true) {
                valueOffset++
                if (valueOffset < length) {
                    val c = blob[valueOffset]
                    if (c == ' '.code.toByte()) break
                    // Keys MUST only use the characters [a-z] [0-9] . -.
                    if (c >= 'a'.code.toByte() && c <= 'z'.code.toByte()) continue
                    if (c >= '0'.code.toByte() && c <= '9'.code.toByte()) continue
                    if (c == '.'.code.toByte() || c == '-'.code.toByte()) continue
                }
                // Found invalid character.
                return null
            }
            var endOffset = valueOffset
            // Value
            do {
                endOffset++
                if (endOffset >= length) return null
                // Values MUST NOT contain return or newline characters.
            } while (blob[endOffset] != '\n'.code.toByte())
            val key = String(blob, keyOffset, valueOffset - keyOffset, StandardCharsets.UTF_8)
            val value: String = try {
                decoder.decode(ByteBuffer.wrap(blob, valueOffset + 1, endOffset - valueOffset - 1)).toString()
            } catch (e: CharacterCodingException) {
                return null
            }
            if (required < REQUIRED.size && REQUIRED[required].name == key) {
                if (!REQUIRED[required].pattern.matcher(value).matches()) {
                    return null
                }
                required++
            }
            if (keyOffset > offset) {
                if (lastKey != null && key <= lastKey) {
                    return null
                }
                lastKey = key
            }
            if (result.put(key, value) != null) {
                return null
            }
            keyOffset = endOffset + 1
        }
        // Not found all required fields.
        return if (required < REQUIRED.size) {
            null
        } else result
    }

    private class RequiredKey(
        val name: String,
        val pattern: Pattern
    )
}
