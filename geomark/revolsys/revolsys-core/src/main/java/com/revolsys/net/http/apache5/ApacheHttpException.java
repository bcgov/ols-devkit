package com.revolsys.net.http.apache5;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.jeometry.common.exception.Exceptions;

public class ApacheHttpException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public static ApacheHttpException create(final ClassicHttpRequest request,
    final ClassicHttpResponse response) {
    String content;
    try {
      content = ApacheHttp.getString(response);
    } catch (final Exception e) {
      content = null;
    }
    try {
      return new ApacheHttpException(request.getUri(), response, content);
    } catch (final URISyntaxException e) {
      throw Exceptions.wrap(e);
    }
  }

  private final int statusCode;

  private final String reasonPhrase;

  private final String content;

  private final URI requestUri;

  public ApacheHttpException(final URI requestUri, final ClassicHttpResponse response,
    final String content) {
    super(
      requestUri + "\n" + response.getCode() + ":" + response.getReasonPhrase() + "\n" + content);
    this.requestUri = requestUri;
    this.statusCode = response.getCode();
    this.reasonPhrase = response.getReasonPhrase();
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
