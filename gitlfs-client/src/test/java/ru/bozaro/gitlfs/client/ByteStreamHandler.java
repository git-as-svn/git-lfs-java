package ru.bozaro.gitlfs.client;

import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.client.io.StreamHandler;

import java.io.IOException;
import java.io.InputStream;

/**
 * Handle stream as byte array.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class ByteStreamHandler implements StreamHandler<byte[]> {
  @NotNull
  @Override
  public byte[] accept(@NotNull InputStream inputStream) throws IOException {
    return ByteStreams.toByteArray(inputStream);
  }
}
