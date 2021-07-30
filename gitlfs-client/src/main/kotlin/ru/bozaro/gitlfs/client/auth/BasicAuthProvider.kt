package ru.bozaro.gitlfs.client.auth

import org.apache.commons.codec.binary.Base64
import ru.bozaro.gitlfs.common.Constants.HEADER_AUTHORIZATION
import ru.bozaro.gitlfs.common.data.Link
import ru.bozaro.gitlfs.common.data.Operation
import java.net.URI
import java.net.URISyntaxException
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Auth provider for basic authentication.
 *
 * @author Artem V. Navrotskiy
 */
class BasicAuthProvider constructor(href: URI, login: String? = null, password: String? = null) : AuthProvider {
    private var auth: Link

    override fun getAuth(operation: Operation): Link {
        return auth
    }

    override fun invalidateAuth(operation: Operation, auth: Link) {}

    companion object {
        private fun isEmpty(value: String?): Boolean {
            return value == null || value.isEmpty()
        }

        private fun extractLogin(userInfo: String?): String {
            if (userInfo == null) return ""
            val separator = userInfo.indexOf(':')
            return if (separator >= 0) userInfo.substring(0, separator) else userInfo
        }

        private fun extractPassword(userInfo: String?): String {
            if (userInfo == null) return ""
            val separator = userInfo.indexOf(':')
            return if (separator >= 0) userInfo.substring(separator + 1) else ""
        }
    }

    init {
        val authLogin: String? = if (isEmpty(login)) {
            extractLogin(href.userInfo)
        } else {
            login
        }
        val authPassword: String? = if (isEmpty(password)) {
            extractPassword(href.userInfo)
        } else {
            password
        }
        val header = TreeMap<String, String>()
        val userInfo = "$authLogin:$authPassword"
        header[HEADER_AUTHORIZATION] =
                "Basic " + String(Base64.encodeBase64(userInfo.toByteArray(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)
        try {
            val scheme = if ("git" == href.scheme) "https" else href.scheme
            auth = Link(URI(scheme, href.authority, href.path, null, null), Collections.unmodifiableMap(header), null)
        } catch (e: URISyntaxException) {
            throw IllegalStateException(e)
        }
    }
}
