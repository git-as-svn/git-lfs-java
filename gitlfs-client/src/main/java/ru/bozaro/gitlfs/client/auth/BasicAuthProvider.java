package ru.bozaro.gitlfs.client.auth;

import org.apache.commons.codec.binary.Base64;
import ru.bozaro.gitlfs.common.data.Link;
import ru.bozaro.gitlfs.common.data.Operation;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.TreeMap;

import static ru.bozaro.gitlfs.common.Constants.HEADER_AUTHORIZATION;

/**
 * Auth provider for basic authentication.
 *
 * @author Artem V. Navrotskiy
 */
public class BasicAuthProvider implements AuthProvider {
  @Nonnull
  private final Link auth;

  public BasicAuthProvider(@Nonnull URI href) {
    this(href, null, null);
  }

  public BasicAuthProvider(@Nonnull URI href, @CheckForNull String login, @CheckForNull String password) {
    final String authLogin;
    if (isEmpty(login)) {
      authLogin = extractLogin(href.getUserInfo());
    } else {
      authLogin = login;
    }
    final String authPassword;
    if (isEmpty(password)) {
      authPassword = extractPassword(href.getUserInfo());
    } else {
      authPassword = password;
    }
    final TreeMap<String, String> header = new TreeMap<>();
    final String userInfo = authLogin + ':' + authPassword;
    header.put(HEADER_AUTHORIZATION, "Basic " + new String(Base64.encodeBase64(userInfo.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
    try {
      final String scheme = "git".equals(href.getScheme()) ? "https" : href.getScheme();
      this.auth = new Link(new URI(scheme, href.getAuthority(), href.getPath(), null, null), Collections.unmodifiableMap(header), null);
    } catch (URISyntaxException e) {
      throw new IllegalStateException(e);
    }
  }

  private static boolean isEmpty(@CheckForNull String value) {
    return value == null || value.isEmpty();
  }

  @Nonnull
  private static String extractLogin(@CheckForNull String userInfo) {
    if (userInfo == null) return "";
    final int separator = userInfo.indexOf(':');
    return (separator >= 0) ? userInfo.substring(0, separator) : userInfo;
  }

  @Nonnull
  private static String extractPassword(@CheckForNull String userInfo) {
    if (userInfo == null) return "";
    final int separator = userInfo.indexOf(':');
    return (separator >= 0) ? userInfo.substring(separator + 1) : "";
  }

  @Nonnull
  @Override
  public Link getAuth(@Nonnull Operation operation) {
    return auth;
  }

  @Override
  public void invalidateAuth(@Nonnull Operation operation, @Nonnull Link auth) {
  }
}
