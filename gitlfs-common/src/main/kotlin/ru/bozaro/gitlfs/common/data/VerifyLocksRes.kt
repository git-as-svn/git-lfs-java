package ru.bozaro.gitlfs.common.data

import com.fasterxml.jackson.annotation.JsonProperty

class VerifyLocksRes(
        @field:JsonProperty(value = "ours", required = true)
        @param:JsonProperty(value = "ours", required = true)
        val ours: List<Lock>,
        @field:JsonProperty(value = "theirs", required = true)
        @param:JsonProperty(value = "theirs", required = true)
        val theirs: List<Lock>,
        @field:JsonProperty(value = "next_cursor")
        @param:JsonProperty(value = "next_cursor")
        val nextCursor: String?
)
