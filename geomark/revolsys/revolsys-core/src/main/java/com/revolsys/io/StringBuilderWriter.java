package com.revolsys.io;

import java.io.IOException;

public class StringBuilderWriter extends java.io.Writer {

  private final StringBuilder buffer;

  public StringBuilderWriter(final StringBuilder buffer) {
    this.buffer = buffer;
  }

  @Override
  public void close() throws IOException {
  }

  /**
   * Flush the stream.
   */
  @Override
  public void flush() {
  }

  public StringBuilder getBuffer() {
    return this.buffer;
  }

  @Override
  public String toString() {
    return this.buffer.toString();
  }

  @Override
  public void write(final char[] cbuf, final int off, final int len) {
    if (off < 0 || off > cbuf.length || len < 0 || off + len > cbuf.length || off + len < 0) {
      throw new IndexOutOfBoundsException();
    } else if (len == 0) {
      return;
    }
    this.buffer.append(cbuf, off, len);
  }

  @Override
  public void write(final int c) {
    this.buffer.append((char)c);
  }

  @Override
  public void write(final String str) {
    this.buffer.append(str);
  }

  @Override
  public void write(final String str, final int off, final int len) {
    this.buffer.append(str.substring(off, off + len));
  }

}
