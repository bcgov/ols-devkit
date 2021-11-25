package com.revolsys.io;

public class StringWriter extends java.io.Writer implements CharSequence {
  private final StringBuilder buffer;

  public StringWriter() {
    this.buffer = new StringBuilder();
    this.lock = this.buffer;
  }

  public StringWriter(final int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("Buffer size must be > 0 ");
    }
    this.buffer = new StringBuilder(capacity);
    this.lock = this.buffer;
  }

  @Override
  public char charAt(final int index) {
    return this.buffer.charAt(index);
  }

  @Override
  public void close() {
  }

  @Override
  public void flush() {
  }

  public StringBuilder getBuffer() {
    return this.buffer;
  }

  @Override
  public int length() {
    return this.buffer.length();
  }

  @Override
  public CharSequence subSequence(final int start, final int end) {
    return this.buffer.subSequence(start, end);
  }

  @Override
  public String toString() {
    return this.buffer.toString();
  }

  @Override
  public void write(final char data[], final int offset, final int length) {
    if (length == 0) {
      return;
    } else {
      if (offset < 0 || length < 0) {
        throw new IndexOutOfBoundsException();
      } else {
        this.buffer.append(data, offset, length);
      }
    }
  }

  @Override
  public void write(final int c) {
    this.buffer.append((char)c);
  }

  @Override
  public void write(final String string) {
    this.buffer.append(string);
  }

  @Override
  public void write(final String string, final int offset, final int length) {
    this.buffer.append(string, offset, offset + length);
  }

}
