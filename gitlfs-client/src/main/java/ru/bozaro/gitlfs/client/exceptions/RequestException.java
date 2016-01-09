package ru.bozaro.gitlfs.client.exceptions;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpUriRequest;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Simple HTTP exception.
 *
 * @author Artem V. Navrotskiy
 */
public class RequestException extends IOException {
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
    for (Header header : request.getAllHeaders()) {
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

    final StatusLine statusLine = response.getStatusLine();
    sb.append("Response: ").append(statusLine.getStatusCode()).append(" ").append(statusLine.getReasonPhrase()).append("\n");
    for (Header header : response.getAllHeaders()) {
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
}
