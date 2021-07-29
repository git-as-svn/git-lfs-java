package ru.bozaro.gitlfs.client

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.codec.binary.Hex
import org.apache.http.HttpStatus
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import ru.bozaro.gitlfs.client.auth.AuthProvider
import ru.bozaro.gitlfs.client.exceptions.ForbiddenException
import ru.bozaro.gitlfs.client.exceptions.RequestException
import ru.bozaro.gitlfs.client.exceptions.UnauthorizedException
import ru.bozaro.gitlfs.client.internal.*
import ru.bozaro.gitlfs.client.io.StreamHandler
import ru.bozaro.gitlfs.client.io.StreamProvider
import ru.bozaro.gitlfs.common.Constants.HEADER_LOCATION
import ru.bozaro.gitlfs.common.Constants.PATH_BATCH
import ru.bozaro.gitlfs.common.Constants.PATH_LOCKS
import ru.bozaro.gitlfs.common.Constants.PATH_OBJECTS
import ru.bozaro.gitlfs.common.JsonHelper
import ru.bozaro.gitlfs.common.LockConflictException
import ru.bozaro.gitlfs.common.VerifyLocksResult
import ru.bozaro.gitlfs.common.data.*
import ru.bozaro.gitlfs.common.io.InputStreamValidator
import java.io.*
import java.net.URI
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Git LFS client.
 *
 * @author Artem V. Navrotskiy
 */
class Client(val authProvider: AuthProvider, private val http: HttpExecutor) : Closeable {
    private val mapper: ObjectMapper = JsonHelper.mapper

    constructor(
        authProvider: AuthProvider,
        http: CloseableHttpClient = HttpClients.createDefault()
    ) : this(authProvider, HttpClientExecutor(http))

    /**
     * Get metadata for object by hash.
     *
     * @param hash Object hash.
     * @return Object metadata or null, if object not found.
     */
    @Throws(IOException::class)
    fun getMeta(hash: String): ObjectRes? {
        return doWork({ auth: Link ->
            doRequest(
                auth,
                MetaGet(),
                AuthHelper.join(auth.href, "$PATH_OBJECTS/", hash),
                ConnectionClosePolicy.Close
            )
        }, Operation.Download)
    }

    @Throws(IOException::class)
    private fun <T> doWork(work: Work<T>, operation: Operation): T {
        var auth = authProvider.getAuth(operation)
        var authCount = 0
        while (true) {
            auth = try {
                return work.exec(auth)
            } catch (e: UnauthorizedException) {
                if (authCount >= MAX_AUTH_COUNT) {
                    throw e
                }
                authCount++
                // Get new authentication ru.bozaro.gitlfs.common.data.
                authProvider.invalidateAuth(operation, auth)
                val newAuth = authProvider.getAuth(operation)
                if (newAuth.header == auth.header && newAuth.href == auth.href) {
                    throw e
                }
                newAuth
            } catch (e: ForbiddenException) {
                if (authCount >= MAX_AUTH_COUNT) {
                    throw e
                }
                authCount++
                authProvider.invalidateAuth(operation, auth)
                val newAuth = authProvider.getAuth(operation)
                if (newAuth.header == auth.header && newAuth.href == auth.href) {
                    throw e
                }
                newAuth
            }
        }
    }

