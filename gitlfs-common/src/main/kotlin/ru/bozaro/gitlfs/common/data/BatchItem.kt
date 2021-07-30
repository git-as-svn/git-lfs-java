package ru.bozaro.gitlfs.common.data

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

/**
 * LFS batch object.
 *
 * @author Artem V. Navrotskiy
 */
class BatchItem(
        @JsonProperty(value = "oid", required = true) oid: String,
        @JsonProperty(value = "size", required = true) size: Long,
        @JsonProperty(value = "actions") links1: Map<LinkType, Link>?,
        @JsonProperty(value = "_links") links2: Map<LinkType, Link>?,
        @field:JsonProperty(value = "error")
        @param:JsonProperty(value = "error")
        val error: Error?
) : Meta(oid, size), Links {
    @JsonProperty(value = "actions")
    override val links: Map<LinkType, Link> = combine(links1, links2)

    constructor(meta: Meta, links: MutableMap<LinkType, Link>) : this(meta.oid, meta.size, links, null, null)
    constructor(meta: Meta, error: Error?) : this(meta.oid, meta.size, null, null, error)

    companion object {
        private fun <K, V> combine(a: Map<K, V>?, b: Map<K, V>?): Map<K, V> {
            var r: Map<K, V>? = null
            if (a != null && a.isNotEmpty()) {
                r = a
            }
            if (b != null && b.isNotEmpty()) {
                if (r == null) {
                    r = b
                } else {
                    r = TreeMap(r)
                    r.putAll(b)
                }
            }
            return if (r == null) emptyMap() else Collections.unmodifiableMap(r)
        }
    }

}
