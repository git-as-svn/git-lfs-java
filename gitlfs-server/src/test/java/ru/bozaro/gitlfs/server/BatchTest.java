package ru.bozaro.gitlfs.server;

import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.bozaro.gitlfs.client.BatchDownloader;
import ru.bozaro.gitlfs.client.BatchSettings;
import ru.bozaro.gitlfs.client.BatchUploader;
import ru.bozaro.gitlfs.client.Client;
import ru.bozaro.gitlfs.client.auth.AuthProvider;
import ru.bozaro.gitlfs.client.auth.ExternalAuthProvider;
import ru.bozaro.gitlfs.client.io.ByteArrayStreamProvider;
import ru.bozaro.gitlfs.common.data.Meta;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Simple upload/download test.
 *
 * @author Artem V. Navrotskiy
 */
public class BatchTest {
  private static final int REQUEST_COUNT = 1000;
  private static final int TIMEOUT = 60000;

  @DataProvider(name = "batchProvider")
  public static Object[][] batchProvider() {
    return new Object[][]{
        new Object[]{-1, new BatchSettings(100, 10, 3)},
        new Object[]{42, new BatchSettings(100, 10, 3)},
        new Object[]{7, new BatchSettings(5, 3, 3)},
    };
  }

  @Test(dataProvider = "batchProvider")
  public void uploadTest(int tokenMaxUsage, @NotNull BatchSettings settings) throws Exception {
    final ExecutorService pool = Executors.newFixedThreadPool(4);
    try (final EmbeddedLfsServer server = new EmbeddedLfsServer(new MemoryStorage(tokenMaxUsage))) {
      final AuthProvider auth = server.getAuthProvider();
      final BatchUploader uploader = new BatchUploader(new Client(auth), pool, settings);
      // Upload half data
      upload(
          uploader,
          IntStream
              .range(0, REQUEST_COUNT)
              .filter(i -> i % 2 == 0)
              .mapToObj(BatchTest::content)
              .collect(Collectors.toList()),
          server.getStorage()
      );
      // Upload full data
      upload(
          uploader,
          IntStream
              .range(0, REQUEST_COUNT)
              .mapToObj(BatchTest::content)
              .collect(Collectors.toList()),
          server.getStorage()
      );
      // Upload none data
      upload(
          uploader,
          IntStream
              .range(0, REQUEST_COUNT)
              .mapToObj(BatchTest::content)
              .collect(Collectors.toList()),
          server.getStorage()
      );
      Assert.assertTrue(pool.shutdownNow().isEmpty());
    } finally {
      pool.shutdownNow();
    }
  }

  @Test(dataProvider = "batchProvider")
  public void downloadTest(int tokenMaxUsage, @NotNull BatchSettings settings) throws Exception {
    final ExecutorService pool = Executors.newFixedThreadPool(4);
    try (final EmbeddedLfsServer server = new EmbeddedLfsServer(new MemoryStorage(tokenMaxUsage))) {
      final AuthProvider auth = server.getAuthProvider();
      final BatchDownloader downloader = new BatchDownloader(new Client(auth), pool, settings);
      download(
          downloader,
          IntStream
              .range(0, REQUEST_COUNT)
              .mapToObj(BatchTest::content)
              .collect(Collectors.toList()),
          server.getStorage()
      );
      // Add data to storage
      populate(server.getStorage(), IntStream
          .range(0, REQUEST_COUNT)
          .filter(i -> i % 2 == 0)
          .mapToObj(BatchTest::content)
          .collect(Collectors.toList()));
      // Download full data
      download(
          downloader,
          IntStream
              .range(0, REQUEST_COUNT)
              .filter(i -> i % 2 == 0)
              .mapToObj(BatchTest::content)
              .collect(Collectors.toList()),
          server.getStorage()
      );
      // Download half data
      download(
          downloader,
          IntStream
              .range(0, REQUEST_COUNT)
              .mapToObj(BatchTest::content)
              .collect(Collectors.toList()),
          server.getStorage()
      );
      Assert.assertTrue(pool.shutdownNow().isEmpty());
    } finally {
      pool.shutdownNow();
    }
  }

  private void download(@NotNull BatchDownloader downloader, @NotNull List<byte[]> contents, @NotNull MemoryStorage storage) throws Exception {
    download(downloader, contents, meta -> storage.getObject(meta.getOid()) != null);
  }

  private void download(@NotNull BatchDownloader downloader, @NotNull List<byte[]> contents, @NotNull List<byte[]> expected) throws Exception {
    final Set<String> oids = new HashSet<>();
    for (byte[] content : expected) {
      oids.add(Client.generateMeta(new ByteArrayStreamProvider(content)).getOid());
    }
    download(downloader, contents, meta -> oids.contains(meta.getOid()));
  }

