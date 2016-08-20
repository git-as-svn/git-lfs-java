package ru.bozaro.gitlfs.client.exceptions;

import org.apache.http.*;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

/**
 * Simple HTTP exception.
 *
 * @author Artem V. Navrotskiy
 */
public class RequestException extends IOException {
  @NotNull
  private static final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
  @NotNull
  private final HttpUriRequest request;
  @NotNull
  private final HttpResponse response;

  public RequestException(@NotNull HttpUriRequest request, @NotNull HttpResponse response) {
    this.request = request;
    this.response = response;
  }

  public int getStatusCode() {
    return response.getStatusLine().getStatusCode();
  }

  @Override
  public String getMessage() {
    final StatusLine statusLine = response.getStatusLine();
    return request.getURI().toString() + " - " + statusLine.getStatusCode() + " (" + statusLine.getReasonPhrase() + ")";
  }

  @NotNull
  public String getRequestInfo() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Request:\n");
    sb.append("  ").append(request.getMethod()).append(" ").append(request.getURI().toString()).append("\n");
    buildMessageInfo(sb, request);
    if (request instanceof HttpEntityEnclosingRequestBase) {
      builEntityInfo(sb, ((HttpEntityEnclosingRequestBase) request).getEntity());
    }

    final StatusLine statusLine = response.getStatusLine();
    sb.append("Response: ").append(statusLine.getStatusCode()).append(" ").append(statusLine.getReasonPhrase()).append("\n");
    buildMessageInfo(sb, response);
    builEntityInfo(sb, response.getEntity());
    return sb.toString();
  }

  private static void builEntityInfo(@NotNull StringBuilder sb, @Nullable HttpEntity entity) {
    if (entity == null) return;
    try {
      try (InputStream content = entity.getContent()) {
        byte[] buffer = new byte[1024];
        int size = 0;
        while (size < buffer.length) {
          int read = content.read(buffer, size, buffer.length - size);
          if (read <= 0) break;
          size += read;
        }
        int block = 32;
        for (int offset = 0; offset < size; offset += block) {
          buildHexLine(sb, buffer, offset, Math.min(block, size - offset), block);
          sb.append(" ");
          buildSafeLine(sb, buffer, offset, Math.min(block, size - offset));
          sb.append("\n");
        }
      }
    } catch (IOException ignored) {
    }
  }

  private static void buildHexLine(@NotNull StringBuilder sb, byte[] buffer, int offset, int size, int line) {
    for (int i = 0; i < size; ++i) {
      if ((i == 0) && (i % 8 == 0)) sb.append(' ');
      byte b = buffer[offset + i];
      sb.append(HEX[0x0F & (b >> 4)]);
      sb.append(HEX[0x0F & b]);
      sb.append(' ');
    }
    for (int i = size; i < line; ++i) {
      if ((i == 0) && (i % 8 == 0)) sb.append(' ');
      sb.append("   ");
    }
  }

  private static void buildSafeLine(@NotNull StringBuilder sb, byte[] buffer, int offset, int size) {
    for (int i = 0; i < size; ++i) {
      int b = 0xFF & (int) buffer[offset + i];
      if ((b >= 0x20) && (b < 0x80)) {
        sb.append((char) b);
      } else {
        sb.append(' ');
      }
    }
  }

  private static void buildMessageInfo(@NotNull StringBuilder sb, @NotNull HttpMessage message) {
    for (Header header : message.getAllHeaders()) {
      sb.append("  ").append(header.getName()).append(": ");
      if (!header.getName().equals("Authorization")) {
        sb.append(header.getValue());
      } else {
        int space = header.getValue().indexOf(' ');
        if (space > 0) {
          sb.append(header.getValue().substring(0, space + 1));
        }
        sb.append("*****");
      }
      sb.append("\n");
    }
  }
}
