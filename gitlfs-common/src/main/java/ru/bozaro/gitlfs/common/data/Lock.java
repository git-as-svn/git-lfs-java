package ru.bozaro.gitlfs.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

public final class Lock implements Comparable<Lock> {

  @Nonnull
  public static final Comparator<Lock> lockComparator = Comparator
      .comparing(Lock::getLockedAt)
      .thenComparing(Lock::getId);

  /**
   * String ID of the Lock.
   */
  @JsonProperty(value = "id", required = true)
  @Nonnull
  private final String id;

  /**
   * String path name of the locked file.
   */
  @JsonProperty(value = "path", required = true)
  @Nonnull
  private final String path;

  /**
   * The timestamp the lock was created, as an ISO 8601 formatted string.
   */
  @JsonProperty(value = "locked_at", required = true)
  @Nonnull
  private final Date lockedAt;

  /**
   * The name of the user that created the Lock. This should be set from the user credentials posted when creating the lock.
   */
  @JsonProperty(value = "owner")
  @CheckForNull
  private final User owner;

  public Lock(
      @JsonProperty(value = "id", required = true) @Nonnull String id,
      @JsonProperty(value = "path", required = true) @Nonnull String path,
      @JsonProperty(value = "locked_at") @Nonnull Date lockedAt,
      @JsonProperty(value = "owner") @CheckForNull User owner
  ) {
    this.id = id;
    this.path = path;
    this.lockedAt = lockedAt;
    this.owner = owner;
  }

  @Nonnull
  public String getPath() {
    return path;
  }

  @CheckForNull
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
  public int compareTo(@Nonnull Lock o) {
    return lockComparator.compare(this, o);
  }

  @Nonnull
  public Date getLockedAt() {
    return lockedAt;
  }

  @Nonnull
  public String getId() {
    return id;
  }
}
