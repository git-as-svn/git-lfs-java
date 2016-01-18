# Overview

[![Build Status](https://travis-ci.org/bozaro/git-lfs-java.svg?branch=master)](https://travis-ci.org/bozaro/git-lfs-java)
[![Maven Central](https://img.shields.io/maven-central/v/ru.bozaro.gitlfs/gitlfs-common.svg)](http://mvnrepository.com/artifact/ru.bozaro.gitlfs)

## What is this?

This is Git LFS Java API implementation.

This project contains:

 * gitlfs-common - Common structures for serialization/deserialization Git LFS messages
 * gitlfs-pointer - Git LFS pointer serialization/deserialization
 * gitlfs-client - API for uploading/downloading Git LFS objects from server
 * gitlfs-server - Servlets for creating custom LFS server

## How to use?

You can download latest stable version from [Maven Central](http://mvnrepository.com/artifact/ru.bozaro.gitlfs).

### Uploading object to Git LFS server

```java
  final AuthProvider auth = AuthHelper.create("git@github.com:foo/bar.git");
  final Client client = new Client(auth);

  // Single object
  client.putObject(new FileStreamProvider(new File("foo.bin")));

  // Batch mode
  final ExecutorService pool = Executors.newFixedThreadPool(4);
  final BatchUploader uploader = new BatchUploader(client, pool);
  CompletableFuture<Meta> future = uploader.upload(new FileStreamProvider(new File("bar.bin")));
```

### Downloading object from Git LFS server

```java
  final AuthProvider auth = AuthHelper.create("git@github.com:foo/bar.git");
  final Client client = new Client(auth);

  // Single object
  final byte[] content = client.getObject("4d7a214614ab2935c943f9e0ff69d22eadbb8f32b1258daaa5e2ca24d17e2393", ByteStreams::toByteArray);

  // Batch mode
  final ExecutorService pool = Executors.newFixedThreadPool(4);
  final BatchDownloader downloader = new BatchDownloader(client, pool);
  CompletableFuture<byte[]> future = uploader.download(new Meta("4d7a214614ab2935c943f9e0ff69d22eadbb8f32b1258daaa5e2ca24d17e2393", 10), ByteStreams::toByteArray);
```

### Embedded LFS server

See https://github.com/bozaro/git-lfs-java/blob/master/gitlfs-server/src/test/java/ru/bozaro/gitlfs/server/ServerTest.java for example.

## Changes

Version 0.9.0

 * Update all dependencies

Version 0.8.0

 * Replace commons-httpclient:commons-httpclient:3.1 by org.apache.httpcomponents:httpclient:4.1.3

Version 0.7.0

 * Add header modification for replacing Basic authentication by Token
 * Don't ask password for SSH authentication
 * Add more informative HTTP error message

Version 0.6.0

 * Require JDK 8
 * High level batch API implementation
 * Add steam hash validation on download

Version 0.5.0

 * Server implemetation stabilization
 * Create multitheaded HttpClient by default
 * Fix single object downloading API

Version 0.4.0

 * Initial server implementation
 * Fix url concatenation
 * Fix downloading for not uploaded object (404 error)
 * Fix minor bugs

Version 0.3.0

 * Add authenticator for git-lfs-authenticate command (experimental)
 * Add AuthHelper class for simple AuthProvider creation
 * Fix verify url bug
 * Fix basic authentication
 * Fix already uploaded behaviour

Version 0.2.0

 * Support [Git LFS v1 Batch API](https://github.com/github/git-lfs/blob/master/docs/api/http-v1-batch.md)

Version 0.1.0

 * Initital version;
 * Support [Git LFS v1 Original API](https://github.com/github/git-lfs/blob/master/docs/api/http-v1-original.md)
 * Support [Git LFS pointer serialize/parse](https://github.com/github/git-lfs/blob/master/docs/spec.md)
