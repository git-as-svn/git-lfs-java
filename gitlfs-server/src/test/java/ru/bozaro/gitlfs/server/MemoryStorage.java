package ru.bozaro.gitlfs.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import ru.bozaro.gitlfs.client.auth.AuthProvider;
import ru.bozaro.gitlfs.client.auth.CachedAuthProvider;
import ru.bozaro.gitlfs.common.Constants;
import ru.bozaro.gitlfs.common.data.Link;
import ru.bozaro.gitlfs.common.data.Meta;
import ru.bozaro.gitlfs.common.data.Operation;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory storage.
 *
 * @author Artem V. Navrotskiy
 */
public class MemoryStorage implements ContentManager {
  @NotNull
  private final Map<String, byte[]> storage = new ConcurrentHashMap<>();
  @NotNull
  private final AtomicInteger tokenId = new AtomicInteger(0);
  private final int tokenMaxUsage;

  public MemoryStorage(int tokenMaxUsage) {
    this.tokenMaxUsage = tokenMaxUsage;
  }

  @Nullable
  @Override
  public Meta getMetadata(@NotNull String hash) throws IOException {
    final byte[] data = storage.get(hash);
    return data == null ? null : new Meta(hash, data.length);
  }

  @NotNull
  @Override
  public Downloader checkDownloadAccess(@NotNull HttpServletRequest request) throws IOException, ForbiddenError, UnauthorizedError {
    if (!getToken().equals(request.getHeader(Constants.HEADER_AUTHORIZATION))) {
      throw new UnauthorizedError("Basic realm=\"Test\"");
    }
    return new Downloader() {
      @NotNull
      public InputStream openObject(@NotNull String hash) throws IOException {
        final byte[] data = storage.get(hash);
        if (data == null) throw new FileNotFoundException();
        return new ByteArrayInputStream(data);
      }

      @Nullable
      public InputStream openObjectGzipped(@NotNull String hash) throws IOException {
        return null;
      }
    };
  }

  @NotNull
  private String getToken() {
    if (tokenMaxUsage > 0) {
      final int token = tokenId.incrementAndGet();
      return "Bearer Token-" + (token / tokenMaxUsage);
    } else {
      return "Bearer Token-" + tokenId.get();
    }
  }

  @NotNull
  @Override
  public Uploader checkUploadAccess(@NotNull HttpServletRequest request) throws IOException, ForbiddenError, UnauthorizedError {
    if (!getToken().equals(request.getHeader(Constants.HEADER_AUTHORIZATION))) {
      throw new UnauthorizedError("Basic realm=\"Test\"");
    }
    return new Uploader() {
      @Override
      public void saveObject(@NotNull Meta meta, @NotNull InputStream content) throws IOException {
        final byte[] data = ByteStreams.toByteArray(content);
        if (meta.getSize() >= 0) {
          Assert.assertEquals(meta.getSize(), data.length);
        }
        Assert.assertEquals(meta.getOid(), Hashing.sha256().hashBytes(data).toString());
        storage.put(meta.getOid(), data);
      }
    };
  }

  @NotNull
  public AuthProvider getAuthProvider(@NotNull URI href) {
    return new CachedAuthProvider() {
      @NotNull
      @Override
      protected Link getAuthUncached(@NotNull Operation operation) throws IOException, InterruptedException {
        return new Link(href, ImmutableMap.of(Constants.HEADER_AUTHORIZATION, getToken()), null);
      }
    };
  }
}
