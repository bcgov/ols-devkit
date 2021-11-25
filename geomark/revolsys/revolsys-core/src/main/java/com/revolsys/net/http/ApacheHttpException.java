package com.revolsys.net.http;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpUriRequest;

public class ApacheHttpException extends RuntimeException {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public static ApacheHttpException create(final HttpUriRequest request,
    final HttpResponse response) {
    final StatusLine statusLine = response.getStatusLine();
    String content;
    try {
      content = ApacheHttp.getString(response);
    } catch (final Exception e) {
      content = null;
    }
    return new ApacheHttpException(request.getURI(), statusLine, content);
  }

  private final int statusCode;

  private final String reasonPhrase;

  private final String content;

  private final URI requestUri;

  public ApacheHttpException(final URI requestUri, final StatusLine statusLine,
    final String content) {
    super(requestUri + "\n" + statusLine + "\n" + content);
    this.requestUri = requestUri;
    this.statusCode = statusLine.getStatusCode();
    this.reasonPhrase = statusLine.getReasonPhrase();
    this.content = content;
  }

  public String getContent() {
    return this.content;
  }

  public String getReasonPhrase() {
    return this.reasonPhrase;
  }

  public URI getRequestUri() {
    return this.requestUri;
  }

  public int getStatusCode() {
    return this.statusCode;
  }

}
