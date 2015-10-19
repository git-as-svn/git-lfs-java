package ru.bozaro.gitlfs.client;

import com.google.common.base.Utf8;
import com.google.common.io.BaseEncoding;
import com.google.common.net.HttpHeaders;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

/**
 * HTTP request-response pair for testing.
 *
 * @author Artem V. Navrotskiy
 */
public class HttpRecord {
  @NotNull
  private final Request request;
  @NotNull
  private final Response response;

  public HttpRecord(@NotNull HttpMethod method) throws IOException {
    this.request = new Request(method);
    this.response = new Response(method);
  }

  protected HttpRecord() {
    this.request = new Request();
    this.response = new Response();
  }

  @NotNull
  public Request getRequest() {
    return request;
  }

  @NotNull
  public Response getResponse() {
    return response;
  }

  @NotNull
  private static String asString(@NotNull byte[] data) {
    if (Utf8.isWellFormed(data)) {
      return new String(data, StandardCharsets.UTF_8);
    } else {
      return BaseEncoding.base16().encode(data);
    }
  }

  public static class Response {
    private final int statusCode;
    @NotNull
    private final String statusText;
    @NotNull
    private final TreeMap<String, String> headers;
    @Nullable
    private final byte[] body;

    protected Response() {
      this.statusCode = 0;
      this.statusText = "";
      this.headers = new TreeMap<>();
      this.body = null;
    }

    public Response(@NotNull HttpMethod method) throws IOException {
      this.statusCode = method.getStatusCode();
      this.statusText = method.getStatusText();
      this.headers = new TreeMap<>();
      for (Header header : method.getResponseHeaders()) {
        headers.put(header.getName(), header.getValue());
      }
      this.body = method.getResponseBody();
    }

    public void apply(@NotNull HttpMethod method) throws IOException {
      setField(method, "statusLine", new StatusLine("HTTP/1.0 " + statusCode + " " + statusText));
      setField(method, "responseBody", body);
      final HeaderGroup headerGroup = new HeaderGroup();
      for (Map.Entry<String, String> header : headers.entrySet()) {
        headerGroup.addHeader(new Header(header.getKey(), header.getValue()));
      }
      setField(method, "responseHeaders", headerGroup);
    }

    private void setField(@NotNull HttpMethod method, @NotNull String name, Object value) {
      try {
        final Field field = HttpMethodBase.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(method, value);
      } catch (ReflectiveOperationException e) {
        throw new AssertionError(e);
      }
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

  public static class Request {
    @NotNull
    private final String href;
    @NotNull
    private final String method;
    @NotNull
    private final TreeMap<String, String> headers;
    @Nullable
    private final byte[] body;

    protected Request() {
      href = "";
      method = "";
      headers = new TreeMap<>();
      body = null;
    }

    public Request(@NotNull HttpMethod method) throws IOException {
      this.href = method.getURI().getURI();
      this.method = method.getName();
      this.headers = new TreeMap<>();
      final RequestEntity entity = method instanceof EntityEnclosingMethod ? ((EntityEnclosingMethod) method).getRequestEntity() : null;
      if (entity != null) {
        if (entity.getContentLength() >= 0) {
          headers.put(HttpHeaders.CONTENT_LENGTH, String.valueOf(entity.getContentLength()));
        }
        headers.put(HttpHeaders.CONTENT_TYPE, entity.getContentType());
        Assert.assertTrue(entity.isRepeatable());
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
          entity.writeRequest(buffer);
          body = buffer.toByteArray();
        }
      } else {
        body = null;
      }
      for (Header header : method.getRequestHeaders()) {
        headers.put(header.getName(), header.getValue());
      }
      headers.remove(HttpHeaders.HOST);
      headers.remove(HttpHeaders.USER_AGENT);
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
