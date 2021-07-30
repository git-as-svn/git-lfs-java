package ru.bozaro.gitlfs.common.data

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * LFS error description.
 *
 * @author Artem V. Navrotskiy
 */
class Error @JsonCreator constructor(
        @field:JsonProperty(value = "code", required = true) @param:JsonProperty(value = "code") val code: Int,
        @field:JsonProperty(value = "message") @param:JsonProperty(value = "message") val message: String?
)
