package com.revolsys.spring.resource;

public class NoSuchResourceException extends IllegalArgumentException {

  private static final long serialVersionUID = 1L;

  private final Resource resource;

  public NoSuchResourceException(final Resource resource) {
    super(resource.toString());
    this.resource = resource;
  }

  public Resource getResource() {
    return this.resource;
  }

}
