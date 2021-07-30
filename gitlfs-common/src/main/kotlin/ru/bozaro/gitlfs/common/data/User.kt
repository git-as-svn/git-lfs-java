package ru.bozaro.gitlfs.common.data

import com.fasterxml.jackson.annotation.JsonProperty

class User(
        @field:JsonProperty(value = "name", required = true)
        @param:JsonProperty(value = "name", required = true)
        val name: String
)
