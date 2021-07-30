package ru.bozaro.gitlfs.common.data

import com.fasterxml.jackson.annotation.JsonProperty

class LocksRes(
        @field:JsonProperty(value = "locks", required = true)
        @param:JsonProperty(value = "locks", required = true)
        val locks: List<Lock>,
        @field:JsonProperty(value = "next_cursor")
        @param:JsonProperty(value = "next_cursor")
        val nextCursor: String?
)
