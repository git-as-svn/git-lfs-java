package ru.bozaro.gitlfs.common.data

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI
import java.util.*

/**
 * LFS reference.
 *
 * @author Artem V. Navrotskiy
 */
class Link @JsonCreator constructor(
        @field:JsonProperty(value = "href", required = true)
        @param:JsonProperty(value = "href", required = true)
        val href: URI,
        @JsonProperty("header") header: Map<String, String>?,
        @JsonProperty("expires_at") expiresAt: Date?
) {

    @JsonProperty("header")
    val header: Map<String, String> = header?.let { TreeMap(it) } ?: emptyMap()

    @JsonProperty("expires_at")
    val expiresAt: Date? = if (expiresAt != null) Date(expiresAt.time) else null
}