    @Throws(IOException::class)
    fun <R> doRequest(
        link: Link?,
        task: Request<R>,
        url: URI,
        autoClose: ConnectionClosePolicy
    ): R {
        var url = url
        var redirectCount = 0
        var retryCount = 0
        while (true) {
            val lfsRequest = task.createRequest(mapper, url.toString())
            val request = lfsRequest.addHeaders(link?.header ?: emptyMap())
            val response = http.executeMethod(request)
            var needClose = true
            try {
                val success = task.statusCodes()
                for (item in success) {
                    if (response.statusLine.statusCode == item) {
                        if (autoClose == ConnectionClosePolicy.DoNotClose) needClose = false
                        return task.processResponse(mapper, response)
                    }
                }
                when (response.statusLine.statusCode) {
                    HttpStatus.SC_UNAUTHORIZED -> throw UnauthorizedException(request, response)
                    HttpStatus.SC_FORBIDDEN -> throw ForbiddenException(request, response)
                    HttpStatus.SC_MOVED_PERMANENTLY, HttpStatus.SC_MOVED_TEMPORARILY, HttpStatus.SC_SEE_OTHER, HttpStatus.SC_TEMPORARY_REDIRECT -> {
                        // Follow by redirect.
                        val location = response.getFirstHeader(HEADER_LOCATION).value
                        if (location == null || redirectCount >= MAX_REDIRECT_COUNT) {
                            throw RequestException(request, response)
                        }
                        ++redirectCount
                        url = url.resolve(location)
                        continue
                    }
                    HttpStatus.SC_BAD_GATEWAY, HttpStatus.SC_GATEWAY_TIMEOUT, HttpStatus.SC_SERVICE_UNAVAILABLE, HttpStatus.SC_INTERNAL_SERVER_ERROR -> {
                        // Temporary error. need to retry.
                        if (retryCount >= MAX_RETRY_COUNT) {
                            throw RequestException(request, response)
                        }
                        ++retryCount
                        continue
                    }
                }
                throw RequestException(request, response)
            } finally {
                if (needClose) response.close()
            }
        }
    }

    /**
     * Upload object with specified hash and size.
     *
     * @param streamProvider Object stream provider.
     * @param hash           Object hash.
     * @param size           Object size.
     * @return Return true is object is uploaded successfully and false if object is already uploaded.
     * @throws IOException On some errors.
     */
    @Throws(IOException::class)
    fun putObject(streamProvider: StreamProvider, hash: String, size: Long): Boolean {
        return putObject(streamProvider, Meta(hash, size))
    }

    /**
     * Upload object.
     *
     * @param streamProvider Object stream provider.
     * @return Return true is object is uploaded successfully and false if object is already uploaded.
     * @throws IOException On some errors.
     */
    @Throws(IOException::class)
    fun putObject(
        streamProvider: StreamProvider,
        meta: Meta = generateMeta(streamProvider)
    ): Boolean {
        return doWork({ auth: Link ->
            val links =
                doRequest(auth, MetaPost(meta), AuthHelper.join(auth.href, PATH_OBJECTS), ConnectionClosePolicy.Close)
            links != null && putObject(streamProvider, meta, links)
        }, Operation.Upload)
    }

    /**
     * Upload object by metadata.
     *
     * @param links          Object links.
     * @param streamProvider Object stream provider.
     * @param meta           Object metadata.
     * @return Return true is object is uploaded successfully and false if object is already uploaded.
     * @throws IOException On some errors.
     */
    @Throws(IOException::class)
    fun putObject(streamProvider: StreamProvider, meta: Meta, links: Links): Boolean {
        val uploadLink = links.links[LinkType.Upload] ?: return false
        doRequest(uploadLink, ObjectPut(streamProvider, meta.size), uploadLink.href, ConnectionClosePolicy.Close)
        val verifyLink = links.links[LinkType.Verify]
        if (verifyLink != null) doRequest(verifyLink, ObjectVerify(meta), verifyLink.href, ConnectionClosePolicy.Close)
        return true
    }

    /**
     * Get metadata for object by hash.
     *
     * @param hash Object hash.
     * @param size Object size.
     * @return Object metadata or null, if object not found.
     */
    @Throws(IOException::class)
    fun postMeta(hash: String, size: Long): ObjectRes? {
        return postMeta(Meta(hash, size))
    }

