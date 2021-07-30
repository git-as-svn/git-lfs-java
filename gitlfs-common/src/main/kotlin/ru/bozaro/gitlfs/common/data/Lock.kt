package ru.bozaro.gitlfs.common.data

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

class Lock(
        /**
         * String ID of the Lock.
         */
        @field:JsonProperty(value = "id", required = true)
        @param:JsonProperty(value = "id", required = true)
        val id: String,
        /**
         * String path name of the locked file.
         */
        @field:JsonProperty(value = "path", required = true)
        @param:JsonProperty(value = "path", required = true)
        val path: String,
        /**
         * The timestamp the lock was created, as an ISO 8601 formatted string.
         */
        @field:JsonProperty(value = "locked_at", required = true)
        @param:JsonProperty(value = "locked_at")
        val lockedAt: Date,
        /**
         * The name of the user that created the Lock. This should be set from the user credentials posted when creating the lock.
         */
        @field:JsonProperty(value = "owner")
        @param:JsonProperty(value = "owner")
        val owner: User?
) : Comparable<Lock> {

    override fun hashCode(): Int {
        return Objects.hash(id)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is Lock) false else id == other.id
    }

    override fun compareTo(other: Lock): Int {
        return lockComparator.compare(this, other)
    }

    companion object {
        val lockComparator: Comparator<Lock> = Comparator
                .comparing { obj: Lock -> obj.lockedAt }
                .thenComparing { obj: Lock -> obj.id }
    }
}
