package ru.bozaro.gitlfs.common.data;

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
public final class BatchReq {
  @JsonProperty(value = "operation", required = true)
  @Nonnull
  private final Operation operation;

  @JsonProperty(value = "objects", required = true)
  @Nonnull
  private final List<Meta> objects;

  public BatchReq(
      @JsonProperty(value = "operation", required = true) @Nonnull Operation operation,
      @JsonProperty(value = "objects", required = true) @Nonnull List<Meta> objects
  ) {
    this.operation = operation;
    this.objects = Collections.unmodifiableList(new ArrayList<>(objects));
  }

  @Nonnull
  public Operation getOperation() {
    return operation;
  }

  @Nonnull
  public List<Meta> getObjects() {
    return objects;
  }
}
