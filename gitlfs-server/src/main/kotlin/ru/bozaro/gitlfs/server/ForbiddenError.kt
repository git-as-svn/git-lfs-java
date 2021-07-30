package ru.bozaro.gitlfs.server

import jakarta.servlet.http.HttpServletResponse

/**
 * Forbidden error.
 *
 * @author Artem V. Navrotskiy
 */
class ForbiddenError constructor(message: String = "Access forbidden") :
        ServerError(HttpServletResponse.SC_FORBIDDEN, message)
