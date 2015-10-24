package ru.bozaro.gitlfs.client.exceptions;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Simple HTTP exception.
 *
 * @author Artem V. Navrotskiy
 */
public class RequestException extends IOException {
  @NotNull
  private final HttpMethod request;

  public RequestException(@NotNull HttpMethod request) {
    this.request = request;
  }

  public int getStatusCode() {
    return request.getStatusCode();
  }

  @NotNull
  public String getRequestInfo() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Request:\n");
    sb.append("  ").append(request.getName()).append(" ").append(getUrl(request)).append("\n");
    for (Header header : request.getRequestHeaders()) {
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

    sb.append("Response: ").append(request.getStatusCode()).append(" ").append(request.getStatusText()).append("\n");
    for (Header header : request.getResponseHeaders()) {
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
    return sb.toString();
  }

  private static String getUrl(@NotNull HttpMethod request) {
    try {
      return request.getURI().toString();
    } catch (URIException e) {
      return "<unknown url>";
    }
  }
}
