package ru.bozaro.gitlfs.server;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import ru.bozaro.gitlfs.common.data.Meta;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple in-memory storage.
 *
 * @author Artem V. Navrotskiy
 */
public class MemoryStorage implements ContentManager {
  @NotNull
  private final Map<String, byte[]> storage = new HashMap<>();

  @Nullable
  @Override
  public Meta getMetadata(@NotNull String hash) throws IOException {
    final byte[] data = storage.get(hash);
    return data == null ? null : new Meta(hash, data.length);
  }

  @NotNull
  @Override
  public Downloader checkDownloadAccess(@NotNull HttpServletRequest request) throws IOException, ForbiddenError, UnauthorizedError {
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
  @Override
  public Uploader checkUploadAccess(@NotNull HttpServletRequest request) throws IOException, ForbiddenError, UnauthorizedError {
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
}