    /**
     * Get metadata for object by hash.
     *
     * @param meta Object meta.
     * @return Object metadata or null, if object not found.
     */
    @Throws(IOException::class)
    fun postMeta(meta: Meta): ObjectRes? {
        return doWork(
            { auth: Link ->
                doRequest(
                    auth,
                    MetaPost(meta),
                    AuthHelper.join(auth.href, PATH_OBJECTS),
                    ConnectionClosePolicy.Close
                )
            },
            Operation.Upload
        )
    }

    /**
     * Send batch request to the LFS-server.
     *
     * @param batchReq Batch request.
     * @return Object metadata.
     */
    @Throws(IOException::class)
    fun postBatch(batchReq: BatchReq): BatchRes {
        return doWork(
            { auth: Link ->
                doRequest(
                    auth,
                    JsonPost(batchReq, BatchRes::class.java),
                    AuthHelper.join(auth.href, PATH_BATCH),
                    ConnectionClosePolicy.Close
                )
            },
            batchReq.operation
        )
    }

    /**
     * Download object by hash.
     *
     * @param hash    Object hash.
     * @param handler Stream handler.
     * @return Stream handler result.
     * @throws FileNotFoundException File not found exception if object don't exists on LFS server.
     * @throws IOException           On some errors.
     */
    @Throws(IOException::class)
    fun <T> getObject(hash: String, handler: StreamHandler<T>): T {
        return doWork({ auth: Link ->
            val links = getLinks(hash, auth)
            getObject(if (links.meta == null) Meta(hash, -1) else links.meta, links, handler)
        }, Operation.Download)
    }

    @Throws(IOException::class)
    private fun getLinks(hash: String, auth: Link): ObjectRes {
        return doRequest(
            auth,
            MetaGet(),
            AuthHelper.join(auth.href, "$PATH_OBJECTS/", hash),
            ConnectionClosePolicy.Close
        ) ?: throw FileNotFoundException()
    }

    /**
     * Download object by metadata.
     *
     * @param meta    Object metadata for stream validation.
     * @param links   Object links.
     * @param handler Stream handler.
     * @return Stream handler result.
     * @throws FileNotFoundException File not found exception if object don't exists on LFS server.
     * @throws IOException           On some errors.
     */
    @Throws(IOException::class)
    fun <T> getObject(meta: Meta?, links: Links, handler: StreamHandler<T>): T {
        val link = links.links[LinkType.Download] ?: throw FileNotFoundException()
        return doRequest(
            link,
            ObjectGet { inputStream: InputStream ->
                handler.accept(
                    if (meta == null) inputStream else InputStreamValidator(
                        inputStream,
                        meta
                    )
                )
            },
            link.href,
            ConnectionClosePolicy.Close
        )
    }

    @Throws(IOException::class)
    fun openObject(hash: String): InputStream {
        return doWork({ auth: Link ->
            val links = getLinks(hash, auth)
            openObject(if (links.meta == null) Meta(hash, -1) else links.meta, links)
        }, Operation.Download)
    }

    @Throws(IOException::class)
    fun openObject(meta: Meta?, links: Links): InputStream {
        val link = links.links[LinkType.Download] ?: throw FileNotFoundException()
        return doRequest(
            link,
            ObjectGet { inputStream: InputStream? ->
                (if (meta == null) inputStream else InputStreamValidator(
                    inputStream!!,
                    meta
                ))!!
            },
            link.href,
            ConnectionClosePolicy.DoNotClose
        )
    }

    @Throws(IOException::class, LockConflictException::class)
    fun lock(path: String, ref: Ref?): Lock {
        val res = doWork(
            { auth: Link ->
                doRequest(
                    auth,
                    LockCreate(path, ref),
                    AuthHelper.join(auth.href, PATH_LOCKS),
                    ConnectionClosePolicy.Close
                )
            },
            Operation.Upload
        )
        return if (res.isSuccess) res.lock else throw LockConflictException(res.message, res.lock)
    }

    @Throws(IOException::class)
    fun unlock(lock: Lock, force: Boolean, ref: Ref?): Lock? {
        return unlock(lock.id, force, ref)
    }

