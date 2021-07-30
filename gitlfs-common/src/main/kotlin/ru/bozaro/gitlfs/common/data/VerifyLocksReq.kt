package ru.bozaro.gitlfs.common.data

import com.fasterxml.jackson.annotation.JsonProperty

class VerifyLocksReq(
        /**
         * Optional cursor to allow pagination.
         */
        @field:JsonProperty(value = "cursor") @param:JsonProperty(value = "cursor") val cursor: String?,
        /**
         * Optional object describing the server ref that the locks belong to.
         */
        @field:JsonProperty(value = "ref") @param:JsonProperty(value = "ref") val ref: Ref?,
        /**
         * Optional limit to how many locks to return.
         */
        @field:JsonProperty(value = "limit") @param:JsonProperty(value = "limit") val limit: Int?)
