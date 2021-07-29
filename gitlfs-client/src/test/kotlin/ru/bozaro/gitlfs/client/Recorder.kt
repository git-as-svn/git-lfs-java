package ru.bozaro.gitlfs.client

import org.apache.http.impl.client.HttpClients
import ru.bozaro.gitlfs.client.AuthHelper.create
import ru.bozaro.gitlfs.client.internal.HttpClientExecutor
import ru.bozaro.gitlfs.common.data.BatchReq
import ru.bozaro.gitlfs.common.data.Meta
import ru.bozaro.gitlfs.common.data.Operation
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

/**
 * Simple code for recording replay.
 *
 * @author Artem V. Navrotskiy
 */
object Recorder {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val auth = create("git@github.com:bozaro/test.git")
        HttpClients.createDefault().use { httpClient ->
            val recorder = HttpRecorder(HttpClientExecutor(httpClient))
            doWork(Client(auth, recorder))
            val yaml = YamlHelper.get()
            val file = File("build/replay.yml")
            file.parentFile.mkdirs()
            FileOutputStream(file).use { replay ->
                yaml.dumpAll(
                    recorder.records.iterator(),
                    OutputStreamWriter(replay, StandardCharsets.UTF_8)
                )
            }
        }
    }

    @Throws(IOException::class)
    private fun doWork(client: Client) {
        client.postBatch(
            BatchReq(
                Operation.Upload,
                listOf(
                    Meta("b810bbe954d51e380f395de0c301a0a42d16f115453f2feb4188ca9f7189074e", 28),
                    Meta("1cbec737f863e4922cee63cc2ebbfaafcd1cff8b790d8cfd2e6a5d550b648afa", 3)
                )
            )
        )
    }
}
