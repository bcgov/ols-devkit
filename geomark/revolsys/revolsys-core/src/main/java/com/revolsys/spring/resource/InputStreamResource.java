package com.revolsys.spring.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.jeometry.common.logging.Logs;

import com.revolsys.util.Property;

public class InputStreamResource extends AbstractResource {

  private final String description;

  private String filename;

  private final InputStream inputStream;

  private long length = -1;

  private boolean read = false;

  private Charset charset = StandardCharsets.UTF_8;

  /**
   * Construct a new new InputStreamResource.
   * @param inputStream the InputStream to use
   */
  public InputStreamResource(final InputStream inputStream) {
    this(inputStream, "resource loaded through InputStream");
  }

  public InputStreamResource(final InputStream inputStream, final long length) {
    this(inputStream);
    this.length = length;
  }

  /**
   * Construct a new new InputStreamResource.
   * @param inputStream the InputStream to use
   * @param description where the InputStream comes from
   */
  public InputStreamResource(final InputStream inputStream, final String description) {
    if (inputStream == null) {
      throw new IllegalArgumentException("InputStream must not be null");
    }
    this.inputStream = inputStream;
    this.description = description != null ? description : "";
  }

  public InputStreamResource(final String filename, final InputStream inputStream) {
    this(inputStream);
    this.filename = filename;
  }

  public InputStreamResource(final String filename, final InputStream inputStream,
    final long length) {
    this(inputStream);
    this.filename = filename;
    this.length = length;
  }

  public InputStreamResource(final String filename, final InputStream inputStream,
    final String description) {
    this(inputStream, description);
    this.filename = filename;
  }

  @Override
  public long contentLength() throws IOException {
    if (this.length >= 0) {
      return this.length;
    } else {
      return super.contentLength();
    }
  }

  @Override
  public Resource createRelative(final String relativePath) {
    return null;
  }

  /**
   * This implementation compares the underlying InputStream.
   */
  @Override
  public boolean equals(final Object obj) {
    return obj == this || obj instanceof InputStreamResource
      && ((InputStreamResource)obj).inputStream.equals(this.inputStream);
  }

  /**
   * This implementation always returns <code>true</code>.
   */
  @Override
  public boolean exists() {
    return true;
  }

  public Charset getCharset() {
    return this.charset;
  }

  /**
   * This implementation returns the passed-in description, if any.
   */
  @Override
  public String getDescription() {
    return this.description;
  }

  @Override
  public String getFilename() throws IllegalStateException {
    return this.filename;
  }

  /**
   * This implementation throws IllegalStateException if attempting to
   * read the underlying stream multiple times.
   */
  @Override
  public InputStream getInputStream() {
    if (this.read) {
      throw new IllegalStateException("InputStream has already been read - "
        + "do not use InputStreamResource if a stream needs to be read multiple times");
    }
    this.read = true;
    return this.inputStream;
  }

  /**
   * This implementation returns the hash code of the underlying InputStream.
   */
  @Override
  public int hashCode() {
    return this.inputStream.hashCode();
  }

  /**
   * This implementation always returns <code>true</code>.
   */
  @Override
  public boolean isOpen() {
    return true;
  }

  @Override
  public Reader newReader() {
    final InputStream in = getInputStream();
    if (in == null) {
      return null;
    } else {
      return new InputStreamReader(in, this.charset);
    }
  }

  public InputStreamResource setCharset(final Charset charset) {
    if (charset == null) {
      this.charset = StandardCharsets.UTF_8;
    } else {
      this.charset = charset;
    }
    return this;
  }

  public InputStreamResource setCharset(final String charset) {
    if (Property.hasValue(charset)) {
      try {
        this.charset = Charset.forName(charset);
      } catch (final Exception e) {
        Logs.error(this, "Invalid charset: " + charset);
      }
    }
    return this;
  }
}
