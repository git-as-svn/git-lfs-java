package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Batch request.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class BatchRes {
  @JsonProperty(value = "objects", required = true)
  @NotNull
  private final List<BatchItem> objects;

  @JsonCreator
  public BatchRes(
      @JsonProperty(value = "objects", required = true)
      @NotNull
      List<BatchItem> objects
  ) {
    this.objects = Collections.unmodifiableList(new ArrayList<>(objects));
  }

  @NotNull
  public List<BatchItem> getObjects() {
    return objects;
  }
}
