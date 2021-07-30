package ru.bozaro.gitlfs.client.exceptions

import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpUriRequest

/**
 * Forbidden HTTP exception.
 *
 * @author Artem V. Navrotskiy
 */
class ForbiddenException(request: HttpUriRequest, response: HttpResponse) : RequestException(request, response)
