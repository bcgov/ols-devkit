package com.revolsys.jdbc;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

public class InputStreamBlob implements Blob {
  private final InputStream in;

  private final long length;

  public InputStreamBlob(final InputStream in, final long length) {
    this.in = in;
    this.length = length;
  }

  @Override
  public void free() throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public InputStream getBinaryStream() throws SQLException {
    return this.in;
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
    return this.length;
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
