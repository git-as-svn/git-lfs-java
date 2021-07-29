package ru.bozaro.gitlfs.common.data

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

/**
 * LFS object location.
 *
 * @author Artem V. Navrotskiy
 */
class ObjectRes @JsonCreator constructor(
        @JsonProperty(value = "oid") oid: String?,
        @JsonProperty(value = "size") size: Long,
        @JsonProperty(value = "_links", required = true) links: Map<LinkType, Link>
) : Links {
    @JsonProperty(value = "_links", required = true)
    override val links: Map<LinkType, Link> = Collections.unmodifiableMap(TreeMap(links))
    val meta: Meta? = oid?.let { Meta(it, size) }
}
