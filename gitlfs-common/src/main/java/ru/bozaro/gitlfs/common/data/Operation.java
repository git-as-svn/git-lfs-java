package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Nonnull;

/**
 * LFSP operation type.
 *
 * @author Artem V. Navrotskiy
 */
public enum Operation {
  Download {
    @Override
    public String toValue() {
      return "download";
    }

    @Override
    public <R> R visit(@Nonnull Visitor<R> visitor) {
      return visitor.visitDownload();
    }
  },
  Upload {
    @Override
    public String toValue() {
      return "upload";
    }

    @Override
    public <R> R visit(@Nonnull Visitor<R> visitor) {
      return visitor.visitUpload();
    }
  };

  @JsonCreator
  public static Operation forValue(@Nonnull String value) {
    for (Operation item : values()) {
      if (item.toValue().equals(value)) {
        return item;
      }
    }
    return null;
  }

  @JsonValue
  public abstract String toValue();

  public abstract <R> R visit(@Nonnull Visitor<R> visitor);

  public interface Visitor<R> {
    R visitDownload();

    R visitUpload();
  }
}
