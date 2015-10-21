package ru.bozaro.gitlfs.server;

import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
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

/**
 * Simple upload/download test.
 *
 * @author Artem V. Navrotskiy <bozaro@users.noreply.github.com>
 */
public class BatchTest {
  private static final int REQUEST_COUNT = 1000;
  private static final int TIMEOUT = 30000;

  @Test
  public void batch() throws Exception {
    try (final EmbeddedHttpServer server = new EmbeddedHttpServer()) {
      final MemoryStorage storage = new MemoryStorage(42);
      server.addServlet("/foo/bar.git/info/lfs/objects/*", new PointerServlet(storage, "/foo/bar.git/info/lfs/storage/"));
      server.addServlet("/foo/bar.git/info/lfs/storage/*", new ContentServlet(storage));
      final AuthProvider auth = storage.getAuthProvider(server.getBase().resolve("/foo/bar.git/info/lfs"));

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
        final Meta meta = client.generateMeta(new ByteArrayStreamProvider(content(i)));
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