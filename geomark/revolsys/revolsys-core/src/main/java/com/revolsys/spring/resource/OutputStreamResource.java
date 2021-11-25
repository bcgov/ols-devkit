package com.revolsys.spring.resource;

import java.io.InputStream;
import java.io.OutputStream;

public class OutputStreamResource extends AbstractResource {

  private String description;

  private final String filename;

  private final OutputStream outputStream;

  private boolean read;

  public OutputStreamResource(final String filename, final OutputStream outputStream) {
    this.outputStream = outputStream;
    this.filename = filename;
  }

  public OutputStreamResource(final String filename, final OutputStream outputStream,
    final String description) {
    this.filename = filename;
    this.outputStream = outputStream;
    this.description = description;
  }

  @Override
  public Resource createRelative(final String relativePath) {
    return null;
  }

  @Override
  public boolean equals(final Object object) {
    return object == this;
  }

  @Override
  public boolean exists() {
    return true;
  }

  @Override
  public String getDescription() {
    return this.description;
  }

  @Override
  public String getFilename() throws IllegalStateException {
    return this.filename;
  }

  @Override
  public InputStream getInputStream() {
    throw new IllegalArgumentException("No input stream exists");
  }

  @Override
  public int hashCode() {
    return this.outputStream.hashCode();
  }

  @Override
  public boolean isOpen() {
    return true;
  }

  @Override
  public OutputStream newOutputStream() {
    if (this.read) {
      throw new IllegalStateException("OutputStream has already been read - "
        + "do not use OutputStreamResource if a stream needs to be read multiple times");
    }
    this.read = true;
    return this.outputStream;
  }
}
