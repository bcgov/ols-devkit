package com.revolsys.net.oauth;

import java.net.URI;

public class OpenIdResource {
  private final URI uri;

  private final String resource;

  public OpenIdResource(final String uri) {
    this(URI.create(uri));
  }

  public OpenIdResource(final URI uri) {
    this.uri = uri;
    this.resource = uri.toASCIIString();
  }

  public String getResource() {
    return this.resource;
  }

  public URI getUri() {
    return this.uri;
  }

  @Override
  public String toString() {
    return this.resource;
  }
}
