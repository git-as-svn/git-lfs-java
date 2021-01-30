package ru.bozaro.gitlfs.client;

import com.google.common.base.Utf8;
import com.google.common.io.BaseEncoding;
import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HTTP;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

/**
 * HTTP request-response pair for testing.
 *
 * @author Artem V. Navrotskiy
 */
public class HttpRecord {
  @Nonnull
  private final Request request;
  @Nonnull
  private final Response response;

  public HttpRecord(@Nonnull HttpUriRequest request, @Nonnull HttpResponse response) throws IOException {
    this.request = new Request(request);
    this.response = new Response(response);
  }

  protected HttpRecord() {
    this.request = new Request();
    this.response = new Response();
  }

  @Nonnull
  private static String asString(@Nonnull byte[] data) {
    if (Utf8.isWellFormed(data)) {
      return new String(data, StandardCharsets.UTF_8);
    } else {
      return BaseEncoding.base16().encode(data);
    }
  }

  @Nonnull
  public Request getRequest() {
    return request;
  }

  @Nonnull
  public Response getResponse() {
    return response;
  }

  public static class Response {
    private final int statusCode;
    @Nonnull
    private final String statusText;
    @Nonnull
    private final TreeMap<String, String> headers;
    @CheckForNull
    private final byte[] body;

    Response() {
      this.statusCode = 0;
      this.statusText = "";
      this.headers = new TreeMap<>();
      this.body = null;
    }

    Response(@Nonnull HttpResponse response) throws IOException {
      this.statusCode = response.getStatusLine().getStatusCode();
      this.statusText = response.getStatusLine().getReasonPhrase();
      this.headers = new TreeMap<>();
      for (Header header : response.getAllHeaders()) {
        headers.put(header.getName(), header.getValue());
      }
      try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
        response.getEntity().writeTo(stream);
        this.body = stream.toByteArray();
        response.setEntity(new ByteArrayEntity(this.body));
      }
    }

    @Nonnull
    CloseableHttpResponse toHttpResponse() {
      final CloseableBasicHttpResponse response = new CloseableBasicHttpResponse(new ProtocolVersion("HTTP", 1, 0), statusCode, statusText);

      for (Map.Entry<String, String> header : headers.entrySet())
        response.addHeader(header.getKey(), header.getValue());

      if (body != null)
        response.setEntity(new ByteArrayEntity(body));

      return response;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append("HTTP/1.0 ").append(statusCode).append(" ").append(statusText).append("\n");
      for (Map.Entry<String, String> header : headers.entrySet()) {
        sb.append(header.getKey()).append(": ").append(header.getValue()).append("\n");
      }
      if (body != null) {
        sb.append("\n").append(asString(body));
      }
      return sb.toString();
    }

  }

  private static final class CloseableBasicHttpResponse extends BasicHttpResponse implements CloseableHttpResponse {
    private CloseableBasicHttpResponse(@Nonnull final ProtocolVersion ver,
                                       final int code,
                                       final String reason) {
      super(ver, code, reason);
    }

    @Override
    public void close() {
      // noop
    }
  }

  public static class Request {
    @Nonnull
    private final String href;
    @Nonnull
    private final String method;
    @Nonnull
    private final TreeMap<String, String> headers;
    @CheckForNull
    private final byte[] body;

    Request() {
      href = "";
      method = "";
      headers = new TreeMap<>();
      body = null;
    }

    Request(@Nonnull HttpUriRequest request) throws IOException {
      this.href = request.getURI().toString();
      this.method = request.getMethod();
      this.headers = new TreeMap<>();
      final HttpEntityEnclosingRequest entityRequest = request instanceof HttpEntityEnclosingRequest ? (HttpEntityEnclosingRequest) request : null;
      final HttpEntity entity = entityRequest != null ? entityRequest.getEntity() : null;
      if (entity != null) {
        if (entity.isChunked() || entity.getContentLength() < 0) {
          request.addHeader(HTTP.TRANSFER_ENCODING, HTTP.CHUNK_CODING);
        } else {
          request.addHeader(HTTP.CONTENT_LEN, Long.toString(entity.getContentLength()));
        }

        final Header contentType = entity.getContentType();
        if (contentType != null) {
          headers.put(contentType.getName(), contentType.getValue());
        }
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
          entity.writeTo(buffer);
          body = buffer.toByteArray();
        }
        entityRequest.setEntity(new ByteArrayEntity(body));
      } else {
        body = null;
      }
      for (Header header : request.getAllHeaders()) {
        headers.put(header.getName(), header.getValue());
      }
      headers.remove(HTTP.TARGET_HOST);
      headers.remove(HTTP.USER_AGENT);
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append(method).append(" ").append(href).append("\n");
      for (Map.Entry<String, String> header : headers.entrySet()) {
        sb.append(header.getKey()).append(": ").append(header.getValue()).append("\n");
      }
      if (body != null) {
        sb.append("\n").append(asString(body));
      }
      return sb.toString();
    }
  }
}
