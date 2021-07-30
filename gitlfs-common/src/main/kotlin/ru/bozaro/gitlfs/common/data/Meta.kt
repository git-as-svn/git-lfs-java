package ru.bozaro.gitlfs.common.data

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

/**
 * LFS object location.
 *
 * @author Artem V. Navrotskiy
 */
open class Meta @JsonCreator constructor(
        @field:JsonProperty(value = "oid", required = true)
        @param:JsonProperty(value = "oid", required = true) val oid: String,
        @field:JsonProperty(value = "size", required = true)
        @param:JsonProperty(value = "size", required = true) val size: Long
) {

    override fun hashCode(): Int {
        return Objects.hash(oid, size)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Meta) return false
        return size == other.size && oid == other.oid
    }
}
