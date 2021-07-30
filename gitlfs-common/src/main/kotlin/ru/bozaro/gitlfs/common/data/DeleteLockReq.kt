package ru.bozaro.gitlfs.common.data

import com.fasterxml.jackson.annotation.JsonProperty

class DeleteLockReq(
        /**
         * Optional boolean specifying that the user is deleting another user's lock.
         */
        @field:JsonProperty(value = "force") @param:JsonProperty(value = "force") private val force: Boolean?,
        /**
         * Optional object describing the server ref that the locks belong to.
         */
        @field:JsonProperty(value = "ref") @param:JsonProperty(value = "ref") val ref: Ref?) {
    fun isForce(): Boolean {
        return force != null && force
    }
}
