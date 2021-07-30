package ru.bozaro.gitlfs.server

import com.google.common.collect.ImmutableMap
import com.google.common.hash.Hashing
import com.google.common.io.ByteStreams
import jakarta.servlet.http.HttpServletRequest
import org.testng.Assert
import ru.bozaro.gitlfs.client.Client.Companion.generateMeta
import ru.bozaro.gitlfs.client.auth.AuthProvider
import ru.bozaro.gitlfs.client.auth.CachedAuthProvider
import ru.bozaro.gitlfs.client.io.StreamProvider
import ru.bozaro.gitlfs.common.Constants
import ru.bozaro.gitlfs.common.data.Link
import ru.bozaro.gitlfs.common.data.Meta
import ru.bozaro.gitlfs.common.data.Operation
import ru.bozaro.gitlfs.server.ContentManager.Downloader
import ru.bozaro.gitlfs.server.ContentManager.Uploader
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Simple in-memory storage.
 *
 * @author Artem V. Navrotskiy
 */
class MemoryStorage(private val tokenMaxUsage: Int) : ContentManager {
    private val storage: MutableMap<String?, ByteArray> = ConcurrentHashMap()
    private val tokenId = AtomicInteger(0)

    @Throws(UnauthorizedError::class)
    override fun checkDownloadAccess(request: HttpServletRequest): Downloader {
        if (token != request.getHeader(Constants.HEADER_AUTHORIZATION)) {
            throw UnauthorizedError("Basic realm=\"Test\"")
        }
        return object : Downloader {
            @Throws(IOException::class)
            override fun openObject(hash: String): InputStream {
                val data = storage[hash] ?: throw FileNotFoundException()
                return ByteArrayInputStream(data)
            }

            override fun openObjectGzipped(hash: String): InputStream? {
                return null
            }
        }
    }

    private val token: String
        get() = if (tokenMaxUsage > 0) {
            val token = tokenId.incrementAndGet()
            "Bearer Token-" + token / tokenMaxUsage
        } else {
            "Bearer Token-" + tokenId.get()
        }

    @Throws(UnauthorizedError::class)
    override fun checkUploadAccess(request: HttpServletRequest): Uploader {
        if (token != request.getHeader(Constants.HEADER_AUTHORIZATION)) {
            throw UnauthorizedError("Basic realm=\"Test\"")
        }
        val storage = this
        return object : Uploader {
            override fun saveObject(meta: Meta, content: InputStream) {
                storage.saveObject(meta, content)
            }
        }
    }

    override fun getMetadata(hash: String): Meta? {
        val data = storage[hash]
        return if (data == null) null else Meta(hash, data.size.toLong())
    }

    @Throws(IOException::class)
    fun saveObject(meta: Meta, content: InputStream) {
        val data = ByteStreams.toByteArray(content)
        if (meta.size >= 0) {
            Assert.assertEquals(meta.size, data.size.toLong())
        }
        Assert.assertEquals(meta.oid, Hashing.sha256().hashBytes(data).toString())
        storage[meta.oid] = data
    }

    @Throws(IOException::class)
    fun saveObject(provider: StreamProvider) {
        val meta = generateMeta(provider)
        provider.stream.use { stream -> saveObject(meta, stream) }
    }

    fun getObject(oid: String): ByteArray? {
        return storage[oid]
    }

    fun getAuthProvider(href: URI): AuthProvider {
        return object : CachedAuthProvider() {
            override fun getAuthUncached(operation: Operation): Link {
                return Link(href, ImmutableMap.of(Constants.HEADER_AUTHORIZATION, token), null)
            }
        }
    }
}
