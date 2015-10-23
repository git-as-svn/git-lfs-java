package ru.bozaro.gitlfs.server;

import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.bozaro.gitlfs.client.BatchDownloader;
import ru.bozaro.gitlfs.client.BatchUploader;
import ru.bozaro.gitlfs.client.Client;
import ru.bozaro.gitlfs.client.auth.AuthProvider;
import ru.bozaro.gitlfs.client.io.ByteArrayStreamProvider;
import ru.bozaro.gitlfs.common.data.Meta;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
  private static final int TIMEOUT = 30000;

  @DataProvider(name = "uploadProvider")
  public static Object[][] uploadProvider() {
    return new Object[][]{
        new Object[]{-1, 100, 10},
        new Object[]{42, 100, 10},
        new Object[]{7, 5, 3},
    };
  }

  @Test(dataProvider = "uploadProvider")
  public void uploadTest(int tokenMaxUsage, int batchLimit, int batchTreshold) throws Exception {
    final ExecutorService pool = Executors.newFixedThreadPool(4);
    try (final EmbeddedLfsServer server = new EmbeddedLfsServer(new MemoryStorage(tokenMaxUsage))) {
      final AuthProvider auth = server.getAuthProvider();
      final BatchUploader uploader = new BatchUploader(new Client(auth), pool, batchLimit, batchTreshold);
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