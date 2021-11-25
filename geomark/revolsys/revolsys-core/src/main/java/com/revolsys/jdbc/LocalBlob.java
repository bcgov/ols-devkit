package com.revolsys.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

import com.revolsys.spring.resource.ByteArrayResource;
import com.revolsys.spring.resource.Resource;

public class LocalBlob implements Blob {

  private final Resource resource;

  public LocalBlob(final byte[] content) {
    this.resource = new ByteArrayResource(content);
  }

  public LocalBlob(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public void free() throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public InputStream getBinaryStream() throws SQLException {
    if (this.resource == null) {
      return null;
    } else {
      return this.resource.newBufferedInputStream();
    }
  }

  @Override
  public InputStream getBinaryStream(final long pos, final long length) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public byte[] getBytes(final long pos, final int length) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public long length() throws SQLException {
    try {
      return this.resource.contentLength();
    } catch (final IOException e) {
      throw new RuntimeException("Unable to get length for resource: " + this.resource, e);
    }
  }

  @Override
  public long position(final Blob pattern, final long start) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public long position(final byte pattern[], final long start) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public OutputStream setBinaryStream(final long pos) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int setBytes(final long pos, final byte[] bytes) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int setBytes(final long pos, final byte[] bytes, final int offset, final int len)
    throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void truncate(final long len) throws SQLException {
    throw new UnsupportedOperationException();
  }

}
