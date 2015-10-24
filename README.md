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
  client.putObject(new FileStreamProvider(new File("foo.bin")));
```

### Downloading object from Git LFS server

```java
  final AuthProvider auth = AuthHelper.create("git@github.com:foo/bar.git");
  final Client client = new Client(auth);
  final byte[] content = client.getObject("4d7a214614ab2935c943f9e0ff69d22eadbb8f32b1258daaa5e2ca24d17e2393", ByteStreams::toByteArray);
```

## Changes

Version 0.6.0

 * Require JDK 8
 * High level batch API implementation

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
