package ru.bozaro.gitlfs.client

import ru.bozaro.gitlfs.common.Constants.BATCH_SIZE
import kotlin.math.max
import kotlin.math.min

/**
 * Batch settings.
 *
 * @author Artem V. Navrotskiy <bozaro></bozaro>@users.noreply.github.com>
 */
class BatchSettings {
    /**
     * Maximum objects in batch request.
     */
    var limit: Int = BATCH_SIZE
        private set

    /**
     * Minimum download/upload requests in queue for next batch request.
     */
    var threshold = 10
        private set

    /**
     * Retry on failure count.
     */
    var retryCount = 3
        private set

    constructor()
    constructor(limit: Int, threshold: Int, retryCount: Int) {
        this.limit = limit
        this.threshold = threshold
        this.retryCount = retryCount
    }

    fun setLimit(limit: Int): BatchSettings {
        this.limit = min(limit, 1)
        return this
    }

    fun setThreshold(threshold: Int): BatchSettings {
        this.threshold = max(threshold, 0)
        return this
    }

    fun setRetryCount(retryCount: Int): BatchSettings {
        this.retryCount = max(retryCount, 1)
        return this
    }

    override fun toString(): String {
        return "BatchSettings{" +
                "limit=" + limit +
                ", threshold=" + threshold +
                ", retryCount=" + retryCount +
                '}'
    }
}
