package ru.bozaro.gitlfs.common.data

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

/**
 * LFSP operation type.
 *
 * @author Artem V. Navrotskiy
 */
enum class LinkType {
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
    },
    Verify {
        override fun toValue(): String {
            return "verify"
        }

        override fun <R> visit(visitor: Visitor<R>): R {
            return visitor.visitVerify()
        }
    },
    Self {
        override fun toValue(): String {
            return "self"
        }

        override fun <R> visit(visitor: Visitor<R>): R {
            return visitor.visitSelf()
        }
    };

    @JsonValue
    abstract fun toValue(): String
    abstract fun <R> visit(visitor: Visitor<R>): R
    interface Visitor<R> {
        fun visitDownload(): R
        fun visitUpload(): R
        fun visitVerify(): R
        fun visitSelf(): R
    }

    companion object {
        @JsonCreator
        fun forValue(value: String): LinkType? {
            for (item in values()) {
                if (item.toValue() == value) {
                    return item
                }
            }
            return null
        }
    }
}
