package ru.bozaro.gitlfs.server

import jakarta.servlet.http.HttpServletResponse

/**
 * Server side error exception.
 *
 * @author Artem V. Navrotskiy
 */
open class ServerError : Exception {
    val statusCode: Int

    constructor(statusCode: Int, message: String?) : super(message) {
        this.statusCode = statusCode
    }

    constructor(statusCode: Int, message: String?, cause: Throwable?) : super(message, cause) {
        this.statusCode = statusCode
    }

    open fun updateHeaders(response: HttpServletResponse) {}
}
