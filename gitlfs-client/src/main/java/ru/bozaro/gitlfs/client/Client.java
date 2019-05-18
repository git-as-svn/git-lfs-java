package ru.bozaro.gitlfs.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.bozaro.gitlfs.client.auth.AuthProvider;
import ru.bozaro.gitlfs.client.exceptions.ForbiddenException;
import ru.bozaro.gitlfs.client.exceptions.RequestException;
import ru.bozaro.gitlfs.client.exceptions.UnauthorizedException;
import ru.bozaro.gitlfs.client.internal.*;
import ru.bozaro.gitlfs.client.io.StreamHandler;
import ru.bozaro.gitlfs.client.io.StreamProvider;
import ru.bozaro.gitlfs.common.JsonHelper;
import ru.bozaro.gitlfs.common.LockConflictException;
import ru.bozaro.gitlfs.common.VerifyLocksResult;
import ru.bozaro.gitlfs.common.data.*;
import ru.bozaro.gitlfs.common.io.InputStreamValidator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.bozaro.gitlfs.common.Constants.*;

/**
 * Git LFS client.
 *
 * @author Artem V. Navrotskiy
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
    this(authProvider, HttpClients.createDefault());
  }

  public Client(@NotNull AuthProvider authProvider, @NotNull final HttpClient http) {
    this(authProvider, new HttpClientExecutor(http));
  }

  public Client(@NotNull AuthProvider authProvider, @NotNull HttpExecutor http) {
    this.authProvider = authProvider;
    this.mapper = JsonHelper.mapper;
    this.http = http;
  }

  @NotNull
  public AuthProvider getAuthProvider() {
    return authProvider;
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
    return doWork(auth -> doRequest(
        auth,
        new MetaGet(),
        AuthHelper.join(auth.getHref(), PATH_OBJECTS + "/", hash)
    ), Operation.Download);
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
    return doWork(auth -> doRequest(
        auth,
        new MetaPost(meta),
        AuthHelper.join(auth.getHref(), PATH_OBJECTS)),
        Operation.Upload
    );
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
    return doWork(auth -> doRequest(
        auth,
        new JsonPost<>(batchReq, BatchRes.class),
        AuthHelper.join(auth.getHref(), PATH_BATCH)),
        batchReq.getOperation()
    );
  }

  /**
   * Download object by hash.
   *
   * @param hash    Object hash.
   * @param handler Stream handler.
   * @return Stream handler result.
   * @throws FileNotFoundException File not found exception if object don't exists on LFS server.
   * @throws IOException           On some errors.
   */
  @NotNull
  public <T> T getObject(@NotNull final String hash, @NotNull final StreamHandler<T> handler) throws IOException {
    return doWork(auth -> {
      final ObjectRes links = doRequest(
          auth,
          new MetaGet(),
          AuthHelper.join(auth.getHref(), PATH_OBJECTS + "/", hash)
      );
      if (links == null) {
        throw new FileNotFoundException();
      }
      return getObject(new Meta(hash, -1), links, handler);
    }, Operation.Download);
  }

  /**
   * Download object by metadata.
   *
   * @param meta    Object metadata for stream validation.
   * @param links   Object links.
   * @param handler Stream handler.
   * @return Stream handler result.
   * @throws FileNotFoundException File not found exception if object don't exists on LFS server.
   * @throws IOException           On some errors.
   */
  @NotNull
  public <T> T getObject(@Nullable final Meta meta, @NotNull final Links links, @NotNull final StreamHandler<T> handler) throws IOException {
    final Link link = links.getLinks().get(LinkType.Download);
    if (link == null) {
      throw new FileNotFoundException();
    }
    return doRequest(link, new ObjectGet<>(inputStream -> handler.accept(meta != null ? new InputStreamValidator(inputStream, meta) : inputStream)), link.getHref());
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
  public static Meta generateMeta(@NotNull final StreamProvider streamProvider) throws IOException {
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
    return doWork(auth -> {
      final ObjectRes links = doRequest(auth, new MetaPost(meta), AuthHelper.join(auth.getHref(), PATH_OBJECTS));
      return links != null && putObject(streamProvider, meta, links);
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
    if (links.getLinks().containsKey(LinkType.Download)) {
      return false;
    }
    final Link uploadLink = links.getLinks().get(LinkType.Upload);
    if (uploadLink == null) {
      throw new IOException("Upload link not found");
    }
    doRequest(uploadLink, new ObjectPut(streamProvider, meta.getSize()), uploadLink.getHref());

    final Link verifyLink = links.getLinks().get(LinkType.Verify);
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

  public <R> R doRequest(@Nullable Link link, @NotNull Request<R> task, @NotNull URI url) throws IOException {
    int redirectCount = 0;
    int retryCount = 0;
    while (true) {
      final HttpUriRequest request = task.createRequest(mapper, url.toString());
      addHeaders(request, link);
      final HttpResponse response = http.executeMethod(request);
      try {
        int[] success = task.statusCodes();
        if (success == null) {
          success = DEFAULT_HTTP_SUCCESS;
        }
        for (int item : success) {
          if (response.getStatusLine().getStatusCode() == item) {
            return task.processResponse(mapper, response);
          }
        }
        switch (response.getStatusLine().getStatusCode()) {
          case HttpStatus.SC_UNAUTHORIZED:
            throw new UnauthorizedException(request, response);
          case HttpStatus.SC_FORBIDDEN:
            throw new ForbiddenException(request, response);
          case HttpStatus.SC_MOVED_PERMANENTLY:
          case HttpStatus.SC_MOVED_TEMPORARILY:
          case HttpStatus.SC_SEE_OTHER:
          case HttpStatus.SC_TEMPORARY_REDIRECT:
            // Follow by redirect.
            final String location = response.getFirstHeader(HEADER_LOCATION).getValue();
            if (location == null || redirectCount >= MAX_REDIRECT_COUNT) {
              throw new RequestException(request, response);
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
              throw new RequestException(request, response);
            }
            ++retryCount;
            continue;
        }
        // Unexpected status code.
        throw new RequestException(request, response);
      } finally {
        request.abort();
      }
    }
  }

  protected void addHeaders(@NotNull HttpUriRequest req, @Nullable Link link) {
    if (link != null) {
      for (Map.Entry<String, String> entry : link.getHeader().entrySet()) {
        req.setHeader(entry.getKey(), entry.getValue());
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

  @NotNull
  public Lock lock(@NotNull String path, @Nullable Ref ref) throws IOException, LockConflictException {
    final LockCreate.Res res = doWork(auth -> doRequest(
        auth,
        new LockCreate(path, ref),
        AuthHelper.join(auth.getHref(), PATH_LOCKS)),
        Operation.Upload
    );

    if (res.isSuccess())
      return res.getLock();
    else
      throw new LockConflictException(res.getLock());
  }

  @Nullable
  public Lock unlock(@NotNull Lock lock, boolean force, @Nullable Ref ref) throws IOException {
    return unlock(lock.getId(), force, ref);
  }

  @Nullable
  public Lock unlock(@NotNull String lockId, boolean force, @Nullable Ref ref) throws IOException {
    return doWork(auth -> doRequest(
        auth,
        new LockDelete(force, ref),
        AuthHelper.join(auth.getHref(), PATH_LOCKS + "/", lockId + "/unlock")),
        Operation.Upload
    );
  }

  @NotNull
  public List<Lock> listLocks(@Nullable String path, @Nullable String id, @Nullable Ref ref) throws IOException {
    final List<Lock> result = new ArrayList<>();

    if (path == null)
      path = "";
    if (id == null)
      id = "";

    final String params = "?path=" + URLEncoder.encode(path, "UTF-8")
        + "&id=" + URLEncoder.encode(id, "UTF-8")
        + "&refspec=" + URLEncoder.encode(ref == null ? "" : ref.getName(), "UTF-8");

    String cursor = "";
    do {
      final String cursorFinal = cursor;
      final LocksRes res = doWork(auth -> doRequest(
          auth,
          new LocksList(),
          AuthHelper.join(
              auth.getHref(),
              PATH_LOCKS + params + "&cursor=" + URLEncoder.encode(cursorFinal, "UTF-8"))
          ),
          Operation.Download
      );
      result.addAll(res.getLocks());
      cursor = res.getNextCursor();
    } while (cursor != null && !cursor.isEmpty());

    return result;
  }

  @NotNull
  public VerifyLocksResult verifyLocks(@Nullable Ref ref) throws IOException {
    final VerifyLocksResult result = new VerifyLocksResult(new ArrayList<>(), new ArrayList<>());

    String cursor = null;
    do {
      final String cursorFinal = cursor;
      final VerifyLocksRes res = doWork(auth -> doRequest(
          auth,
          new JsonPost<>(new VerifyLocksReq(cursorFinal, ref, null), VerifyLocksRes.class),
          AuthHelper.join(auth.getHref(), PATH_LOCKS + "/verify")),
          Operation.Upload
      );
      result.getOurLocks().addAll(res.getOurs());
      result.getTheirLocks().addAll(res.getTheirs());
      cursor = res.getNextCursor();
    } while (cursor != null && !cursor.isEmpty());

    return result;
  }
}
