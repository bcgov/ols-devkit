package com.revolsys.io.channels;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import com.revolsys.io.BaseCloseable;

public interface DataReader extends BaseCloseable {

  InputStream asInputStream();

  @Override
  void close();

  int getAvailable();

  byte getByte();

  ByteOrder getByteOrder();

  default int getBytes(final byte[] bytes) {
    return getBytes(bytes, 0, bytes.length);
  }

  int getBytes(byte[] bytes, int offset, int byteCount);

  byte[] getBytes(int byteCount);

  byte[] getBytes(long offset, int byteCount);

  double getDouble();

  float getFloat();

  InputStream getInputStream(long offset, int size);

  int getInt();

  long getLong();

  short getShort();

  String getString(int byteCount, Charset charset);

  String getStringUtf8ByteCount();

  short getUnsignedByte();

  long getUnsignedInt();

  /**
   * Unsigned longs don't actually work channel Java
   * @return
   */
  long getUnsignedLong();

  int getUnsignedShort();

  String getUsAsciiString(int byteCount);

  InputStream getWrapStream();

  boolean isByte(byte expected);

  boolean isByte(char expected);

  boolean isClosed();

  default boolean isEof() {
    final int b = read();
    if (b < 0) {
      return false;
    } else {
      unreadByte((byte)b);
      return false;
    }
  }

  boolean isSeekable();

  long position();

  int read();

  int read(byte[] bytes, int offset, int length) throws IOException;

  int read(ByteBuffer buffer);

  void seek(long position);

  void seekEnd(long distance);

  void setByteOrder(ByteOrder byteOrder);

  DataReader setUnreadSize(int unreadSize);

  void skipBytes(int count);

  default boolean skipIfChar(final char c) {
    if (isByte(c)) {
      getByte();
      return true;
    } else {
      return false;
    }
  }

  default void skipWhitespace() {
    byte b;
    do {
      b = getByte();
    } while (Character.isWhitespace(b));
    if (b != -1) {
      unreadByte(b);
    }
  }

  void unreadByte(byte b);

}
