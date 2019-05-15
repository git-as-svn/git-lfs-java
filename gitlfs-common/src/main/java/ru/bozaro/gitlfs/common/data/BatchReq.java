package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

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
  @NotNull
  private final Operation operation;

  @JsonProperty(value = "objects", required = true)
  @NotNull
  private final List<Meta> objects;

  public BatchReq(
      @JsonProperty(value = "operation", required = true) @NotNull Operation operation,
      @JsonProperty(value = "objects", required = true) @NotNull List<Meta> objects
  ) {
    this.operation = operation;
    this.objects = Collections.unmodifiableList(new ArrayList<>(objects));
  }

  @NotNull
  public Operation getOperation() {
    return operation;
  }

  @NotNull
  public List<Meta> getObjects() {
    return objects;
  }
}
