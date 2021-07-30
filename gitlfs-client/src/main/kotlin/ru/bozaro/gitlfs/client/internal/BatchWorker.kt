package ru.bozaro.gitlfs.client.internal

import org.apache.http.HttpStatus
import org.slf4j.LoggerFactory
import ru.bozaro.gitlfs.client.AuthHelper
import ru.bozaro.gitlfs.client.BatchSettings
import ru.bozaro.gitlfs.client.Client
import ru.bozaro.gitlfs.client.Client.ConnectionClosePolicy
import ru.bozaro.gitlfs.client.exceptions.ForbiddenException
import ru.bozaro.gitlfs.client.exceptions.UnauthorizedException
import ru.bozaro.gitlfs.common.Constants.PATH_BATCH
import ru.bozaro.gitlfs.common.data.*
import java.io.FileNotFoundException
import java.io.IOException
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.stream.Collectors

/**
 * Base batch client.
 *
 * @author Artem V. Navrotskiy
 */
abstract class BatchWorker<T, R>(
    protected val client: Client,
    protected val pool: ExecutorService,
    private val settings: BatchSettings,
    private val operation: Operation
) {
    private val log = LoggerFactory.getLogger(BatchWorker::class.java)

    private val batchSequence = AtomicInteger(0)

    private val batchInProgress = AtomicInteger()

    private val objectQueue: ConcurrentMap<String, State<T, R?>> = ConcurrentHashMap()

    private val objectInProgress = AtomicInteger(0)

    private val currentAuth = AtomicReference<Link?>(null)

    /**
     * This method start send object metadata to server.
     *
     * @param context Object worker context.
     * @param meta    Object metadata.
     * @return Return future with result. For same objects can return same future.
     */
    protected fun enqueue(meta: Meta, context: T): CompletableFuture<R?> {
        var state = objectQueue[meta.oid]
        if (state != null) {
            if (state.future.isCancelled) {
                objectQueue.remove(meta.oid, state)
                state = null
            }
        }
        if (state == null) {
            val newState = State<T, R?>(meta, context)
            state = objectQueue.putIfAbsent(meta.oid, newState)
            if (state == null) {
                state = newState
                stateEnqueue(true)
            }
        }
        return state.future
    }

    private fun stateEnqueue(pooled: Boolean) {
        val batchId = batchSequence.incrementAndGet()
        tryBatchRequest(batchId, pooled)
    }

    private fun tryBatchRequestPredicate(): Boolean {
        return (objectInProgress.get() < settings.threshold
                && !objectQueue.isEmpty())
    }

    private fun tryBatchRequest(batchId: Int, pooled: Boolean): Boolean {
        if (!tryBatchRequestPredicate()) {
            return false
        }
        if (batchInProgress.compareAndSet(0, batchId)) {
            executeInPool(
                "batch request: " + objectQueue.size + " in queue",
                {
                    var curBatchId = batchId
                    while (true) {
                        try {
                            submitBatchTask()
                        } finally {
                            batchInProgress.set(0)
                        }
                        val newBatchId = batchSequence.get()
                        if (newBatchId == curBatchId && !tryBatchRequestPredicate()) {
                            break
                        }
                        curBatchId = newBatchId
                        if (!batchInProgress.compareAndSet(0, curBatchId)) {
                            break
                        }
                    }
                },
                { batchInProgress.compareAndSet(batchId, 0) },
                pooled
            )
            return true
        }
        return false
    }

    private fun invalidateAuth(auth: Link) {
        if (currentAuth.compareAndSet(auth, null)) {
            client.authProvider.invalidateAuth(operation, auth)
        }
    }

    private fun submitBatchTask() {
        val batch = takeBatch()
        var auth = currentAuth.get()
        try {
            if (batch.isNotEmpty()) {
                if (auth == null) {
                    auth = client.authProvider.getAuth(operation)
                    currentAuth.set(auth)
                }
                val metas = batch.values.stream().map { s: State<T, R?> -> s.meta }.collect(Collectors.toList())
                val result = client.doRequest(
                    auth,
                    JsonPost(BatchReq(operation, metas), BatchRes::class.java),
                    AuthHelper.join(auth.href, PATH_BATCH),
                    ConnectionClosePolicy.Close
                )
                for (item in result.objects) {
                    val state = batch.remove(item.oid)
                    if (state != null) {
                        val error = item.error
                        if (error != null) {
                            objectQueue.remove(item.oid, state)
                            state.future.completeExceptionally(createError(error))
                        } else {
                            submitTask(state, item, auth)
                        }
                    }
                }
                for (value in batch.values) {
                    value.future.completeExceptionally(IOException("Requested object not found in server response: " + value.meta.oid))
                }
            }
        } catch (e: UnauthorizedException) {
            auth?.let { invalidateAuth(it) }
        } catch (e: IOException) {
            for (state in batch.values) {
                state.onException(e, state.retry)
            }
        }
    }

    protected fun createError(error: Error): Throwable {
        return if (error.code == HttpStatus.SC_NOT_FOUND) {
            FileNotFoundException(error.message)
        } else IOException("Can't process object (code " + error.code + "): " + error.message)
    }

    protected abstract fun objectTask(state: State<T, R?>, item: BatchItem): Work<R>?

    /**
     * Submit object processing task.
     *
     * @param state Current object state
     * @param item  Metadata information with upload/download urls.
     * @param auth  Urls authentication state.
     */
    private fun submitTask(state: State<T, R?>, item: BatchItem, auth: Link) {
        // Submit task
        val holder = StateHolder(state)
        try {
            state.auth = auth
            val worker = objectTask(state, item)
            if (state.future.isDone) {
                holder.close()
                return
            }
            checkNotNull(worker) { "Uncompleted task worker is null: $item" }
            executeInPool(
                "task: " + state.meta.oid,
                { processObject(state, auth, worker) }, { holder.close() },
                true
            )
        } catch (e: Throwable) {
            holder.close()
            throw e
        }
    }

    private fun takeBatch(): MutableMap<String, State<T, R?>> {
        val batch: MutableMap<String, State<T, R?>> = HashMap()
        val completed: MutableList<State<T, R?>> = ArrayList()
        for (state in objectQueue.values) {
            if (state.future.isDone) {
                completed.add(state)
                continue
            }
            if (state.auth == null) {
                batch[state.meta.oid] = state
                if (batch.size >= settings.limit) {
                    break
                }
            }
        }
        for (state in completed) {
            objectQueue.remove(state.meta.oid, state)
        }
        return batch
    }

    private fun processObject(state: State<T, R?>, auth: Link, worker: Work<R>) {
        if (currentAuth.get() != auth) {
            state.auth = null
            return
        }
        try {
            state.auth = auth
            val result = worker.exec(auth)
            objectQueue.remove(state.meta.oid, state)
            state.future.complete(result)
        } catch (e: UnauthorizedException) {
            invalidateAuth(auth)
        } catch (e: ForbiddenException) {
            state.onException(e, 0)
        } catch (e: Throwable) {
            state.onException(e, settings.retryCount)
        } finally {
            state.auth = null
        }
    }

    /**
     * Schedule task in thread pool.
     *
     *
     * If pool reject task - task will execute immediately in current thread.
     *
     *
     * If pool is shutdown - task will not run, but finalizer will executed.
     *
     * @param name      Task name for debug
     * @param task      Task runnable
     * @param finalizer Finalizer to execute like 'try-final' block
     */
    private fun executeInPool(name: String, task: Runnable, finalizer: Runnable?, pooled: Boolean) {
        if (pool.isShutdown) {
            log.warn("Thread pool is shutdown")
            finalizer?.run()
            return
        }
        if (!pooled) {
            log.debug("Begin: $name")
            try {
                task.run()
            } catch (e: Throwable) {
                log.error("Execute exception: $e")
                finalizer?.run()
                throw e
            } finally {
                finalizer?.run()
                log.debug("End: $name")
            }
            return
        }
        try {
            pool.execute(object : Runnable {
                override fun run() {
                    executeInPool(name, task, finalizer, false)
                }

                override fun toString(): String {
                    return name
                }
            })
        } catch (e: RejectedExecutionException) {
            if (pool.isShutdown) {
                log.warn("Thread pool is shutdown")
            } else {
                executeInPool(name, task, finalizer, false)
            }
        } catch (e: Throwable) {
            log.error("Execute in pool exception: $e")
            finalizer?.run()
            throw e
        }
    }

    class State<T, R>(
        val meta: Meta,
        val context: T
    ) {

        val future = CompletableFuture<R>()

        @Volatile
        var auth: Link? = null
        var retry = 0
        fun onException(e: Throwable, maxRetryCount: Int) {
            retry++
            if (retry >= maxRetryCount) {
                future.completeExceptionally(e)
            }
            auth = null
        }
    }

    private inner class StateHolder(state: State<T, R?>) : AutoCloseable {
        private val stateRef: AtomicReference<State<T, R?>> = AtomicReference(state)

        override fun close() {
            val state = stateRef.getAndSet(null) ?: return
            if (state.future.isDone) {
                objectInProgress.decrementAndGet()
                objectQueue.remove(state.meta.oid, state)
            } else {
                state.auth = null
                objectInProgress.decrementAndGet()
            }
            stateEnqueue(false)
        }

        init {
            objectInProgress.incrementAndGet()
        }
    }
}
