package ru.bozaro.gitlfs.common.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.common.client.exceptions.ForbiddenException;
import ru.bozaro.gitlfs.common.client.exceptions.RequestException;
import ru.bozaro.gitlfs.common.client.exceptions.UnauthorizedException;
import ru.bozaro.gitlfs.common.client.internal.*;
import ru.bozaro.gitlfs.common.data.Auth;
import ru.bozaro.gitlfs.common.data.Link;
import ru.bozaro.gitlfs.common.data.Meta;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static ru.bozaro.gitlfs.common.client.Constants.*;

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

  public Client(@NotNull AuthProvider authProvider) {
    this(authProvider, new HttpClient());
  }

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
        return doRequest(auth, new MetaGet(), URI.create(auth.getHref() + OBJECTS + "/" + hash));
      }
    }, AuthAccess.Download);
  }

  /**
   * Get metadata for object by hash.
   *
   * @param hash Object hash.
   * @param size Object size.
   * @return Object metadata or null, if object not found.
   * @throws IOException
   */
  @Nullable
  public Meta postMeta(@NotNull final String hash, final long size) throws IOException {
    return doWork(new Work<Meta>() {
      @Override
      public Meta exec(@NotNull Auth auth) throws IOException {
        return doRequest(auth, new MetaPost(hash, size), URI.create(auth.getHref() + OBJECTS));
      }
    }, AuthAccess.Upload);
  }

  /**
   * Download object by hash.
   *
   * @param hash Object hash.
   * @return Object stream.
   * @throws FileNotFoundException File not found exception if object don't exists on LFS server.
   * @throws IOException           On some errors.
   */
  @NotNull
  public InputStream getObject(@NotNull final String hash) throws IOException {
    return doWork(new Work<InputStream>() {
      @Override
      public InputStream exec(@NotNull Auth auth) throws IOException {
        return getObject(doRequest(auth, new MetaGet(), URI.create(auth.getHref() + OBJECTS + "/" + hash)));
      }
    }, AuthAccess.Download);
  }

  /**
   * Download object by metadata.
   *
   * @param meta Object metadata.
   * @return Object stream.
   * @throws FileNotFoundException File not found exception if object don't exists on LFS server.
   * @throws IOException           On some errors.
   */
  @NotNull
  public InputStream getObject(@NotNull final Meta meta) throws IOException {
    final Link link = meta.getLinks().get(LINK_DOWNLOAD);
    if ((link == null) || (link.getHref() == null)) {
      throw new FileNotFoundException();
    }
    return doRequest(link, new ObjectGet(), link.getHref());
  }

  /**
   * Upload object.
   *
   * @param streamProvider Object stream provider.
   * @return Return true is object is uploaded successfully and false if object is already uploaded.
   * @throws IOException On some errors.
   */
  public boolean putObject(@NotNull final StreamProvider streamProvider) throws IOException {
    final MessageDigest digest = sha256();
    final byte[] buffer = new byte[0x10000];
    long size = 0;
    try (InputStream stream = streamProvider.getStream()) {
      while (true) {
        int read = stream.read(buffer);
        if (read <= 0) break;
        digest.update(buffer, 0, read);
        size += read;
      }
    }
    final String hash = new String(Hex.encodeHex(digest.digest()));
    return putObject(streamProvider, hash, size);
  }

  /**
   * Upload object with specified hash and size.
   *
   * @param streamProvider Object stream provider.
   * @param hash           Object hash.
   * @param size           Object size.
   * @return Return true is object is uploaded successfully and false if object is already uploaded.
   * @throws IOException On some errors.
   */
  public boolean putObject(@NotNull final StreamProvider streamProvider, @NotNull final String hash, final long size) throws IOException {
    return doWork(new Work<Boolean>() {
      @Override
      public Boolean exec(@NotNull Auth auth) throws IOException {
        return putObject(doRequest(auth, new MetaPost(hash, size), URI.create(auth.getHref() + OBJECTS)), streamProvider);
      }
    }, AuthAccess.Upload);
  }

  /**
   * Upload object by metadata.
   *
   * @param meta           Object metadata.
   * @param streamProvider Object stream provider.
   * @return Return true is object is uploaded successfully and false if object is already uploaded.
   * @throws IOException On some errors.
   */
  public boolean putObject(@NotNull final Meta meta, @NotNull final StreamProvider streamProvider) throws IOException {
    if (meta.getLinks().containsKey(LINK_DOWNLOAD)) {
      return false;
    }
    final Link uploadLink = meta.getLinks().get(LINK_UPLOAD);
    if ((uploadLink == null) || (uploadLink.getHref() == null)) {
      throw new IOException("Upload link not found");
    }
    doRequest(uploadLink, new ObjectPut(streamProvider), uploadLink.getHref());

    final Link verifyLink = meta.getLinks().get(LINK_VERIFY);
    if (verifyLink != null && verifyLink.getHref() != null) {
      doRequest(verifyLink, new ObjectVerify(), verifyLink.getHref());
    }
    return true;
  }

  protected <T> T doWork(@NotNull Work<T> work, @NotNull AuthAccess mode) throws IOException {
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
        authProvider.invalidateAuth(AuthAccess.Download, auth);
        final Auth newAuth = authProvider.getAuth(AuthAccess.Download);
        if (newAuth.getHeader().equals(auth.getHeader())) {
          throw e;
        }
        auth = newAuth;
      }
    }
  }

  protected <T extends HttpMethod, R> R doRequest(@Nullable Link link, @NotNull Request<T, R> task, @NotNull URI url) throws IOException {
    int redirectCount = 0;
    int retryCount = 0;
    while (true) {
      final T request = task.createRequest(mapper, url.toString());
      addHeaders(request, link);
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
      return task.processResponse(mapper, request);
    }
  }

  protected void addHeaders(@NotNull HttpMethod req, @Nullable Link link) {
    if (link != null) {
      for (Map.Entry<String, String> entry : link.getHeader().entrySet()) {
        req.addRequestHeader(entry.getKey(), entry.getValue());
      }
    }
  }

  protected static MessageDigest sha256() {
    try {
      return MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}