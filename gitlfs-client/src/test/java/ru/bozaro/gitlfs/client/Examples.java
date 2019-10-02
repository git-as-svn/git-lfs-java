package ru.bozaro.gitlfs.client;

import com.google.common.io.ByteStreams;
import ru.bozaro.gitlfs.client.auth.AuthProvider;
import ru.bozaro.gitlfs.client.io.FileStreamProvider;
import ru.bozaro.gitlfs.common.data.Meta;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class provides examples for documentation.
 */
public final class Examples {

  public void download() throws IOException {
    // tag::download[]
    AuthProvider auth = AuthHelper.create("git@github.com:foo/bar.git");
    Client client = new Client(auth);

    // Single object
    byte[] content = client.getObject("4d7a214614ab2935c943f9e0ff69d22eadbb8f32b1258daaa5e2ca24d17e2393", ByteStreams::toByteArray);

    // Batch mode
    ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    BatchDownloader downloader = new BatchDownloader(client, pool);
    CompletableFuture<byte[]> future = downloader.download(new Meta("4d7a214614ab2935c943f9e0ff69d22eadbb8f32b1258daaa5e2ca24d17e2393", 10), ByteStreams::toByteArray);
    // end::download[]
  }

  public void upload() throws Exception {
    // tag::upload[]
    AuthProvider auth = AuthHelper.create("git@github.com:foo/bar.git");
    Client client = new Client(auth);

    // Single object
    client.putObject(new FileStreamProvider(new File("foo.bin")));

    // Batch mode
    final ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    final BatchUploader uploader = new BatchUploader(client, pool);
    CompletableFuture<Meta> future = uploader.upload(new FileStreamProvider(new File("bar.bin")));
    // end::upload[]
  }
}
