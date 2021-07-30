package ru.bozaro.gitlfs.common.data

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

/**
 * Batch request.
 *
 * @author Artem V. Navrotskiy
 */
class BatchReq(
        @field:JsonProperty(value = "operation", required = true)
        @param:JsonProperty(value = "operation", required = true)
        val operation: Operation,
        @JsonProperty(value = "objects", required = true) objects: List<Meta>
) {

    @JsonProperty(value = "objects", required = true)
    val objects: List<Meta> = Collections.unmodifiableList(ArrayList(objects))
}
