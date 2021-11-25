package org.jeometry.common.jdbc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

public class ByteArrayBlob implements Blob {

  private byte[] data;

  public ByteArrayBlob(final byte[] string) {
    this.data = string;
  }

  public boolean equals(final ByteArrayBlob blob) {
    if (this.data == null) {
      return blob.data == null;
    } else if (blob.data == null) {
      return false;
    } else {
      return this.data.equals(blob.data);
    }
  }

  @Override
  public void free() throws SQLException {
    this.data = null;
  }

  @Override
  public InputStream getBinaryStream() throws SQLException {
    return new ByteArrayInputStream(this.data);
  }

  @Override
  public InputStream getBinaryStream(final long pos, final long length) throws SQLException {
    throw new UnsupportedOperationException();
  }

  public byte[] getBytes() {
    return this.data;
  }

  @Override
  public byte[] getBytes(final long pos, int length) throws SQLException {
    final long maxLength = length();
    if (pos + length > maxLength) {
      length = (int)(maxLength - pos);
    }
    final byte[] data = new byte[length];
    System.arraycopy(this.data, (int)pos, data, 0, length);
    return data;
  }

  @Override
  public long length() throws SQLException {
    return this.data.length;
  }

  @Override
  public long position(final Blob pattern, final long start) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public long position(final byte[] pattern, final long start) throws SQLException {
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
  public void truncate(final long pos) throws SQLException {
    throw new UnsupportedOperationException("Cannot modify Blob");
  }
}
