package ru.bozaro.gitlfs.client

import ru.bozaro.gitlfs.client.internal.BatchWorker
import ru.bozaro.gitlfs.client.internal.Work
import ru.bozaro.gitlfs.client.io.StreamHandler
import ru.bozaro.gitlfs.common.data.BatchItem
import ru.bozaro.gitlfs.common.data.LinkType
import ru.bozaro.gitlfs.common.data.Meta
import ru.bozaro.gitlfs.common.data.Operation
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService

/**
 * Batching downloader client.
 *
 * @author Artem V. Navrotskiy
 */
class BatchDownloader constructor(client: Client, pool: ExecutorService, settings: BatchSettings = BatchSettings()) :
        BatchWorker<StreamHandler<*>, Any?>(client, pool, settings, Operation.Download) {
    fun <T> download(meta: Meta, callback: StreamHandler<T>): CompletableFuture<T?> {
        return enqueue(meta, callback) as CompletableFuture<T?>
    }

    override fun objectTask(state: State<StreamHandler<*>, Any?>, item: BatchItem): Work<Any?>? {
        // Invalid links data
        if (!item.links.containsKey(LinkType.Download)) {
            state.future.completeExceptionally(IOException("Download link not found"))
            return null
        }
        // Already processed
        return Work { client.getObject(item, item, state.context) }
    }
}
