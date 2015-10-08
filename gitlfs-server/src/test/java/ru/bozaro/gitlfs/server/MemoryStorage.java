package ru.bozaro.gitlfs.server;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import ru.bozaro.gitlfs.common.data.Meta;
import ru.bozaro.gitlfs.common.data.Operation;

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
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class MemoryStorage implements ContentManager<AuthContext> {
  @NotNull
  private final Map<String, byte[]> storage = new HashMap<>();

  @Override
  public AuthContext checkAccess(@NotNull HttpServletRequest request, @NotNull Operation operation) throws IOException, ServerError {
    return new AuthContext(operation);
  }

  @Nullable
  @Override
  public Meta getMetadata(@NotNull String hash) throws IOException {
    final byte[] data = storage.get(hash);
    return data == null ? null : new Meta(hash, data.length);
  }

  @NotNull
  @Override
  public InputStream openObject(AuthContext context, @NotNull String hash) throws IOException {
    final byte[] data = storage.get(hash);
    if (data == null) throw new FileNotFoundException();
    return new ByteArrayInputStream(data);
  }

  @Nullable
  @Override
  public InputStream openObjectGzipped(AuthContext context, @NotNull String hash) throws IOException {
    return null;
  }

  @Override
  public void saveObject(AuthContext context, @NotNull Meta meta, @NotNull InputStream content) throws IOException {
    Assert.assertNotNull(context);
    Assert.assertEquals(Operation.Upload, context.getOperation());
    final byte[] data = ByteStreams.toByteArray(content);
    if (meta.getSize() >= 0) {
      Assert.assertEquals(meta.getSize(), data.length);
    }
    Assert.assertEquals(meta.getOid(), Hashing.sha256().hashBytes(data).toString());
    storage.put(meta.getOid(), data);
  }
}
