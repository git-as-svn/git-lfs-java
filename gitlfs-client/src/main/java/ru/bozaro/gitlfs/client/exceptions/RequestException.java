package ru.bozaro.gitlfs.client.exceptions;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpUriRequest;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Simple HTTP exception.
 *
 * @author Artem V. Navrotskiy
 */
public class RequestException extends IOException {
  @Nonnull
  private final HttpUriRequest request;
  @Nonnull
  private final HttpResponse response;

  public RequestException(@Nonnull HttpUriRequest request, @Nonnull HttpResponse response) {
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
}
