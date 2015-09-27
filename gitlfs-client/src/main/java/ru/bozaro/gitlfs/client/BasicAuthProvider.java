package ru.bozaro.gitlfs.client;

import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.common.data.Link;
import ru.bozaro.gitlfs.common.data.Operation;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.TreeMap;

/**
 * Auth provider for basic authentication.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class BasicAuthProvider implements AuthProvider {
  @NotNull
  private final Link auth;

  public BasicAuthProvider(@NotNull URI href) {
    this(href, null, null);
  }

  public BasicAuthProvider(@NotNull URI href, @Nullable String login, @Nullable String password) {
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
    header.put(Constants.HEADER_AUTHORIZATION, new String(Base64.encodeBase64(userInfo.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
    try {
      this.auth = new Link(new URI(href.getScheme(), href.getAuthority(), href.getPath(), null, null), Collections.unmodifiableMap(header), null);
    } catch (URISyntaxException e) {
      throw new IllegalStateException(e);
    }
  }

  @NotNull
  @Override
  public Link getAuth(@NotNull Operation operation) throws IOException {
    return auth;
  }

  @Override
  public void invalidateAuth(@NotNull Operation operation, @NotNull Link auth) {
  }

  private static boolean isEmpty(@Nullable String value) {
    return value == null || value.isEmpty();
  }

  @NotNull
  private static String extractLogin(@Nullable String userInfo) {
    if (userInfo == null) return "";
    final int separator = userInfo.indexOf(':');
    return (separator >= 0) ? userInfo.substring(0, separator) : userInfo;
  }

  @NotNull
  private static String extractPassword(@Nullable String userInfo) {
    if (userInfo == null) return "";
    final int separator = userInfo.indexOf(':');
    return (separator >= 0) ? userInfo.substring(separator + 1) : "";
  }
}
