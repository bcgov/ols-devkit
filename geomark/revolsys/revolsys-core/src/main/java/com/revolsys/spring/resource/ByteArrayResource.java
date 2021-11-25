package com.revolsys.spring.resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ByteArrayResource extends AbstractResource {

  private final byte[] data;

  private final String description;

  private String filename;

  private Charset charset = StandardCharsets.UTF_8;

  /**
   * Construct a new new ByteArrayResource.
   * @param data the byte array to wrap
   */
  public ByteArrayResource(final byte[] data) {
    this(data, "resource loaded from byte array");
  }

  /**
   * Construct a new new ByteArrayResource.
   * @param data the byte array to wrap
   * @param description where the byte array comes from
   */
  public ByteArrayResource(final byte[] data, final String description) {
    if (data == null) {
      throw new IllegalArgumentException("Byte array must not be null");
    }
    this.data = data;
    this.description = description != null ? description : "";
  }

  public ByteArrayResource(final String filename, final byte[] data) {
    this(data);
    this.filename = filename;
  }

  public ByteArrayResource(final String filename, final byte[] data, final String description) {
    this(data, description);
    this.filename = filename;
  }

  public ByteArrayResource(final String filename, final String data) {
    this(data.getBytes());
    this.filename = filename;
  }

  /**
   * This implementation returns the length of the underlying byte array.
   */
  @Override
  public long contentLength() {
    return this.data.length;
  }

  @Override
  public Resource createRelative(final String relativePath) {
    return null;
  }

  /**
   * This implementation compares the underlying byte array.
   * @see java.util.Arrays#equals(byte[], byte[])
   */
  @Override
  public boolean equals(final Object obj) {
    return obj == this || obj instanceof ByteArrayResource
      && Arrays.equals(((ByteArrayResource)obj).data, this.data);
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
   * Return the underlying byte array.
   */
  public final byte[] getData() {
    return this.data;
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
   * This implementation returns a ByteArrayInputStream for the
   * underlying byte array.
   * @see java.io.ByteArrayInputStream
   */
  @Override
  public InputStream getInputStream() {
    return new ByteArrayInputStream(this.data);
  }

  /**
   * This implementation returns the hash code based on the
   * underlying byte array.
   */
  @Override
  public int hashCode() {
    return byte[].class.hashCode() * 29 * this.data.length;
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

  public ByteArrayResource setCharset(final Charset charset) {
    if (charset == null) {
      this.charset = StandardCharsets.UTF_8;
    } else {
      this.charset = charset;
    }
    return this;
  }

}
