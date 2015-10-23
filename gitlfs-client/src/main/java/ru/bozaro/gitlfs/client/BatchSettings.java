package ru.bozaro.gitlfs.client;

import org.jetbrains.annotations.NotNull;

import static ru.bozaro.gitlfs.common.Constants.BATCH_SIZE;

/**
 * Batch settings.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class BatchSettings {
  /**
   * Maximum objects in batch request.
   */
  private int limit = BATCH_SIZE;
  /**
   * Minimum download/upload requests in queue for next batch request.
   */
  private int threshold = 10;
  /**
   * Retry on failure count.
   */
  private int retryCount = 3;

  public BatchSettings() {
  }

  public BatchSettings(int limit, int threshold, int retryCount) {
    this.limit = limit;
    this.threshold = threshold;
    this.retryCount = retryCount;
  }

  public int getLimit() {
    return limit;
  }

  @NotNull
  public BatchSettings setLimit(int limit) {
    this.limit = Math.min(limit, 1);
    return this;
  }

  public int getThreshold() {
    return threshold;
  }

  @NotNull
  public BatchSettings setThreshold(int threshold) {
    this.threshold = Math.max(threshold, 0);
    return this;
  }

  public int getRetryCount() {
    return retryCount;
  }

  @NotNull
  public BatchSettings setRetryCount(int retryCount) {
    this.retryCount = Math.max(retryCount, 1);
    return this;
  }

  @Override
  public String toString() {
    return "BatchSettings{" +
        "limit=" + limit +
        ", threshold=" + threshold +
        ", retryCount=" + retryCount +
        '}';
  }
}
