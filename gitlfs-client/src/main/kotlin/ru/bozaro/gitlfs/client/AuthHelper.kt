package ru.bozaro.gitlfs.client

import ru.bozaro.gitlfs.client.auth.AuthProvider
import ru.bozaro.gitlfs.client.auth.BasicAuthProvider
import ru.bozaro.gitlfs.client.auth.ExternalAuthProvider
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException

/**
 * Utility class.
 *
 * @author Artem V. Navrotskiy
 */
object AuthHelper {
    /**
     * Create AuthProvider by gitURL.
     *
     *
     * Supported URL formats:
     *
     *
     * * https://user:passw0rd@github.com/foo/bar.git
     * * http://user:passw0rd@github.com/foo/bar.git
     * * git://user:passw0rd@github.com/foo/bar.git
     * * ssh://git@github.com/foo/bar.git
     * * git@github.com:foo/bar.git
     *
     *
     * Detail Git URL format: https://git-scm.com/book/ch4-1.html
     *
     * @param gitURL URL to repository.
     * @return Created auth provider.
     */
    @kotlin.jvm.JvmStatic
    @Throws(MalformedURLException::class)
    fun create(gitURL: String): AuthProvider {
        if (gitURL.contains("://")) {
            val uri = URI.create(gitURL)
            val path = uri.path
            return when (uri.scheme) {
                "https", "http", "git" -> BasicAuthProvider(join(uri, "info/lfs"))
                "ssh" -> ExternalAuthProvider(uri.authority, if (path.startsWith("/")) path.substring(1) else path)
                else -> throw MalformedURLException("Can't find authenticator for scheme: " + uri.scheme)
            }
        }
        return ExternalAuthProvider(gitURL)
    }

    fun join(href: URI, vararg path: String): URI {
        return try {
            var uri = URI(href.scheme, href.authority, href.path + if (href.path.endsWith("/")) "" else "/", null, null)
            for (fragment in path) uri = uri.resolve(fragment)
            uri
        } catch (e: URISyntaxException) {
            throw IllegalStateException(e)
        }
    }
}
