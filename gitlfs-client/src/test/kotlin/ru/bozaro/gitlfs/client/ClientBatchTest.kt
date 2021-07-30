package ru.bozaro.gitlfs.client

import com.google.common.collect.ImmutableMap
import org.testng.Assert
import org.testng.annotations.Test
import ru.bozaro.gitlfs.common.JsonHelper
import ru.bozaro.gitlfs.common.data.*
import java.io.IOException
import java.net.URI

/**
 * Replay tests for https://github.com/github/git-lfs/blob/master/docs/api/http-v1-batch.md
 *
 * @author Artem V. Navrotskiy
 */
class ClientBatchTest {
    /**
     * Simple upload.
     */
    @Test
    @Throws(IOException::class)
    fun batchUpload01() {
        batchUpload("/ru/bozaro/gitlfs/client/batch-upload-01.yml", false)
    }

    /**
     * Simple upload (JFrog Artifactory).
     */
    @Test
    @Throws(IOException::class)
    fun batchUpload02() {
        batchUpload("/ru/bozaro/gitlfs/client/batch-upload-02.yml", false)
    }

    @Test
    @Throws(IOException::class)
    fun batchUploadChunked() {
        batchUpload("/ru/bozaro/gitlfs/client/batch-upload-chunked.yml", true)
    }

    @Throws(IOException::class)
    private fun batchUpload(path: String, chunked: Boolean) {
        val replay = YamlHelper.createReplay(path)
        val client = Client(FakeAuthProvider(chunked), replay)
        val result = client.postBatch(
                BatchReq(
                        Operation.Upload,
                        listOf(
                                Meta("b810bbe954d51e380f395de0c301a0a42d16f115453f2feb4188ca9f7189074e", 28),
                                Meta("1cbec737f863e4922cee63cc2ebbfaafcd1cff8b790d8cfd2e6a5d550b648afa", 3)
                        )
                )
        )
        Assert.assertEquals(
                JsonHelper.mapper.writeValueAsString(result), JsonHelper.mapper.writeValueAsString(
                BatchRes(
                        listOf(
                                BatchItem(
                                        Meta("b810bbe954d51e380f395de0c301a0a42d16f115453f2feb4188ca9f7189074e", 28),
                                        ImmutableMap.builder<LinkType, Link>()
                                                .build()
                                ),
                                BatchItem(
                                        Meta("1cbec737f863e4922cee63cc2ebbfaafcd1cff8b790d8cfd2e6a5d550b648afa", 3),
                                        ImmutableMap.builder<LinkType, Link>()
                                                .put(
                                                        LinkType.Upload, Link(
                                                        URI.create("https://github-cloud.s3.amazonaws.com/alambic/media/111975537/1c/be/1cbec737f863e4922cee63cc2ebbfaafcd1cff8b790d8cfd2e6a5d550b648afa?actor_id=2458138"),
                                                        ImmutableMap.builder<String, String>()
                                                                .put("Authorization", "AWS4-HMAC-SHA256 Credential=Token-2")
                                                                .put(
                                                                        "x-amz-content-sha256",
                                                                        "1cbec737f863e4922cee63cc2ebbfaafcd1cff8b790d8cfd2e6a5d550b648afa"
                                                                )
                                                                .put("x-amz-date", "20151007T190730Z")
                                                                .build(),
                                                        null
                                                )
                                                )
                                                .put(
                                                        LinkType.Verify, Link(
                                                        URI.create("https://api.github.com/lfs/bozaro/test/objects/1cbec737f863e4922cee63cc2ebbfaafcd1cff8b790d8cfd2e6a5d550b648afa/verify"),
                                                        ImmutableMap.builder<String, String>()
                                                                .put("Accept", "application/vnd.git-lfs+json")
                                                                .put("Authorization", "RemoteAuth Token-3")
                                                                .build(),
                                                        null
                                                )
                                                )
                                                .build()
                                )
                        )
                )
        )
        )
        replay.close()
    }

    /**
     * Simple download.
     */
    @Test
    @Throws(IOException::class)
    fun batchDownload01() {
        batchDownload("/ru/bozaro/gitlfs/client/batch-download-01.yml")
    }

    @Throws(IOException::class)
    private fun batchDownload(path: String) {
        val replay = YamlHelper.createReplay(path)
        val client = Client(FakeAuthProvider(false), replay)
        val result = client.postBatch(
                BatchReq(
                        Operation.Download,
                        listOf(
                                Meta("b810bbe954d51e380f395de0c301a0a42d16f115453f2feb4188ca9f7189074e", 28),
                                Meta("1cbec737f863e4922cee63cc2ebbfaafcd1cff8b790d8cfd2e6a5d550b648afa", 3)
                        )
                )
        )
        Assert.assertEquals(
                JsonHelper.mapper.writeValueAsString(result), JsonHelper.mapper.writeValueAsString(
                BatchRes(
                        listOf(
                                BatchItem(
                                        Meta("b810bbe954d51e380f395de0c301a0a42d16f115453f2feb4188ca9f7189074e", 28),
                                        ImmutableMap.builder<LinkType, Link>()
                                                .put(
                                                        LinkType.Download, Link(
                                                        URI.create("https://github-cloud.s3.amazonaws.com/alambic/media/111975537/b8/10/b810bbe954d51e380f395de0c301a0a42d16f115453f2feb4188ca9f7189074e?actor_id=2458138"),
                                                        ImmutableMap.builder<String, String>()
                                                                .put("Authorization", "Token-2")
                                                                .put(
                                                                        "x-amz-content-sha256",
                                                                        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
                                                                )
                                                                .put("x-amz-date", "20151007T190640Z")
                                                                .build(),
                                                        null
                                                )
                                                )
                                                .build()
                                ),
                                BatchItem(
                                        Meta("1cbec737f863e4922cee63cc2ebbfaafcd1cff8b790d8cfd2e6a5d550b648afa", 3),
                                        Error(404, "Object does not exist on the server")
                                )
                        )
                )
        )
        )
        replay.close()
    }

    /**
     * Simple download (JFrog Artifactory).
     */
    @Test
    @Throws(IOException::class)
    fun batchDownload02() {
        batchDownload("/ru/bozaro/gitlfs/client/batch-download-02.yml")
    }
}
