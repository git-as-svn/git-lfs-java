package ru.bozaro.gitlfs.client.exceptions

import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpUriRequest

/**
 * Unauthorized HTTP exception.
 *
 * @author Artem V. Navrotskiy
 */
class UnauthorizedException(request: HttpUriRequest, response: HttpResponse) : RequestException(request, response)
