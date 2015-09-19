package ru.bozaro.gitlfs.common.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.common.client.exceptions.ForbiddenException;
import ru.bozaro.gitlfs.common.client.exceptions.HttpException;
import ru.bozaro.gitlfs.common.client.exceptions.UnauthorizedException;
import ru.bozaro.gitlfs.common.data.Auth;
import ru.bozaro.gitlfs.common.data.Meta;

import java.io.IOException;
import java.util.Map;

import static ru.bozaro.gitlfs.common.client.Constants.HEADER_LOCATION;

/**
 * Git LFS client.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class Client {
  private static final int MAX_RETRY = 5;
  @NotNull
  private final ObjectMapper mapper;
  @NotNull
  private final AuthProvider authProvider;
  @NotNull
  private final HttpClient http;

  public Client(@NotNull AuthProvider authProvider, @NotNull HttpClient http) {
    this.authProvider = authProvider;
    this.mapper = createMapper();
    this.http = http;
  }

  @NotNull
  public static ObjectMapper createMapper() {
    return new ObjectMapper();
  }

  /**
   * Get metadata for object by hash.
   *
   * @param hash Object hash.
   * @return Object metadata or null, if object not found.
   * @throws IOException
   */
  @Nullable
  public Meta getMeta(@NotNull String hash) throws IOException {
    Auth auth = authProvider.getAuth(AuthProvider.Mode.Download);
    String href = auth.getHref() + Constants.OBJECTS + "/" + hash;
    for (int pass = 0; ; ++pass) {
      final GetMethod req = new GetMethod(href);
      req.addRequestHeader(Constants.HEADER_ACCEPT, Constants.MIME_TYPE);
      addHeaders(req, auth);
      http.executeMethod(req);
      if (req.getStatusCode() == HttpStatus.SC_OK) {
        return mapper.readValue(req.getResponseBodyAsStream(), Meta.class);
      }
      if (req.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
        return null;
      }
      href = getNextUrl(req, href);
      final HttpException exception = createException(req);
      if (pass >= MAX_RETRY || exception.isPermanent()) {
        throw exception;
      }
      if (req.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
        auth = updateAuth(req, auth, AuthProvider.Mode.Download);
      }
    }
  }

  private void addHeaders(@NotNull HttpMethod req, @NotNull Auth auth) {
    for (Map.Entry<String, String> entry : auth.getHeader().entrySet()) {
      req.addRequestHeader(entry.getKey(), entry.getValue());
    }
  }

  @NotNull
  private Auth updateAuth(@NotNull HttpMethod req, @NotNull Auth oldAuth, @NotNull AuthProvider.Mode mode) throws IOException {
    final Auth newAuth = authProvider.getAuth(mode);
    if (newAuth.getHeader().equals(oldAuth.getHeader())) {
      throw new UnauthorizedException(req);
    }
    return newAuth;
  }

  @NotNull
  private String getNextUrl(@NotNull HttpMethod req, @NotNull String href) {
    switch (req.getStatusCode()) {
      case HttpStatus.SC_MOVED_PERMANENTLY:
      case HttpStatus.SC_MOVED_TEMPORARILY:
      case HttpStatus.SC_SEE_OTHER:
      case HttpStatus.SC_TEMPORARY_REDIRECT:
        final String location = req.getRequestHeader(HEADER_LOCATION).getValue();
        return location == null ? href : location;
      default:
        return href;
    }
  }

  @NotNull
  private HttpException createException(@NotNull HttpMethod req) {
    switch (req.getStatusCode()) {
      case HttpStatus.SC_UNAUTHORIZED:
        return new UnauthorizedException(req);
      case HttpStatus.SC_FORBIDDEN:
        return new ForbiddenException(req);
      case HttpStatus.SC_BAD_REQUEST:
      case HttpStatus.SC_METHOD_NOT_ALLOWED:
      case HttpStatus.SC_NOT_ACCEPTABLE:
      case HttpStatus.SC_NOT_IMPLEMENTED:
        return new HttpException(req, true);
      default:
        return new HttpException(req, false);
    }
  }
}
