package ru.bozaro.gitlfs.common.data

import com.fasterxml.jackson.annotation.JsonProperty

class Ref(
        /**
         * Fully-qualified server refspec.
         */
        @field:JsonProperty(value = "name", required = true)
        @param:JsonProperty(value = "name", required = true)
        val name: String) {
    override fun toString(): String {
        return name
    }

    companion object {
        fun create(ref: String?): Ref? {
            return if (ref == null) null else Ref(ref)
        }
    }
}
