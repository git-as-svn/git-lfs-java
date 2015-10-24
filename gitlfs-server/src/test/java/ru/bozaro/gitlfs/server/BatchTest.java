package ru.bozaro.gitlfs.server;

import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.junit.ArrayAsserts;
import ru.bozaro.gitlfs.client.BatchDownloader;
import ru.bozaro.gitlfs.client.BatchSettings;
import ru.bozaro.gitlfs.client.BatchUploader;
import ru.bozaro.gitlfs.client.Client;
import ru.bozaro.gitlfs.client.auth.AuthProvider;
import ru.bozaro.gitlfs.client.io.ByteArrayStreamProvider;
import ru.bozaro.gitlfs.common.data.Meta;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Simple upload/download test.
 *
 * @author Artem V. Navrotskiy
 */
public class BatchTest {
  private static final int REQUEST_COUNT = 1000;
  private static final int TIMEOUT = 180000;

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
      upload(server.getStorage(), uploader, IntStream
          .range(0, REQUEST_COUNT)
          .filter(i -> i % 2 == 0)
          .mapToObj(BatchTest::content)
          .collect(Collectors.toList()));
      // Upload full data
      upload(server.getStorage(), uploader, IntStream
          .range(0, REQUEST_COUNT)
          .mapToObj(BatchTest::content)
          .collect(Collectors.toList()));
      // Upload none data
      upload(server.getStorage(), uploader, IntStream
          .range(0, REQUEST_COUNT)
          .mapToObj(BatchTest::content)
          .collect(Collectors.toList()));
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
      download(server.getStorage(), downloader, IntStream
          .range(0, REQUEST_COUNT)
          .mapToObj(BatchTest::content)
          .collect(Collectors.toList()));
      // Add data to storage
      populate(server.getStorage(), IntStream
          .range(0, REQUEST_COUNT)
          .filter(i -> i % 2 == 0)
          .mapToObj(BatchTest::content)
          .collect(Collectors.toList()));
      // Download full data
      download(server.getStorage(), downloader, IntStream
          .range(0, REQUEST_COUNT)
          .filter(i -> i % 2 == 0)
          .mapToObj(BatchTest::content)
          .collect(Collectors.toList()));
      // Download half data
      download(server.getStorage(), downloader, IntStream
          .range(0, REQUEST_COUNT)
          .mapToObj(BatchTest::content)
          .collect(Collectors.toList()));
    } finally {
      pool.shutdownNow();
    }
  }

  private void download(@NotNull MemoryStorage storage, @NotNull BatchDownloader downloader, @NotNull List<byte[]> contents) throws InterruptedException, ExecutionException, TimeoutException, IOException {
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
        ArrayAsserts.assertArrayEquals(content, storage.getObject(entry.getKey().getOid()));
      } catch (ExecutionException e) {
        if (e.getCause() instanceof FileNotFoundException) {
          Assert.assertNull(storage.getMetadata(entry.getKey().getOid()));
        } else {
          Assert.fail("Unexpected exception", e.getCause());
        }
      }
    }
  }

  private void upload(@NotNull MemoryStorage storage, @NotNull BatchUploader uploader, @NotNull List<byte[]> contents) throws InterruptedException, ExecutionException, TimeoutException, IOException {
    // Upload data
    @SuppressWarnings("unchecked")
    final CompletableFuture<Meta>[] futures = contents
        .stream()
        .map(content -> uploader.upload(new ByteArrayStreamProvider(content)))
        .toArray(CompletableFuture[]::new);
    // Wait uploading finished
    CompletableFuture.allOf(futures).get(TIMEOUT, TimeUnit.MILLISECONDS);
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
    try (final EmbeddedLfsServer server = new EmbeddedLfsServer(new MemoryStorage(/*42*/ -1))) {
      final AuthProvider auth = server.getAuthProvider();
      upload(auth);
      upload(auth);
      download(auth);
    }
  }

  private void upload(@NotNull AuthProvider auth) throws InterruptedException, ExecutionException, TimeoutException {
    final ExecutorService pool = Executors.newFixedThreadPool(4);
    try {
      final BatchUploader uploader = new BatchUploader(new Client(auth), pool);
      final List<CompletableFuture<Meta>> futures = new ArrayList<>();
      for (int i = 0; i < REQUEST_COUNT; ++i) {
        futures.add(uploader.upload(new ByteArrayStreamProvider(content(i))));
      }
      CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).get(TIMEOUT, TimeUnit.MILLISECONDS);
    } finally {
      pool.shutdownNow();
    }
  }

  private void download(@NotNull AuthProvider auth) throws InterruptedException, ExecutionException, TimeoutException, IOException {
    final ExecutorService pool = Executors.newFixedThreadPool(4);
    try {
      final Client client = new Client(auth);
      final BatchDownloader downloader = new BatchDownloader(client, pool);
      final ConcurrentMap<Integer, byte[]> result = new ConcurrentHashMap<>();
      final List<CompletableFuture<?>> futures = new ArrayList<>();
      for (int i = 0; i < REQUEST_COUNT; ++i) {
        final int id = i;
        final Meta meta = Client.generateMeta(new ByteArrayStreamProvider(content(i)));
        futures.add(downloader.download(meta, inputStream -> {
          Assert.assertNull(result.put(id, ByteStreams.toByteArray(inputStream)));
          return id;
        }));
      }
      CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).get(TIMEOUT, TimeUnit.MILLISECONDS);

      Assert.assertEquals(result.size(), REQUEST_COUNT);
      for (int i = 0; i < REQUEST_COUNT; ++i) {
        Assert.assertEquals(result.get(i), content(i));
      }
    } finally {
      pool.shutdownNow();
    }
  }

  @NotNull
  private static byte[] content(int id) {
    final String result = "Stream " + id;
    return result.getBytes(StandardCharsets.UTF_8);
  }
}