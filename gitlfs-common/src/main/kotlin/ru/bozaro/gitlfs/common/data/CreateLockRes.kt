package ru.bozaro.gitlfs.common.data

import com.fasterxml.jackson.annotation.JsonProperty

class CreateLockRes(
        @field:JsonProperty(value = "lock", required = true)
        @param:JsonProperty(value = "lock", required = true)
        val lock: Lock
)
