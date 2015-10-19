package ru.bozaro.gitlfs.client.io;

import org.jetbrains.annotations.NotNull;

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
  @NotNull
  private final File file;

  public FileStreamProvider(@NotNull File file) {
    this.file = file;
  }

  @NotNull
  @Override
  public InputStream getStream() throws IOException {
    return new FileInputStream(file);
  }
}