  private void download(@NotNull BatchDownloader downloader, @NotNull List<byte[]> contents, @NotNull Function<Meta, Boolean> checker) throws Exception {
    // Download data
    final Map<Meta, CompletableFuture<byte[]>> map = new HashMap<>();
    for (byte[] content : contents) {
      final Meta meta = Client.generateMeta(new ByteArrayStreamProvider(content));
      map.put(meta, downloader.download(meta, ByteStreams::toByteArray));
    }
    // Check result
    for (Map.Entry<Meta, CompletableFuture<byte[]>> entry : map.entrySet()) {
      try {
        final byte[] content = entry.getValue().get(TIMEOUT, TimeUnit.MILLISECONDS);
        Assert.assertNotNull(content);
        Assert.assertTrue(checker.apply(entry.getKey()));
      } catch (ExecutionException e) {
        if (e.getCause() instanceof FileNotFoundException) {
          Assert.assertFalse(checker.apply(entry.getKey()));
        } else {
          Assert.fail("Unexpected exception", e.getCause());
        }
      }
    }
  }

  private void upload(@NotNull BatchUploader uploader, @NotNull List<byte[]> contents) throws Exception {
    // Upload data
    @SuppressWarnings("unchecked")
    final CompletableFuture<Meta>[] futures = contents
        .stream()
        .map(content -> uploader.upload(new ByteArrayStreamProvider(content)))
        .toArray(CompletableFuture[]::new);
    // Wait uploading finished
    CompletableFuture.allOf(futures).get(TIMEOUT, TimeUnit.MILLISECONDS);
    // Check future status
    for (CompletableFuture<Meta> future : futures) {
      future.get();
    }
  }

  private void upload(@NotNull BatchUploader uploader, @NotNull List<byte[]> contents, @NotNull MemoryStorage storage) throws Exception {
    // Upload data
    upload(uploader, contents);
    // Check result
    for (byte[] content : contents) {
      final Meta meta = Client.generateMeta(new ByteArrayStreamProvider(content));
      Assert.assertNotNull(storage.getMetadata(meta.getOid()), new String(content, StandardCharsets.UTF_8));
    }
  }

  private void populate(@NotNull MemoryStorage storage, @NotNull List<byte[]> contents) throws InterruptedException, ExecutionException, TimeoutException, IOException {
    for (byte[] content : contents) {
      storage.saveObject(new ByteArrayStreamProvider(content));
    }
  }

  @Test
  public void simple() throws Exception {
    try (final EmbeddedLfsServer server = new EmbeddedLfsServer(new MemoryStorage(-1))) {
      fullCircle(server.getAuthProvider());
    }
  }

  @Test(enabled = false)
  public void github() throws Exception {
    fullCircle(new ExternalAuthProvider("git@github.com:bozaro/test.git"));
  }

  private void fullCircle(@NotNull AuthProvider auth) throws Exception {
    final ExecutorService pool = Executors.newFixedThreadPool(4);
    try {
      BatchSettings settings = new BatchSettings()
          .setLimit(10);
      final BatchDownloader downloader = new BatchDownloader(new Client(auth), pool, settings);
      final BatchUploader uploader = new BatchUploader(new Client(auth), pool, settings);
      final String prefix = UUID.randomUUID().toString();
      // Initial upload
      int requestCount = 50;
      upload(
          uploader,
          IntStream
              .range(0, requestCount)
              .filter(i -> i % 2 == 0)
              .mapToObj((id) -> BatchTest.content(prefix, id))
              .collect(Collectors.toList())
      );
      // Download
      download(
          downloader,
          IntStream
              .range(0, requestCount)
              .mapToObj((id) -> BatchTest.content(prefix, id))
              .collect(Collectors.toList()),
          IntStream
              .range(0, requestCount)
              .filter(i -> i % 2 == 0)
              .mapToObj((id) -> BatchTest.content(prefix, id))
              .collect(Collectors.toList())
      );
      // Already upload
      upload(
          uploader,
          IntStream
              .range(0, requestCount)
              .mapToObj((id) -> BatchTest.content(prefix, id))
              .collect(Collectors.toList())
      );
      Assert.assertTrue(pool.shutdownNow().isEmpty());
    } finally {
      pool.shutdownNow();
    }
  }

  @NotNull
  private static byte[] content(int id) {
    return content("TEST", id);
  }

  @NotNull
  private static byte[] content(@NotNull String prefix, int id) {
    final String result = prefix + " " + id;
    return result.getBytes(StandardCharsets.UTF_8);
  }
}