package ru.bozaro.gitlfs.common.data

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

/**
 * LFSP operation type.
 *
 * @author Artem V. Navrotskiy
 */
enum class Operation {
    Download {
        override fun toValue(): String {
            return "download"
        }

        override fun <R> visit(visitor: Visitor<R>): R {
            return visitor.visitDownload()
        }
    },
    Upload {
        override fun toValue(): String {
            return "upload"
        }

        override fun <R> visit(visitor: Visitor<R>): R {
            return visitor.visitUpload()
        }
    };

    @JsonValue
    abstract fun toValue(): String
    abstract fun <R> visit(visitor: Visitor<R>): R
    interface Visitor<R> {
        fun visitDownload(): R
        fun visitUpload(): R
    }

    companion object {
        @JsonCreator
        fun forValue(value: String): Operation? {
            for (item in values()) {
                if (item.toValue() == value) {
                    return item
                }
            }
            return null
        }
    }
}
