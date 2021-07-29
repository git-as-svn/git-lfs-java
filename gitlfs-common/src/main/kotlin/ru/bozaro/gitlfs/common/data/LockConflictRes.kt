package ru.bozaro.gitlfs.common.data

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

class LockConflictRes @JsonCreator constructor(
        @field:JsonProperty(value = "message", required = true)
        @param:JsonProperty(value = "message", required = true)
        val message: String,
        @field:JsonProperty(value = "lock", required = true)
        @param:JsonProperty(value = "lock", required = true)
        val lock: Lock
)
