package ru.bozaro.gitlfs.server

import com.google.common.io.ByteStreams
import org.testng.Assert
import org.testng.annotations.Test
import ru.bozaro.gitlfs.client.Client
import ru.bozaro.gitlfs.client.Client.Companion.generateMeta
import ru.bozaro.gitlfs.client.io.StringStreamProvider
import ru.bozaro.gitlfs.common.data.BatchReq
import ru.bozaro.gitlfs.common.data.Operation
import java.io.FileNotFoundException
import java.io.InputStream

/**
 * Git LFS server implementation test.
 *
 * @author Artem V. Navrotskiy
 */
class ServerTest {
    @Test
    @Throws(Exception::class)
    fun simpleTest() {
        EmbeddedLfsServer(MemoryStorage(-1), null).use { server ->
            val auth = server.authProvider
            val client = Client(auth)
            val streamProvider = StringStreamProvider("Hello, world")
            val meta = generateMeta(streamProvider)
            // Not uploaded yet.
            try {
                client.getObject(meta.oid) { `in`: InputStream -> ByteStreams.toByteArray(`in`) }
                Assert.fail()
            } catch (ignored: FileNotFoundException) {
            }
            client.postBatch(BatchReq(Operation.Download, listOf(meta)))
            // Can upload.
            Assert.assertTrue(client.putObject(streamProvider, meta))
            // Can download uploaded.
            val content = client.getObject(meta.oid) { `in`: InputStream -> ByteStreams.toByteArray(`in`) }
            Assert.assertEquals(content, ByteStreams.toByteArray(streamProvider.stream))
            // Already uploaded.
            Assert.assertFalse(client.putObject(streamProvider, meta))
        }
    }
}
