package ru.bozaro.gitlfs.client

import com.google.common.io.ByteStreams
import org.testng.Assert
import org.testng.annotations.Test
import ru.bozaro.gitlfs.client.exceptions.ForbiddenException
import ru.bozaro.gitlfs.client.io.StringStreamProvider
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets

/**
 * Replay tests for https://github.com/github/git-lfs/blob/master/docs/api/http-v1-original.md
 *
 * @author Artem V. Navrotskiy
 */
class ClientLegacyTest {
    /**
     * Simple upload.
     */
    @Test
    @Throws(IOException::class)
    fun legacyUpload01() {
        val replay = YamlHelper.createReplay("/ru/bozaro/gitlfs/client/legacy-upload-01.yml")
        val client = Client(FakeAuthProvider(false), replay)
        Assert.assertTrue(client.putObject(StringStreamProvider("Fri Oct 02 21:07:33 MSK 2015")))
        replay.close()
    }

    /**
     * Forbidden.
     */
    @Test(expectedExceptions = [ForbiddenException::class])
    @Throws(IOException::class)
    fun legacyUpload02() {
        val replay = YamlHelper.createReplay("/ru/bozaro/gitlfs/client/legacy-upload-02.yml")
        val client = Client(FakeAuthProvider(false), replay)
        Assert.assertFalse(client.putObject(StringStreamProvider("Fri Oct 02 21:07:33 MSK 2015")))
        replay.close()
    }

    /**
     * Expired token.
     */
    @Test
    @Throws(IOException::class)
    fun legacyUpload03() {
        val replay = YamlHelper.createReplay("/ru/bozaro/gitlfs/client/legacy-upload-03.yml")
        val client = Client(FakeAuthProvider(false), replay)
        Assert.assertTrue(client.putObject(StringStreamProvider("Fri Oct 02 21:07:33 MSK 2015")))
        replay.close()
    }

    /**
     * Already uploaded,
     */
    @Test
    @Throws(IOException::class)
    fun legacyUpload04() {
        val replay = YamlHelper.createReplay("/ru/bozaro/gitlfs/client/legacy-upload-04.yml")
        val client = Client(FakeAuthProvider(false), replay)
        Assert.assertFalse(client.putObject(StringStreamProvider("Hello, world!!!")))
        replay.close()
    }

    /**
     * Simple download
     */
    @Test
    @Throws(IOException::class)
    fun legacyDownload01() {
        val replay = YamlHelper.createReplay("/ru/bozaro/gitlfs/client/legacy-download-01.yml")
        val client = Client(FakeAuthProvider(false), replay)
        val data =
                client.getObject("b810bbe954d51e380f395de0c301a0a42d16f115453f2feb4188ca9f7189074e") { `in`: InputStream ->
                    ByteStreams.toByteArray(`in`)
                }
        Assert.assertEquals(String(data, StandardCharsets.UTF_8), "Fri Oct 02 21:07:33 MSK 2015")
        replay.close()
    }

    /**
     * Download not uploaded object
     */
    @Test
    @Throws(IOException::class)
    fun legacyDownload02() {
        val replay = YamlHelper.createReplay("/ru/bozaro/gitlfs/client/legacy-download-02.yml")
        val client = Client(FakeAuthProvider(false), replay)
        try {
            client.getObject("01ba4719c80b6fe911b091a7c05124b64eeece964e09c058ef8f9805daca546b") { `in`: InputStream ->
                ByteStreams.toByteArray(`in`)
            }
            Assert.fail()
        } catch (ignored: FileNotFoundException) {
        }
        replay.close()
    }
}
