package ru.bozaro.gitlfs.common.data

import com.fasterxml.jackson.annotation.JsonProperty

class CreateLockReq(
        /**
         * String path name of the locked file.
         */
        @field:JsonProperty(value = "path", required = true)
        @param:JsonProperty(value = "path", required = true) val path: String,
        /**
         * Optional object describing the server ref that the locks belong to.
         */
        @field:JsonProperty(value = "ref")
        @param:JsonProperty(value = "ref")
        val ref: Ref?
)
