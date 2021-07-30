package ru.bozaro.gitlfs.common.data

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

/**
 * Batch request.
 *
 * @author Artem V. Navrotskiy
 */
class BatchRes @JsonCreator constructor(
        @JsonProperty(value = "objects", required = true) objects: List<BatchItem>
) {
    @JsonProperty(value = "objects", required = true)
    val objects: List<BatchItem> = Collections.unmodifiableList(ArrayList(objects))
}
