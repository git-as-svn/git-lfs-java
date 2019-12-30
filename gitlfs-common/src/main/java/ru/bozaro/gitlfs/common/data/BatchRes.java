package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Batch request.
 *
 * @author Artem V. Navrotskiy
 */
public final class BatchRes {
  @JsonProperty(value = "objects", required = true)
  @Nonnull
  private final List<BatchItem> objects;

  @JsonCreator
  public BatchRes(
      @JsonProperty(value = "objects", required = true) @Nonnull List<BatchItem> objects
  ) {
    this.objects = Collections.unmodifiableList(new ArrayList<>(objects));
  }

  @Nonnull
  public List<BatchItem> getObjects() {
    return objects;
  }
}
