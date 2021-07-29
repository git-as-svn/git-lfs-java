package ru.bozaro.gitlfs.client

import com.google.common.io.ByteStreams
import ru.bozaro.gitlfs.client.AuthHelper.create
import ru.bozaro.gitlfs.client.io.FileStreamProvider
import ru.bozaro.gitlfs.common.data.Meta
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Executors

/**
 * This class provides examples for documentation.
 */
class Examples {
    @Throws(IOException::class)
    fun download() {
        // tag::download[]
        val auth = create("git@github.com:foo/bar.git")
        val client = Client(auth)

        // Single object
        val content = client.getObject("4d7a214614ab2935c943f9e0ff69d22eadbb8f32b1258daaa5e2ca24d17e2393") { `in`: InputStream -> ByteStreams.toByteArray(`in`) }

        // Batch mode
        val pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        val downloader = BatchDownloader(client, pool)
        val future = downloader.download(Meta("4d7a214614ab2935c943f9e0ff69d22eadbb8f32b1258daaa5e2ca24d17e2393", 10)) { `in`: InputStream -> ByteStreams.toByteArray(`in`) }
        // end::download[]
    }

    @Throws(Exception::class)
    fun upload() {
        // tag::upload[]
        val auth = create("git@github.com:foo/bar.git")
        val client = Client(auth)

        // Single object
        client.putObject(FileStreamProvider(File("foo.bin")))

        // Batch mode
        val pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        val uploader = BatchUploader(client, pool)
        val future = uploader.upload(FileStreamProvider(File("bar.bin")))
        // end::upload[]
    }
}
