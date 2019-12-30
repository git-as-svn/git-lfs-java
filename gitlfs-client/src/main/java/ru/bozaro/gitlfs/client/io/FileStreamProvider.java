package ru.bozaro.gitlfs.client.io;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Create stream from file.
 *
 * @author Artem V. Navrotskiy
 */
public class FileStreamProvider implements StreamProvider {
  @Nonnull
  private final File file;

  public FileStreamProvider(@Nonnull File file) {
    this.file = file;
  }

  @Nonnull
  @Override
  public InputStream getStream() throws IOException {
    return new FileInputStream(file);
  }
}
