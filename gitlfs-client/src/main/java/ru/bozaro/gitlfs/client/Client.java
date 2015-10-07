package ru.bozaro.gitlfs.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.client.auth.AuthProvider;
import ru.bozaro.gitlfs.client.exceptions.ForbiddenException;
import ru.bozaro.gitlfs.client.exceptions.RequestException;
import ru.bozaro.gitlfs.client.exceptions.UnauthorizedException;
import ru.bozaro.gitlfs.client.internal.*;
import ru.bozaro.gitlfs.client.io.StreamProvider;
import ru.bozaro.gitlfs.common.data.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static ru.bozaro.gitlfs.client.Constants.*;

/**
 * Git LFS client.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class Client {
  private static final int MAX_AUTH_COUNT = 1;
  private static final int MAX_RETRY_COUNT = 2;
  private static final int MAX_REDIRECT_COUNT = 5;
  @NotNull
  private static final int[] DEFAULT_HTTP_SUCCESS = new int[]{
      HttpStatus.SC_OK
  };
  @NotNull
  private final ObjectMapper mapper;
  @NotNull
  private final AuthProvider authProvider;
  @NotNull
  private final HttpExecutor http;

  public Client(@NotNull AuthProvider authProvider) {
    this(authProvider, new HttpClient());
  }

  public Client(@NotNull AuthProvider authProvider, @NotNull final HttpClient http) {
    this(authProvider, new HttpClientExecutor(http));
  }

  public Client(@NotNull AuthProvider authProvider, @NotNull HttpExecutor http) {
    this.authProvider = authProvider;
    this.mapper = createMapper();
    this.http = http;
  }

  @NotNull
  public static ObjectMapper createMapper() {
    final ObjectMapper mapper = new ObjectMapper();
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    return mapper;
  }

  /**
   * Get metadata for object by hash.
   *
   * @param hash Object hash.
   * @return Object metadata or null, if object not found.
   * @throws IOException
   */
  @Nullable
  public ObjectRes getMeta(@NotNull final String hash) throws IOException {
    return doWork(new Work<ObjectRes>() {
      @Override
      public ObjectRes exec(@NotNull Link auth) throws IOException {
        return doRequest(auth, new MetaGet(), AuthHelper.join(auth.getHref(), Constants.PATH_OBJECTS + "/" + hash));
      }
    }, Operation.Download);
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
  public ObjectRes postMeta(@NotNull final String hash, final long size) throws IOException {
    return postMeta(new Meta(hash, size));
  }

  /**
   * Get metadata for object by hash.
   *
   * @param meta Object meta.
   * @return Object metadata or null, if object not found.
   * @throws IOException
   */
  @Nullable
  public ObjectRes postMeta(@NotNull final Meta meta) throws IOException {
    return doWork(new Work<ObjectRes>() {
      @Override
      public ObjectRes exec(@NotNull Link auth) throws IOException {
        return doRequest(auth, new MetaPost(meta), AuthHelper.join(auth.getHref(), PATH_OBJECTS));
      }
    }, Operation.Upload);
  }

  /**
   * Send batch request to the LFS-server.
   *
   * @param batchReq Batch request.
   * @return Object metadata.
   * @throws IOException
   */
  @NotNull
  public BatchRes postBatch(@NotNull final BatchReq batchReq) throws IOException {
    return doWork(new Work<BatchRes>() {
      @Override
      public BatchRes exec(@NotNull Link auth) throws IOException {
        return doRequest(auth, new JsonPost<>(batchReq, BatchRes.class), AuthHelper.join(auth.getHref(), PATH_BATCH));
      }
    }, batchReq.getOperation());
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
      public InputStream exec(@NotNull Link auth) throws IOException {
        final ObjectRes links = doRequest(auth, new MetaGet(), AuthHelper.join(auth.getHref(), PATH_OBJECTS + "/" + hash));
        if (links == null) {
          throw new FileNotFoundException();
        }
        return getObject(links);
      }
    }, Operation.Download);
  }

  /**
   * Download object by metadata.
   *
   * @param links Object links.
   * @return Object stream.
   * @throws FileNotFoundException File not found exception if object don't exists on LFS server.
   * @throws IOException           On some errors.
   */
  @NotNull
  public InputStream getObject(@NotNull final Links links) throws IOException {
    final Link link = links.getLinks().get(LINK_DOWNLOAD);
    if (link == null) {
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
    return putObject(streamProvider, generateMeta(streamProvider));
  }

  /**
   * Generate object metadata.
   *
   * @param streamProvider Object stream provider.
   * @return Return object metadata.
   * @throws IOException On some errors.
   */
  public Meta generateMeta(@NotNull final StreamProvider streamProvider) throws IOException {
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
    return new Meta(new String(Hex.encodeHex(digest.digest())), size);
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
    return putObject(streamProvider, new Meta(hash, size));
  }

  /**
   * Upload object with specified hash and size.
   *
   * @param streamProvider Object stream provider.
   * @param meta           Object metadata.
   * @return Return true is object is uploaded successfully and false if object is already uploaded.
   * @throws IOException On some errors.
   */
  public boolean putObject(@NotNull final StreamProvider streamProvider, @NotNull final Meta meta) throws IOException {
    return doWork(new Work<Boolean>() {
      @Override
      public Boolean exec(@NotNull Link auth) throws IOException {
        final ObjectRes links = doRequest(auth, new MetaPost(meta), AuthHelper.join(auth.getHref(), PATH_OBJECTS));
        return links != null && putObject(streamProvider, meta, links);
      }
    }, Operation.Upload);
  }

  /**
   * Upload object by metadata.
   *
   * @param links          Object links.
   * @param streamProvider Object stream provider.
   * @param meta           Object metadata.
   * @return Return true is object is uploaded successfully and false if object is already uploaded.
   * @throws IOException On some errors.
   */
  public boolean putObject(@NotNull final StreamProvider streamProvider, @NotNull final Meta meta, @NotNull final Links links) throws IOException {
    if (links.getLinks().containsKey(LINK_DOWNLOAD)) {
      return false;
    }
    final Link uploadLink = links.getLinks().get(LINK_UPLOAD);
    if (uploadLink == null) {
      throw new IOException("Upload link not found");
    }
    doRequest(uploadLink, new ObjectPut(streamProvider, meta.getSize()), uploadLink.getHref());

    final Link verifyLink = links.getLinks().get(LINK_VERIFY);
    if (verifyLink != null) {
      doRequest(verifyLink, new ObjectVerify(meta), verifyLink.getHref());
    }
    return true;
  }

  protected <T> T doWork(@NotNull Work<T> work, @NotNull Operation operation) throws IOException {
    Link auth = authProvider.getAuth(operation);
    int authCount = 0;
    while (true) {
      try {
        return work.exec(auth);
      } catch (UnauthorizedException | ForbiddenException e) {
        if (authCount >= MAX_AUTH_COUNT) {
          throw e;
        }
        authCount++;
        // Get new authentication data.
        authProvider.invalidateAuth(operation, auth);
        final Link newAuth = authProvider.getAuth(operation);
        if (newAuth.getHeader().equals(auth.getHeader()) && newAuth.getHref().equals(auth.getHref())) {
          throw e;
        }
        auth = newAuth;
      }
    }
  }

  protected <T extends HttpMethod, R> R doRequest(@Nullable Link link, @NotNull Request<R> task, @NotNull URI url) throws IOException {
    int redirectCount = 0;
    int retryCount = 0;
    while (true) {
      final HttpMethod request = task.createRequest(mapper, url.toString());
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
      int[] success = task.statusCodes();
      if (success == null) {
        success = DEFAULT_HTTP_SUCCESS;
      }
      for (int item : success) {
        if (request.getStatusCode() == item) {
          return task.processResponse(mapper, request);
        }
      }
      // Unexpected status code.
      throw new RequestException(request);
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
