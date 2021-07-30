package ru.bozaro.gitlfs.server

import com.google.common.io.ByteStreams
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import ru.bozaro.gitlfs.client.BatchDownloader
import ru.bozaro.gitlfs.client.BatchSettings
import ru.bozaro.gitlfs.client.BatchUploader
import ru.bozaro.gitlfs.client.Client
import ru.bozaro.gitlfs.client.Client.Companion.generateMeta
import ru.bozaro.gitlfs.client.auth.AuthProvider
import ru.bozaro.gitlfs.client.auth.ExternalAuthProvider
import ru.bozaro.gitlfs.client.io.ByteArrayStreamProvider
import ru.bozaro.gitlfs.common.data.Meta
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.IntStream

/**
 * Simple upload/download test.
 *
 * @author Artem V. Navrotskiy
 */
class BatchTest {
    @DataProvider(name = "batchProvider")
    fun batchProvider(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(-1, BatchSettings(100, 10, 3)),
                arrayOf(42, BatchSettings(100, 10, 3)),
                arrayOf(7, BatchSettings(5, 3, 3))
        )
    }

    @Test(dataProvider = "batchProvider")
    @Throws(Exception::class)
    fun uploadTest(tokenMaxUsage: Int, settings: BatchSettings) {
        val pool = Executors.newFixedThreadPool(4)
        try {
            EmbeddedLfsServer(MemoryStorage(tokenMaxUsage), null).use { server ->
                val auth = server.authProvider
                val uploader = BatchUploader(Client(auth), pool, settings)
                // Upload half ru.bozaro.gitlfs.common.data
                upload(
                        uploader,
                        IntStream
                                .range(0, REQUEST_COUNT)
                                .filter { i: Int -> i % 2 == 0 }
                                .mapToObj { id: Int -> content(id) }
                                .collect(Collectors.toList()),
                        server.storage
                )
                // Upload full ru.bozaro.gitlfs.common.data
                upload(
                        uploader,
                        IntStream
                                .range(0, REQUEST_COUNT)
                                .mapToObj { id: Int -> content(id) }
                                .collect(Collectors.toList()),
                        server.storage
                )
                // Upload none ru.bozaro.gitlfs.common.data
                upload(
                        uploader,
                        IntStream
                                .range(0, REQUEST_COUNT)
                                .mapToObj { id: Int -> content(id) }
                                .collect(Collectors.toList()),
                        server.storage
                )
                Assert.assertTrue(pool.shutdownNow().isEmpty())
            }
        } finally {
            pool.shutdownNow()
        }
    }

    @Throws(Exception::class)
    private fun upload(
            uploader: BatchUploader,
            contents: List<ByteArray>,
            storage: MemoryStorage
    ) {
        // Upload ru.bozaro.gitlfs.common.data
        upload(uploader, contents)
        // Check result
        for (content in contents) {
            val meta = generateMeta(ByteArrayStreamProvider(content))
            Assert.assertNotNull(storage.getMetadata(meta.oid), String(content, StandardCharsets.UTF_8))
        }
    }

    @Throws(Exception::class)
    private fun upload(uploader: BatchUploader, contents: List<ByteArray>) {
        // Upload ru.bozaro.gitlfs.common.data
        val futures: Array<CompletableFuture<Meta>> = contents
                .map { content: ByteArray -> uploader.upload(ByteArrayStreamProvider(content)) }
                .toTypedArray()
        // Wait uploading finished
        CompletableFuture.allOf(*futures)[TIMEOUT.toLong(), TimeUnit.MILLISECONDS]
        // Check future status
        for (future in futures) {
            future.get()
        }
    }

    @Test(dataProvider = "batchProvider")
    @Throws(Exception::class)
    fun downloadTest(tokenMaxUsage: Int, settings: BatchSettings) {
        val pool = Executors.newFixedThreadPool(4)
        try {
            EmbeddedLfsServer(MemoryStorage(tokenMaxUsage), null).use { server ->
                val auth = server.authProvider
                val downloader = BatchDownloader(Client(auth), pool, settings)
                download(
                        downloader,
                        IntStream
                                .range(0, REQUEST_COUNT)
                                .mapToObj { id: Int -> content(id) }
                                .collect(Collectors.toList()),
                        server.storage
                )
                // Add ru.bozaro.gitlfs.common.data to storage
                populate(server.storage, IntStream
                        .range(0, REQUEST_COUNT)
                        .filter { i: Int -> i % 2 == 0 }
                        .mapToObj { id: Int -> content(id) }
                        .collect(Collectors.toList()))
                // Download full ru.bozaro.gitlfs.common.data
                download(
                        downloader,
                        IntStream
                                .range(0, REQUEST_COUNT)
                                .filter { i: Int -> i % 2 == 0 }
                                .mapToObj { id: Int -> content(id) }
                                .collect(Collectors.toList()),
                        server.storage
                )
                // Download half ru.bozaro.gitlfs.common.data
                download(
                        downloader,
                        IntStream
                                .range(0, REQUEST_COUNT)
                                .mapToObj { id: Int -> content(id) }
                                .collect(Collectors.toList()),
                        server.storage
                )
                Assert.assertTrue(pool.shutdownNow().isEmpty())
            }
        } finally {
            pool.shutdownNow()
        }
    }

    @Throws(Exception::class)
    private fun download(
            downloader: BatchDownloader,
            contents: List<ByteArray>,
            storage: MemoryStorage
    ) {
        download(downloader, contents) { meta: Meta -> storage.getObject(meta.oid) != null }
    }

    @Throws(IOException::class)
    private fun populate(storage: MemoryStorage, contents: List<ByteArray>) {
        for (content in contents) {
            storage.saveObject(ByteArrayStreamProvider(content))
        }
    }

    @Throws(Exception::class)
    private fun download(
            downloader: BatchDownloader,
            contents: List<ByteArray>,
            checker: Function<Meta, Boolean>
    ) {
        // Download ru.bozaro.gitlfs.common.data
        val map: MutableMap<Meta, CompletableFuture<ByteArray?>?> = HashMap()
        for (content in contents) {
            val meta = generateMeta(ByteArrayStreamProvider(content))
            map[meta] = downloader.download(meta) { `in`: InputStream -> ByteStreams.toByteArray(`in`) }
        }
        // Check result
        for ((key, value) in map) {
            try {
                val content = value!![TIMEOUT.toLong(), TimeUnit.MILLISECONDS]
                Assert.assertNotNull(content)
                Assert.assertTrue(checker.apply(key))
            } catch (e: ExecutionException) {
                if (e.cause is FileNotFoundException) {
                    Assert.assertFalse(checker.apply(key))
                } else {
                    Assert.fail("Unexpected exception", e.cause)
                }
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun simple() {
        EmbeddedLfsServer(MemoryStorage(-1), null).use { server -> fullCircle(server.authProvider) }
    }

    @Test(enabled = false)
    @Throws(Exception::class)
    fun github() {
        fullCircle(ExternalAuthProvider("git@github.com:bozaro/test.git"))
    }

    @Throws(Exception::class)
    private fun fullCircle(auth: AuthProvider) {
        val pool = Executors.newFixedThreadPool(4)
        try {
            val settings = BatchSettings()
                    .setLimit(10)
            val downloader = BatchDownloader(Client(auth), pool, settings)
            val uploader = BatchUploader(Client(auth), pool, settings)
            val prefix = UUID.randomUUID().toString()
            // Initial upload
            val requestCount = 50
            upload(
                    uploader,
                    IntStream
                            .range(0, requestCount)
                            .filter { i: Int -> i % 2 == 0 }
                            .mapToObj { id: Int -> content(prefix, id) }
                            .collect(Collectors.toList())
            )
            // Download
            download(
                    downloader,
                    IntStream
                            .range(0, requestCount)
                            .mapToObj { id: Int -> content(prefix, id) }
                            .collect(Collectors.toList()),
                    IntStream
                            .range(0, requestCount)
                            .filter { i: Int -> i % 2 == 0 }
                            .mapToObj { id: Int -> content(prefix, id) }
                            .collect(Collectors.toList())
            )
            // Already upload
            upload(
                    uploader,
                    IntStream
                            .range(0, requestCount)
                            .mapToObj { id: Int -> content(prefix, id) }
                            .collect(Collectors.toList())
            )
            Assert.assertTrue(pool.shutdownNow().isEmpty())
        } finally {
            pool.shutdownNow()
        }
    }

    @Throws(Exception::class)
    private fun download(
            downloader: BatchDownloader,
            contents: List<ByteArray>,
            expected: List<ByteArray>
    ) {
        val oids: MutableSet<String> = HashSet()
        for (content in expected) {
            oids.add(generateMeta(ByteArrayStreamProvider(content)).oid)
        }
        download(downloader, contents) { meta: Meta -> oids.contains(meta.oid) }
    }

    companion object {
        private const val REQUEST_COUNT = 1000
        private const val TIMEOUT = 60000

        private fun content(id: Int): ByteArray {
            return content("TEST", id)
        }

        private fun content(prefix: String, id: Int): ByteArray {
            val result = "$prefix $id"
            return result.toByteArray(StandardCharsets.UTF_8)
        }
    }
}
