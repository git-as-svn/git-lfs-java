package ru.bozaro.gitlfs.client.internal;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.protocol.HTTP;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public final class LfsRequest {
  @Nonnull
  private final HttpUriRequest request;
  @Nullable
  private final AbstractHttpEntity entity;

  LfsRequest(@Nonnull HttpUriRequest request, @Nullable AbstractHttpEntity entity) {
    this.request = request;
    this.entity = entity;
  }

  @Nonnull
  public HttpUriRequest addHeaders(@Nonnull Map<String, String> headers) {
    for (Map.Entry<String, String> en : headers.entrySet()) {
      if (HTTP.TRANSFER_ENCODING.equals(en.getKey())) {
        /*
          See https://github.com/bozaro/git-as-svn/issues/365
          LFS-server can ask us to respond with chunked body via setting "Transfer-Encoding: chunked" HTTP header in LFS link
          Unfortunately, we cannot pass it as-is to response HTTP headers, see RequestContent#process.
          If it sees that Transfer-Encoding header was set, it throws exception immediately.
          So instead, we suppress addition of Transfer-Encoding header and set entity to be chunked here.
          RequestContent#process will see that HttpEntity#isChunked returns true and will set correct Transfer-Encoding header.
        */
        if (entity != null) {
          final boolean chunked = HTTP.CHUNK_CODING.equals(en.getValue());
          entity.setChunked(chunked);
        }
      } else {
        request.addHeader(en.getKey(), en.getValue());
      }
    }
    return request;
  }
}
