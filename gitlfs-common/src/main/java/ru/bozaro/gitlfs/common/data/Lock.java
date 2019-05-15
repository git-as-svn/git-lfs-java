package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

public final class Lock implements Comparable<Lock> {

  @NotNull
  public static final Comparator<Lock> lockComparator = Comparator
      .comparing(Lock::getLockedAt)
      .thenComparing(Lock::getId);

  /**
   * String ID of the Lock.
   */
  @JsonProperty(value = "id", required = true)
  @NotNull
  private final String id;

  /**
   * String path name of the locked file.
   */
  @JsonProperty(value = "path", required = true)
  @NotNull
  private final String path;

  /**
   * The timestamp the lock was created, as an ISO 8601 formatted string.
   */
  @JsonProperty(value = "locked_at", required = true)
  @NotNull
  private final Date lockedAt;

  /**
   * The name of the user that created the Lock. This should be set from the user credentials posted when creating the lock.
   */
  @JsonProperty(value = "owner")
  @Nullable
  private final User owner;

  public Lock(
      @JsonProperty(value = "id", required = true) @NotNull String id,
      @JsonProperty(value = "path", required = true) @NotNull String path,
      @JsonProperty(value = "locked_at") @NotNull Date lockedAt,
      @JsonProperty(value = "owner") @Nullable User owner
  ) {
    this.id = id;
    this.path = path;
    this.lockedAt = lockedAt;
    this.owner = owner;
  }

  @NotNull
  public String getPath() {
    return path;
  }

  @Nullable
  public User getOwner() {
    return owner;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Lock))
      return false;

    return id.equals(((Lock) o).id);
  }

  @Override
  public int compareTo(@NotNull Lock o) {
    return lockComparator.compare(this, o);
  }

  @NotNull
  public Date getLockedAt() {
    return lockedAt;
  }

  @NotNull
  public String getId() {
    return id;
  }
}
