package ru.bozaro.gitlfs.server

import jakarta.servlet.http.HttpServletResponse

/**
 * Unauthorized error.
 *
 * @author Artem V. Navrotskiy
 */
class UnauthorizedError(private val authenticate: String) :
        ServerError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized") {
    override fun updateHeaders(response: HttpServletResponse) {
        super.updateHeaders(response)
        response.addHeader("WWW-Authenticate", authenticate)
    }
}
