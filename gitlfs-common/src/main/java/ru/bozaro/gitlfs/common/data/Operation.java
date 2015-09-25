package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

/**
 * LFSP operation type.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public enum Operation {
  @JsonProperty("upload")
  Upload {
    @Override
    public <R> R visit(@NotNull Visitor<R> visitor) {
      return visitor.visitUpload();
    }
  },
  @JsonProperty("download")
  Download {
    @Override
    public <R> R visit(@NotNull Visitor<R> visitor) {
      return visitor.visitDownload();
    }
  };

  public abstract <R> R visit(@NotNull Visitor<R> visitor);

  public interface Visitor<R> {
    R visitDownload();

    R visitUpload();
  }
}
