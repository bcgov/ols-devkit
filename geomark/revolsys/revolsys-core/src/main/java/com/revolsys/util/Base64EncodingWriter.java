package com.revolsys.util;

import java.io.UnsupportedEncodingException;

public class Base64EncodingWriter extends java.io.PrintWriter {
  private byte[] buffer;

  private final int bufferLength;

  final String charset = "UTF-8";

  private int position;

  public Base64EncodingWriter(final java.io.Writer out) {
    super(out);
    this.bufferLength = 3;
    this.buffer = new byte[this.bufferLength];
    this.position = 0;
  }

  @Override
  public void close() {
    flush();
    super.close();
    this.buffer = null;
  }

  @Override
  public void flush() {
    if (this.position > 0) {
      writeBuffer();
      this.position = 0;
    }

  }

  public void print(final byte[] bytes) {
    for (final byte b : bytes) {
      write(b);
    }
  }

  public void write(final byte b) {
    this.buffer[this.position++] = b;
    if (this.position >= this.bufferLength) {
      writeBuffer();
      this.position = 0;
    }
  }

  @Override
  public void write(final char[] characters, final int off, final int len) {
    try {
      final byte[] bytes = String.valueOf(characters).getBytes(this.charset);
      for (int i = 0; i < len; i++) {
        write(bytes[off + i]);
      }
    } catch (final UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void write(final int character) {
    try {
      final byte[] bytes = String.valueOf(character).getBytes(this.charset);
      print(bytes);
    } catch (final UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  private void writeBuffer() {
    final int inBuff = this.buffer[0] << 24 >>> 8 | this.buffer[1] << 24 >>> 16
      | this.buffer[2] << 24 >>> 24;
    write(Base64Constants.URL_SAFE_ALPHABET[inBuff >>> 18]);
    write(Base64Constants.URL_SAFE_ALPHABET[inBuff >>> 12 & 0x3f]);
    write(Base64Constants.URL_SAFE_ALPHABET[inBuff >>> 6 & 0x3f]);
    write(Base64Constants.URL_SAFE_ALPHABET[inBuff & 0x3f]);
  }

}
