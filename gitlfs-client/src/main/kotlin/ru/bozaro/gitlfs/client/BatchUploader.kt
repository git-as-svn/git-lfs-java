package ru.bozaro.gitlfs.client

import ru.bozaro.gitlfs.client.internal.BatchWorker
import ru.bozaro.gitlfs.client.internal.Work
import ru.bozaro.gitlfs.client.io.StreamProvider
import ru.bozaro.gitlfs.common.data.BatchItem
import ru.bozaro.gitlfs.common.data.LinkType
import ru.bozaro.gitlfs.common.data.Meta
import ru.bozaro.gitlfs.common.data.Operation
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService

/**
 * Batching uploader client.
 *
 * @author Artem V. Navrotskiy
 */
class BatchUploader constructor(client: Client, pool: ExecutorService, settings: BatchSettings = BatchSettings()) :
        BatchWorker<StreamProvider, Meta?>(client, pool, settings, Operation.Upload) {
    /**
     * This method computes stream metadata and upload object.
     *
     * @param streamProvider Stream provider.
     * @return Return future with upload result.
     */
    fun upload(streamProvider: StreamProvider): CompletableFuture<Meta> {
        val future = CompletableFuture<Meta>()
        pool.submit {
            try {
                future.complete(Client.generateMeta(streamProvider))
            } catch (e: Throwable) {
                future.completeExceptionally(e)
            }
        }
        return future.thenCompose { meta: Meta -> upload(meta, streamProvider) }
    }

    /**
     * This method start uploading object to server.
     *
     * @param meta           Object metadata.
     * @param streamProvider Stream provider.
     * @return Return future with upload result. For same objects can return same future.
     */
    fun upload(meta: Meta, streamProvider: StreamProvider): CompletableFuture<Meta?> {
        return enqueue(meta, streamProvider)
    }

    override fun objectTask(state: State<StreamProvider, Meta?>, item: BatchItem): Work<Meta?>? {
        return if (item.links.containsKey(LinkType.Upload)) {
            // Wait for upload.
            Work {
                client.putObject(state.context, state.meta, item)
                null
            }
        } else {
            // Already uploaded.
            state.future.complete(state.meta)
            null
        }
    }
}
