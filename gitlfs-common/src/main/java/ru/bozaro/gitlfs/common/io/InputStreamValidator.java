package ru.bozaro.gitlfs.common.io;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.common.data.Meta;

import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapper for validating hash and size of uploading object.
 *
 * @author Artem V. Navrotskiy
 */
public class InputStreamValidator extends InputStream {
  @NotNull
  private final Hasher hasher;
  @NotNull
  private final InputStream stream;
  @NotNull
  private final Meta meta;

  private boolean eof;
  private long totalSize;

  public InputStreamValidator(@NotNull InputStream stream, @NotNull Meta meta) {
    this.hasher = Hashing.sha256().newHasher();
    this.stream = stream;
    this.meta = meta;
    this.eof = false;
    this.totalSize = 0;
  }

  @Override
  public int read() throws IOException {
    if (eof) {
      return -1;
    }
    final int data = stream.read();
    if (data >= 0) {
      hasher.putByte((byte) data);
      checkSize(1);
    } else {
      checkSize(-1);
    }
    return data;
  }

  private void checkSize(int size) throws IOException {
    if (size > 0) {
      totalSize += size;
    }
    if ((meta.getSize() > 0 && totalSize > meta.getSize())) {
      throw new IOException("Input stream too big");
    }
    if (size < 0) {
      eof = true;
      if ((meta.getSize() >= 0) && (totalSize != meta.getSize())) {
        throw new IOException("Unexpected end of stream");
      }
      final String hash = hasher.hash().toString();
      if (!meta.getOid().equals(hash)) {
        throw new IOException("Invalid stream hash");
      }
    }
  }

  @Override
  public int read(@NotNull byte[] buffer, int off, int len) throws IOException {
    if (eof) {
      return -1;
    }
    final int size = stream.read(buffer, off, len);
    if (size > 0) {
      hasher.putBytes(buffer, off, size);
    }
    checkSize(size);
    return size;
  }

  @Override
  public void close() throws IOException {
    stream.close();
  }
}
