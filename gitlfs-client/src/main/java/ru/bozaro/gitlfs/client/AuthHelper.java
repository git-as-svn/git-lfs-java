package ru.bozaro.gitlfs.client;

import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.client.auth.AuthProvider;
import ru.bozaro.gitlfs.client.auth.BasicAuthProvider;
import ru.bozaro.gitlfs.client.auth.ExternalAuthProvider;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utility class.
 *
 * @author Artem V. Navrotskiy
 */
public final class AuthHelper {
  private AuthHelper() {
  }

  /**
   * Create AuthProvider by gitURL.
   * <p/>
   * Supported URL formats:
   * <p/>
   * * https://user:passw0rd@github.com/foo/bar.git
   * * http://user:passw0rd@github.com/foo/bar.git
   * * git://user:passw0rd@github.com/foo/bar.git
   * * ssh://git@github.com/foo/bar.git
   * * git@github.com:foo/bar.git
   * <p/>
   * Detail Git URL format: https://git-scm.com/book/ch4-1.html
   *
   * @param gitURL URL to repository.
   * @return Created auth provider.
   */
  public static AuthProvider create(@NotNull String gitURL) throws MalformedURLException {
    if (gitURL.contains("://")) {
      final URI uri = URI.create(gitURL);
      final String path = uri.getPath();
      switch (uri.getScheme()) {
        case "https":
        case "http":
        case "git":
          return new BasicAuthProvider(join(uri, "info/lfs"));
        case "ssh":
          return new ExternalAuthProvider(uri.getAuthority(), path.startsWith("/") ? path.substring(1) : path);
        default:
          throw new MalformedURLException("Can't find authenticator for scheme: " + uri.getScheme());
      }
    }
    return new ExternalAuthProvider(gitURL);
  }

  @NotNull
  public static URI join(@NotNull URI href, @NotNull String path) {
    try {
      return new URI(href.getScheme(), href.getAuthority(), href.getPath() + (href.getPath().endsWith("/") ? "" : "/"), null, null).resolve(path);
    } catch (URISyntaxException e) {
      throw new IllegalStateException(e);
    }
  }
}
