package org.jeometry.common.jdbc;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;

public class StringClob implements Clob {

  private String string;

  public StringClob(final String string) {
    this.string = string;
  }

  public boolean equals(final StringClob clob) {
    if (this.string == null) {
      return clob.string == null;
    } else if (clob.string == null) {
      return false;
    } else {
      return this.string.equals(clob.string);
    }
  }

  @Override
  public void free() throws SQLException {
    this.string = null;
  }

  @Override
  public InputStream getAsciiStream() throws SQLException {
    throw new UnsupportedOperationException("Cannot modify Clob");
  }

  @Override
  public Reader getCharacterStream() throws SQLException {
    return new StringReader(this.string);
  }

  @Override
  public Reader getCharacterStream(final long offset, final long length) throws SQLException {
    return new StringReader(this.string.substring((int)offset - 1, (int)length));
  }

  @Override
  public String getSubString(final long pos, final int len) throws SQLException {
    throw new UnsupportedOperationException("Cannot modify Clob");
  }

  @Override
  public long length() throws SQLException {
    return this.string.length();
  }

  @Override
  public long position(final Clob colb, final long pos) throws SQLException {
    throw new UnsupportedOperationException("Cannot modify Clob");
  }

  @Override
  public long position(final String string, final long pos) throws SQLException {
    throw new UnsupportedOperationException("Cannot modify Clob");
  }

  @Override
  public OutputStream setAsciiStream(final long pos) throws SQLException {
    throw new UnsupportedOperationException("Cannot modify Clob");
  }

  @Override
  public Writer setCharacterStream(final long pos) throws SQLException {
    throw new UnsupportedOperationException("Cannot modify Clob");
  }

  @Override
  public int setString(final long pos, final String string) throws SQLException {
    throw new UnsupportedOperationException("Cannot modify Clob");
  }

  @Override
  public int setString(final long pos, final String string, final int i, final int j)
    throws SQLException {
    throw new UnsupportedOperationException("Cannot modify Clob");
  }

  @Override
  public String toString() {
    return this.string;
  }

  @Override
  public void truncate(final long pos) throws SQLException {
    throw new UnsupportedOperationException("Cannot modify Clob");
  }
}
