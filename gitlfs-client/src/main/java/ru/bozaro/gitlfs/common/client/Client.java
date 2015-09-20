package ru.bozaro.gitlfs.common.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.common.client.exceptions.ForbiddenException;
import ru.bozaro.gitlfs.common.client.exceptions.RequestException;
import ru.bozaro.gitlfs.common.client.exceptions.UnauthorizedException;
import ru.bozaro.gitlfs.common.client.internal.Request;
import ru.bozaro.gitlfs.common.client.internal.Work;
import ru.bozaro.gitlfs.common.data.Auth;
import ru.bozaro.gitlfs.common.data.Link;
import ru.bozaro.gitlfs.common.data.Meta;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import static ru.bozaro.gitlfs.common.client.Constants.HEADER_LOCATION;
import static ru.bozaro.gitlfs.common.client.Constants.LINK_DOWNLOAD;

/**
 * Git LFS client.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class Client {
  private static final int MAX_AUTH_COUNT = 2;
  private static final int MAX_RETRY_COUNT = 2;
  private static final int MAX_REDIRECT_COUNT = 5;
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
  public Meta getMeta(@NotNull final String hash) throws IOException {
    return doWork(new Work<Meta>() {
      @Override
      public Meta exec(@NotNull Auth auth) throws IOException {
        return doRequest(auth, new ObjectsGet(), URI.create(auth.getHref() + Constants.OBJECTS + "/" + hash));
      }
    }, AuthProvider.Mode.Download);
  }

  /**
   * Download object by hash.
   *
   * @param hash Object hash.
   * @return Object stream.
   * @throws IOException
   */
  @NotNull
  public InputStream openObject(@NotNull final String hash) throws IOException {
    return doWork(new Work<InputStream>() {
      @Override
      public InputStream exec(@NotNull Auth auth) throws IOException {
        return openObject(doRequest(auth, new ObjectsGet(), URI.create(auth.getHref() + Constants.OBJECTS + "/" + hash)));
      }
    }, AuthProvider.Mode.Download);
  }

  /**
   * Download object by metadata.
   *
   * @param meta Object metadata.
   * @return Object stream.
   * @throws IOException
   */
  @NotNull
  public InputStream openObject(@NotNull final Meta meta) throws IOException {
    final Link link = meta.getLinks().get(LINK_DOWNLOAD);
    if ((link == null) || (link.getHref() == null)) {
      throw new FileNotFoundException();
    }
    return doRequest(null, new Request<HttpMethod, InputStream>() {
      @NotNull
      @Override
      public HttpMethod createRequest(@NotNull String url) {
        final GetMethod request = new GetMethod(url);
        addHeaders(request, link);
        return request;
      }

      @Override
      public InputStream processResponse(@NotNull HttpMethod request) throws IOException {
        return request.getResponseBodyAsStream();
      }
    }, link.getHref());
  }

  private <T> T doWork(@NotNull Work<T> work, @NotNull AuthProvider.Mode mode) throws IOException {
    Auth auth = authProvider.getAuth(mode);
    int authCount = 0;
    while (true) {
      try {
        return work.exec(auth);
      } catch (UnauthorizedException e) {
        if (authCount >= MAX_AUTH_COUNT) {
          throw e;
        }
        authCount++;
        // Get new authentication data.
        authProvider.invalidateAuth(AuthProvider.Mode.Download, auth);
        final Auth newAuth = authProvider.getAuth(AuthProvider.Mode.Download);
        if (newAuth.getHeader().equals(auth.getHeader())) {
          throw e;
        }
        auth = newAuth;
      }
    }
  }

  private <T extends HttpMethod, R> R doRequest(@Nullable Auth auth, @NotNull Request<T, R> task, @NotNull URI url) throws IOException {
    int redirectCount = 0;
    int retryCount = 0;
    while (true) {
      final T request = task.createRequest(url.toString());
      addHeaders(request, auth);
      http.executeMethod(request);
      switch (request.getStatusCode()) {
        case HttpStatus.SC_UNAUTHORIZED:
          throw new UnauthorizedException(request);
        case HttpStatus.SC_FORBIDDEN:
          throw new ForbiddenException(request);
        case HttpStatus.SC_MOVED_PERMANENTLY:
        case HttpStatus.SC_MOVED_TEMPORARILY:
        case HttpStatus.SC_SEE_OTHER:
        case HttpStatus.SC_TEMPORARY_REDIRECT:
          // Follow by redirect.
          final String location = request.getRequestHeader(HEADER_LOCATION).getValue();
          if (location == null || redirectCount >= MAX_REDIRECT_COUNT) {
            throw new RequestException(request);
          }
          ++redirectCount;
          url = url.resolve(location);
          continue;
        case HttpStatus.SC_BAD_GATEWAY:
        case HttpStatus.SC_GATEWAY_TIMEOUT:
        case HttpStatus.SC_SERVICE_UNAVAILABLE:
        case HttpStatus.SC_INTERNAL_SERVER_ERROR:
          // Temporary error. need to retry.
          if (retryCount >= MAX_RETRY_COUNT) {
            throw new RequestException(request);
          }
          ++retryCount;
          continue;
      }
      return task.processResponse(request);
    }
  }

  private void addHeaders(@NotNull HttpMethod req, @Nullable Link link) {
    if (link != null) {
      for (Map.Entry<String, String> entry : link.getHeader().entrySet()) {
        req.addRequestHeader(entry.getKey(), entry.getValue());
      }
    }
  }

  private class ObjectsGet implements Request<GetMethod, Meta> {
    @NotNull
    @Override
    public GetMethod createRequest(@NotNull String url) {
      final GetMethod req = new GetMethod(url);
      req.addRequestHeader(Constants.HEADER_ACCEPT, Constants.MIME_TYPE);
      return req;
    }

    @Override
    public Meta processResponse(@NotNull GetMethod request) throws IOException {
      switch (request.getStatusCode()) {
        case HttpStatus.SC_OK:
          return mapper.readValue(request.getResponseBodyAsStream(), Meta.class);
        case HttpStatus.SC_NOT_FOUND:
          return null;
        default:
          throw new RequestException(request);
      }
    }
  }
}
