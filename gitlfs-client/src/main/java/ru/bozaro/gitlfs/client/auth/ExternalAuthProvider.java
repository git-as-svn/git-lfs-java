package ru.bozaro.gitlfs.client.auth;

import org.jetbrains.annotations.NotNull;
import ru.bozaro.gitlfs.common.JsonHelper;
import ru.bozaro.gitlfs.common.data.Link;
import ru.bozaro.gitlfs.common.data.Operation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Arrays;

/**
 * Get authentication data from external application.
 * This AuthProvider is EXPERIMENTAL and it can only be used at your own risk.
 *
 * @author Artem V. Navrotskiy
 */
public class ExternalAuthProvider extends CachedAuthProvider {
  @NotNull
  private final String authority;
  @NotNull
  private final String path;

  /**
   * Create authentication wrapper for git-lfs-authenticate command.
   *
   * @param gitUrl Git URL like: git@github.com:bozaro/git-lfs-java.git
   */
  public ExternalAuthProvider(@NotNull String gitUrl) throws MalformedURLException {
    final int separator = gitUrl.indexOf(':');
    if (separator < 0) {
      throw new MalformedURLException("Can't find separator ':' in gitUrl: " + gitUrl);
    }
    this.authority = gitUrl.substring(0, separator);
    this.path = gitUrl.substring(separator + 1);
  }

  /**
   * Create authentication wrapper for git-lfs-authenticate command.
   *
   * @param authority SSH server authority with user name
   * @param path      Repostiry path
   */
  public ExternalAuthProvider(@NotNull String authority, @NotNull String path) {
    this.authority = authority;
    this.path = path;
  }

  @NotNull
  protected String[] getCommand(@NotNull Operation operation) {
    return new String[]{
        "ssh",
        getAuthority(),
        "-oBatchMode=yes",
        "-C",
        "git-lfs-authenticate",
        getPath(),
        operation.toValue()
    };
  }

  @NotNull
  protected Link getAuthUncached(@NotNull Operation operation) throws IOException, InterruptedException {
    final ProcessBuilder builder = new ProcessBuilder()
        .command(getCommand(operation))
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .redirectOutput(ProcessBuilder.Redirect.PIPE);
    final Process process = builder.start();
    process.getOutputStream().close();
    final InputStream stdoutStream = process.getInputStream();
    final ByteArrayOutputStream stdoutData = new ByteArrayOutputStream();
    final byte[] buffer = new byte[0x10000];
    while (true) {
      final int read = stdoutStream.read(buffer);
      if (read <= 0) break;
      stdoutData.write(buffer, 0, read);
    }
    final int exitValue = process.waitFor();
    if (exitValue != 0) {
      throw new IOException("Command returned with non-zero exit code " + exitValue + ": " + Arrays.toString(builder.command().toArray()));
    }
    return JsonHelper.mapper.readValue(stdoutData.toByteArray(), Link.class);
  }

  @NotNull
  public String getAuthority() {
    return authority;
  }

  @NotNull
  public String getPath() {
    return path;
  }
}
