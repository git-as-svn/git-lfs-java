package ru.bozaro.gitlfs.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import org.testng.Assert;
import ru.bozaro.gitlfs.client.Client;
import ru.bozaro.gitlfs.client.auth.AuthProvider;
import ru.bozaro.gitlfs.client.auth.CachedAuthProvider;
import ru.bozaro.gitlfs.client.io.StreamProvider;
import ru.bozaro.gitlfs.common.Constants;
import ru.bozaro.gitlfs.common.data.Link;
import ru.bozaro.gitlfs.common.data.Meta;
import ru.bozaro.gitlfs.common.data.Operation;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
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
  @Nonnull
  private final Map<String, byte[]> storage = new ConcurrentHashMap<>();
  @Nonnull
  private final AtomicInteger tokenId = new AtomicInteger(0);
  private final int tokenMaxUsage;

  public MemoryStorage(int tokenMaxUsage) {
    this.tokenMaxUsage = tokenMaxUsage;
  }

  @Nonnull
  @Override
  public Downloader checkDownloadAccess(@Nonnull HttpServletRequest request) throws UnauthorizedError {
    if (!getToken().equals(request.getHeader(Constants.HEADER_AUTHORIZATION))) {
      throw new UnauthorizedError("Basic realm=\"Test\"");
    }
    return new Downloader() {
      @Nonnull
      public InputStream openObject(@Nonnull String hash) throws IOException {
        final byte[] data = storage.get(hash);
        if (data == null) throw new FileNotFoundException();
        return new ByteArrayInputStream(data);
      }

      @CheckForNull
      public InputStream openObjectGzipped(@Nonnull String hash) {
        return null;
      }
    };
  }

  @Nonnull
  private String getToken() {
    if (tokenMaxUsage > 0) {
      final int token = tokenId.incrementAndGet();
      return "Bearer Token-" + (token / tokenMaxUsage);
    } else {
      return "Bearer Token-" + tokenId.get();
    }
  }

  @Nonnull
  @Override
  public Uploader checkUploadAccess(@Nonnull HttpServletRequest request) throws UnauthorizedError {
    if (!getToken().equals(request.getHeader(Constants.HEADER_AUTHORIZATION))) {
      throw new UnauthorizedError("Basic realm=\"Test\"");
    }
    return this::saveObject;
  }

  @CheckForNull
  @Override
  public Meta getMetadata(@Nonnull String hash) {
    final byte[] data = storage.get(hash);
    return data == null ? null : new Meta(hash, data.length);
  }

  public void saveObject(@Nonnull Meta meta, @Nonnull InputStream content) throws IOException {
    final byte[] data = ByteStreams.toByteArray(content);
    if (meta.getSize() >= 0) {
      Assert.assertEquals(meta.getSize(), data.length);
    }
    Assert.assertEquals(meta.getOid(), Hashing.sha256().hashBytes(data).toString());
    storage.put(meta.getOid(), data);
  }

  public void saveObject(@Nonnull StreamProvider provider) throws IOException {
    final Meta meta = Client.generateMeta(provider);
    try (InputStream stream = provider.getStream()) {
      saveObject(meta, stream);
    }
  }

  @CheckForNull
  public byte[] getObject(@Nonnull String oid) {
    return storage.get(oid);
  }

  @Nonnull
  public AuthProvider getAuthProvider(@Nonnull URI href) {
    return new CachedAuthProvider() {
      @Nonnull
      @Override
      protected Link getAuthUncached(@Nonnull Operation operation) {
        return new Link(href, ImmutableMap.of(Constants.HEADER_AUTHORIZATION, getToken()), null);
      }
    };
  }
}
