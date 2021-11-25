package com.revolsys.spring.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.FileNames;

public class GzipResource extends AbstractResource {

  private final Resource resource;

  public GzipResource(final Resource resource) {
    super();
    this.resource = resource;
  }

  @Override
  public String getBaseName() {
    String filename = getFilename();
    if (filename.endsWith(".gz")) {
      filename = filename.substring(0, filename.length() - 3);
    }
    return FileNames.getBaseName(filename);
  }

  @Override
  public String getDescription() {
    return this.resource.getDescription();
  }

  @Override
  public File getFile() {
    return this.resource.getFile();
  }

  @Override
  public String getFilename() {
    return this.resource.getFilename();
  }

  @Override
  public InputStream getInputStream() {
    final InputStream in = this.resource.getInputStream();
    try {
      return new GZIPInputStream(in);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public Resource getParent() {
    return this.resource.getParent();
  }

  public Resource getResource() {
    return this.resource;
  }

  @Override
  public URI getURI() throws IOException {
    return this.resource.getURI();
  }

  @Override
  public boolean isFile() {
    return this.resource.isFile();
  }

  @Override
  public OutputStream newOutputStream() {
    final OutputStream out = this.resource.newOutputStream();
    try {
      return new GZIPOutputStream(out);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public String toString() {
    return this.resource.toString();
  }
}