    @Throws(IOException::class)
    fun unlock(lockId: String, force: Boolean, ref: Ref?): Lock? {
        return doWork(
            { auth: Link ->
                doRequest(
                    auth,
                    LockDelete(force, ref),
                    AuthHelper.join(auth.href, "$PATH_LOCKS/", "$lockId/unlock"),
                    ConnectionClosePolicy.Close
                )
            },
            Operation.Upload
        )
    }

    @Throws(IOException::class)
    fun listLocks(path: String?, id: String?, ref: Ref?): List<Lock> {
        val result: MutableList<Lock> = ArrayList()
        val baseParams = StringBuffer()
        appendOptionalParam(baseParams, "path", path)
        appendOptionalParam(baseParams, "id", id)
        if (ref != null) appendOptionalParam(baseParams, "refspec", ref.name)
        var cursor: String? = null
        do {
            val cursorFinal = cursor
            val params = StringBuffer(baseParams)
            appendOptionalParam(params, "cursor", cursorFinal)
            val res = doWork(
                { auth: Link ->
                    doRequest(
                        auth,
                        LocksList(),
                        AuthHelper.join(auth.href, PATH_LOCKS + params),
                        ConnectionClosePolicy.Close
                    )
                },
                Operation.Download
            )
            result.addAll(res.locks)
            cursor = res.nextCursor
        } while (cursor != null && cursor.isNotEmpty())
        return result
    }

    @Throws(IOException::class)
    fun verifyLocks(ref: Ref?): VerifyLocksResult {
        val ourLocks = ArrayList<Lock>()
        val theirLocks = ArrayList<Lock>()
        var cursor: String? = null
        do {
            val cursorFinal = cursor
            val res = doWork(
                { auth: Link ->
                    doRequest(
                        auth,
                        JsonPost(VerifyLocksReq(cursorFinal, ref, null), VerifyLocksRes::class.java),
                        AuthHelper.join(auth.href, "$PATH_LOCKS/verify"),
                        ConnectionClosePolicy.Close
                    )
                },
                Operation.Upload
            )
            ourLocks.addAll(res.ours)
            theirLocks.addAll(res.theirs)
            cursor = res.nextCursor
        } while (cursor != null && cursor.isNotEmpty())
        return VerifyLocksResult(ourLocks, theirLocks)
    }

    @Throws(IOException::class)
    override fun close() {
        http.close()
    }

    enum class ConnectionClosePolicy {
        Close, DoNotClose
    }

    companion object {
        private const val MAX_AUTH_COUNT = 1
        private const val MAX_RETRY_COUNT = 2
        private const val MAX_REDIRECT_COUNT = 5

        /**
         * Generate object metadata.
         *
         * @param streamProvider Object stream provider.
         * @return Return object metadata.
         * @throws IOException On some errors.
         */
        @Throws(IOException::class)
        fun generateMeta(streamProvider: StreamProvider): Meta {
            val digest = sha256()
            val buffer = ByteArray(0x10000)
            var size: Long = 0
            streamProvider.stream.use { stream ->
                while (true) {
                    val read = stream.read(buffer)
                    if (read <= 0) break
                    digest.update(buffer, 0, read)
                    size += read.toLong()
                }
            }
            return Meta(String(Hex.encodeHex(digest.digest())), size)
        }

        private fun sha256(): MessageDigest {
            return try {
                MessageDigest.getInstance("SHA-256")
            } catch (e: NoSuchAlgorithmException) {
                throw IllegalStateException(e)
            }
        }

        @Throws(UnsupportedEncodingException::class)
        private fun appendOptionalParam(
            buffer: StringBuffer,
            paramName: String,
            paramValue: String?
        ) {
            if (paramValue != null) {
                buffer
                    .append(if (buffer.isEmpty()) '?' else '&')
                    .append(paramName)
                    .append('=')
                    .append(URLEncoder.encode(paramValue, "UTF-8"))
            }
        }
    }

}
