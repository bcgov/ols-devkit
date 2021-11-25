package com.revolsys.io.endian;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public interface EndianOutput extends Closeable {
  @Override
  void close();

  void flush();

  long getFilePointer() throws IOException;

  long length() throws IOException;

  void write(byte[] bytes);

  void write(byte[] bytes, int offset, int length);

  void write(int i);

  void writeBytes(String s);

  default void writeChars(final String string) {
    final byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
    write(bytes);
  }

  /**
   * Write a big endian double.
   *
   * @param d The double.
   *
   */
  void writeDouble(double d);

  /**
   * Write a big endian float.
   *
   * @param f The float.
   *
   */
  void writeFloat(float f);

  /**
   * Write a big endian int.
   *
   * @param i The int.
   *
   */
  void writeInt(int i);

  /**
   * Write a little endian double.
   *
   * @param d The double.
   *
   */
  void writeLEDouble(double d);

  /**
   * Write a little endian float.
   *
   * @param f The float.
   *
   */
  void writeLEFloat(float f);

  /**
   * Write a little endian int.
   *
   * @param i The int.
   *
   */
  void writeLEInt(int i);

  /**
   * Write a little endian int.
   *
   * @param l The long.
   *
   */
  void writeLELong(long l);

  /**
   * Write a little endian short.
   *
   * @param s The short.
   *
   */
  void writeLEShort(short s);

  default void writeLEUnsignedInt(final long i) {
    write((byte)i);
    write((byte)(i >>> 8));
    write((byte)(i >>> 16));
    write((byte)(i >>> 24));
  }

  default void writeLEUnsignedLong(final long l) {
    writeLELong(l);
  }

  void writeLEUnsignedShort(int s);

  /**
   * Write a big endian int.
   *
   * @param l The long.
   *
   */
  void writeLong(long l);

  /**
   * Write a big endian short.
   *
   * @param s The short.
   *
   */
  void writeShort(short s);

  default void writeString(String text, final int maxLength) {
    final int length = text.length();
    if (length > maxLength) {
      text = text.substring(0, maxLength);
    }
    writeBytes(text);
    for (int i = length; i < maxLength; i++) {
      write(0);
    }
  }
}
