package ru.bozaro.gitlfs.pointer;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import static ru.bozaro.gitlfs.pointer.Constants.*;

/**
 * Class for read/writer pointer blobs.
 * https://github.com/github/git-lfs/blob/master/docs/spec.md
 *
 * @author Artem V. Navrotskiy
 */
public class Pointer {
  @Nonnull
  private static final byte[] PREFIX = (VERSION + ' ').getBytes(StandardCharsets.UTF_8);
  @Nonnull
  private static final RequiredKey[] REQUIRED = new RequiredKey[]{
      new RequiredKey(OID, Pattern.compile("^[0-9a-z]+:[0-9a-f]+$")),
      new RequiredKey(SIZE, Pattern.compile("^\\d+$")),
  };

  /**
   * Serialize pointer map.
   *
   * @param pointer Pointer data.
   * @return Pointer content bytes.
   */
  @Nonnull
  public static byte[] serializePointer(@Nonnull Map<String, String> pointer) {
    final Map<String, String> data = new TreeMap<>(pointer);
    final StringBuilder buffer = new StringBuilder();
    // Write version.
    {
      String version = data.remove(VERSION);
      if (version == null) {
        version = VERSION_URL;
      }
      buffer.append(VERSION).append(' ').append(version).append('\n');
    }
    for (Map.Entry<String, String> entry : data.entrySet()) {
      buffer.append(entry.getKey()).append(' ').append(entry.getValue()).append('\n');
    }
    return buffer.toString().getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Create pointer with oid and size.
   *
   * @param oid  Object oid.
   * @param size Object size.
   * @return Return pointer data.
   */
  @Nonnull
  public static Map<String, String> createPointer(@Nonnull String oid, long size) {
    final Map<String, String> pointer = new TreeMap<>();
    pointer.put(VERSION, VERSION_URL);
    pointer.put(OID, oid);
    pointer.put(SIZE, Long.toString(size));
    return pointer;
  }

  /**
   * Read pointer data.
   *
   * @param stream Input stream.
   * @return Return pointer info or null if blob is not a pointer data.
   */
  @CheckForNull
  public static Map<String, String> parsePointer(@Nonnull InputStream stream) throws IOException {
    byte[] buffer = new byte[Constants.POINTER_MAX_SIZE];
    int size = 0;
    while (size < buffer.length) {
      int len = stream.read(buffer, size, buffer.length - size);
      if (len <= 0) {
        return parsePointer(buffer, 0, size);
      }
      size += len;
    }
    return null;
  }

  /**
   * Read pointer data.
   *
   * @param blob Blob data.
   * @return Return pointer info or null if blob is not a pointer data.
   */
  @CheckForNull
  public static Map<String, String> parsePointer(@Nonnull byte[] blob, final int offset, final int length) {
    // Check prefix
    if (length < PREFIX.length) return null;
    for (int i = 0; i < PREFIX.length; ++i) {
      if (blob[i] != PREFIX[i]) return null;
    }
    // Reading key value pairs
    final TreeMap<String, String> result = new TreeMap<>();
    final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
    String lastKey = null;
    int keyOffset = offset;
    int required = 0;
    while (keyOffset < length) {
      int valueOffset = keyOffset;
      // Key
      while (true) {
        valueOffset++;
        if (valueOffset < length) {
          final byte c = blob[valueOffset];
          if (c == ' ') break;
          // Keys MUST only use the characters [a-z] [0-9] . -.
          if (c >= 'a' && c <= 'z') continue;
          if (c >= '0' && c <= '9') continue;
          if (c == '.' || c == '-') continue;
        }
        // Found invalid character.
        return null;
      }
      int endOffset = valueOffset;
      // Value
      do {
        endOffset++;
        if (endOffset >= length) return null;
        // Values MUST NOT contain return or newline characters.
      } while (blob[endOffset] != '\n');
      final String key = new String(blob, keyOffset, valueOffset - keyOffset, StandardCharsets.UTF_8);

      final String value;
      try {
        value = decoder.decode(ByteBuffer.wrap(blob, valueOffset + 1, endOffset - valueOffset - 1)).toString();
      } catch (CharacterCodingException e) {
        return null;
      }
      if (required < REQUIRED.length && REQUIRED[required].name.equals(key)) {
        if (!REQUIRED[required].pattern.matcher(value).matches()) {
          return null;
        }
        required++;
      }
      if (keyOffset > offset) {
        if (lastKey != null && key.compareTo(lastKey) <= 0) {
          return null;
        }
        lastKey = key;
      }
      if (result.put(key, value) != null) {
        return null;
      }
      keyOffset = endOffset + 1;
    }
    // Not found all required fields.
    if (required < REQUIRED.length) {
      return null;
    }
    return result;
  }

  /**
   * Read pointer data.
   *
   * @param blob Blob data.
   * @return Return pointer info or null if blob is not a pointer data.
   */
  @CheckForNull
  public static Map<String, String> parsePointer(@Nonnull byte[] blob) {
    return parsePointer(blob, 0, blob.length);
  }

  private static final class RequiredKey {
    @Nonnull
    private final String name;
    @Nonnull
    private final Pattern pattern;

    public RequiredKey(@Nonnull String name, @Nonnull Pattern pattern) {
      this.name = name;
      this.pattern = pattern;
    }
  }
}
