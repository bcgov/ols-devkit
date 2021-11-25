package com.revolsys.io.endian;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.revolsys.io.EndOfFileException;

public interface EndianInput extends Closeable {

  /**
   * Read a byte.
   *
   * @return The long.
   * @throws IOException If an I/O error occurs.
   */
  int read() throws IOException;

  default int read(final byte buffer[]) throws IOException {
    return read(buffer, 0, buffer.length);
  }

  default int read(final byte buffer[], final int offset, final int length) throws IOException {
    int i = 0;
    for (; i < length; i++) {
      final int value = read();
      if (value == -1) {
        return i;
      } else {
        buffer[offset + i] = (byte)value;
      }
    }
    return i;
  }

  /**
   * See the general contract of the <code>readBoolean</code>
   * method of <code>DataInput</code>.
   * <p>
   * bytes for this operation are read from the contained
   * input stream.
   *
   * @return     the <code>boolean</code> value read.
   * @exception  EndOfFileException  if this input stream has reached the end.
   * @exception  IOException   the stream has been closed and the contained
   *             input stream does not support reading after close, or
   *             another I/O error occurs.
   * @see        java.io.FilterInputStream#in
   */
  default boolean readBoolean() throws IOException {
    final int ch = read();
    if (ch < 0) {
      throw new EndOfFileException();
    }
    return ch != 0;
  }

  /**
   * See the general contract of the <code>readByte</code>
   * method of <code>DataInput</code>.
   * <p>
   * bytes
   * for this operation are read from the contained
   * input stream.
   *
   * @return     the next byte of this input stream as a signed 8-bit
   *             <code>byte</code>.
   * @exception  EndOfFileException  if this input stream has reached the end.
   * @exception  IOException   the stream has been closed and the contained
   *             input stream does not support reading after close, or
   *             another I/O error occurs.
   * @see        java.io.FilterInputStream#in
   */
  default byte readByte() throws IOException {
    final int ch = read();
    if (ch < 0) {
      throw new EndOfFileException();
    }
    return (byte)ch;
  }

  default byte[] readBytes(final int length) throws IOException {
    final byte[] buffer = new byte[length];
    if (read(buffer, 0, length) == length) {
      return buffer;
    } else {
      throw new EndOfFileException();
    }
  }

  /**
   * See the general contract of the <code>readChar</code>
   * method of <code>DataInput</code>.
   * <p>
   * bytes
   * for this operation are read from the contained
   * input stream.
   *
   * @return     the next two bytes of this input stream, interpreted as a
   *             <code>char</code>.
   * @exception  EndOfFileException  if this input stream reaches the end before
   *               reading two bytes.
   * @exception  IOException   the stream has been closed and the contained
   *             input stream does not support reading after close, or
   *             another I/O error occurs.
   * @see        java.io.FilterInputStream#in
   */
  default char readChar() throws IOException {
    final int ch1 = read();
    final int ch2 = read();
    if ((ch1 | ch2) < 0) {
      throw new EndOfFileException();
    }
    return (char)((ch1 << 8) + (ch2 << 0));
  }

  /**
   * Read a big endian double.
   *
   * @return The double.
   * @throws IOException If an I/O error occurs.
   */
  default double readDouble() throws IOException {
    return Double.longBitsToDouble(readLong());
  }

  /**
   * See the general contract of the <code>readFloat</code>
   * method of <code>DataInput</code>.
   * <p>
   * bytes
   * for this operation are read from the contained
   * input stream.
   *
   * @return     the next four bytes of this input stream, interpreted as a
   *             <code>float</code>.
   * @exception  EndOfFileException  if this input stream reaches the end before
   *               reading four bytes.
   * @exception  IOException   the stream has been closed and the contained
   *             input stream does not support reading after close, or
   *             another I/O error occurs.
   * @see        java.io.DataInputStream#readInt()
   * @see        java.lang.Float#intBitsToFloat(int)
   */
  default float readFloat() throws IOException {
    return Float.intBitsToFloat(readInt());
  }

  /**
   * See the general contract of the <code>readFully</code>
   * method of <code>DataInput</code>.
   * <p>
   * bytes
   * for this operation are read from the contained
   * input stream.
   *
   * @param      b   the buffer into which the data is read.
   * @exception  EndOfFileException  if this input stream reaches the end before
   *             reading all the bytes.
   * @exception  IOException   the stream has been closed and the contained
   *             input stream does not support reading after close, or
   *             another I/O error occurs.
   * @see        java.io.FilterInputStream#in
   */
  default void readFully(final byte b[]) throws IOException {
    readFully(b, 0, b.length);
  }

  /**
   * See the general contract of the <code>readFully</code>
   * method of <code>DataInput</code>.
   * <p>
   * bytes
   * for this operation are read from the contained
   * input stream.
   *
   * @param      b     the buffer into which the data is read.
   * @param      off   the start offset of the data.
   * @param      len   the number of bytes to read.
   * @exception  EndOfFileException  if this input stream reaches the end before
   *               reading all the bytes.
   * @exception  IOException   the stream has been closed and the contained
   *             input stream does not support reading after close, or
   *             another I/O error occurs.
   * @see        java.io.FilterInputStream#in
   */
  default void readFully(final byte b[], final int off, final int len) throws IOException {
    if (len < 0) {
      throw new IndexOutOfBoundsException();
    }
    int n = 0;
    while (n < len) {
      final int count = read(b, off + n, len - n);
      if (count < 0) {
        throw new EndOfFileException();
      }
      n += count;
    }
  }

  /**
   * Read a big endian int.
   *
   * @return The int.
   * @throws IOException If an I/O error occurs.
   */
  default int readInt() throws IOException {
    final int ch1 = read();
    final int ch2 = read();
    final int ch3 = read();
    final int ch4 = read();
    if ((ch1 | ch2 | ch3 | ch4) < 0) {
      throw new EndOfFileException();
    }
    return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0);
  }

  /**
   * Read a little endian double.
   *
   * @return The double.
   * @throws IOException If an I/O error occurs.
   */
  default double readLEDouble() throws IOException {
    final long value = readLELong();
    return Double.longBitsToDouble(value);
  }

  default float readLEFloat() throws IOException {
    final int value = readLEInt();
    return Float.intBitsToFloat(value);
  }

  /**
   * Read a little endian int.
   *
   * @return The int.
   * @throws IOException If an I/O error occurs.
   */
  default int readLEInt() throws IOException {
    final int b1 = read();
    final int b2 = read();
    final int b3 = read();
    final int b4 = read();
    if ((b1 | b2 | b3 | b4) < 0) {
      throw new EndOfFileException();
    }
    final int value = (b4 << 24) + (b3 << 16) + (b2 << 8) + b1;

    return value;
  }

  /**
   * Read a little endian long.
   *
   * @return The long.
   * @throws IOException If an I/O error occurs.
   */
  default long readLELong() throws IOException {
    long value = 0;
    for (int shiftBy = 0; shiftBy < 64; shiftBy += 8) {
      value |= (long)(read() & 0xff) << shiftBy;
    }
    return value;
  }

  /**
  * Read a little endian short.
  *
  * @return The short.
  * @throws IOException If an I/O error occurs.
  */
  default short readLEShort() throws IOException {
    final int b1 = read();
    final int b2 = read();
    if ((b1 | b2) < 0) {
      throw new EndOfFileException();
    }
    final int value = (b2 << 8) + b1;
    return (short)value;
  }

  /**
   * Read a little endian int.
   *
   * @return The int.
   * @throws IOException If an I/O error occurs.
   */
  default long readLEUnsignedInt() throws IOException {
    final long b1 = read();
    final long b2 = read();
    final long b3 = read();
    final long b4 = read();
    if ((b1 | b2 | b3 | b4) < 0) {
      throw new EndOfFileException();
    }
    final long value = (b4 << 24) + (b3 << 16) + (b2 << 8) + b1;
    return value;
  }

  /**
   * Read a little endian long.
   *
   * @TODO
   * @return The long.
   * @throws IOException If an I/O error occurs.
   */
  default long readLEUnsignedLong() throws IOException {
    long value = 0;
    for (int shiftBy = 0; shiftBy < 64; shiftBy += 8) {
      value |= (long)(read() & 0xff) << shiftBy;
    }
    return value;
  }

  default int readLEUnsignedShort() throws IOException {
    final int ch1 = read();
    final int ch2 = read();
    if ((ch1 | ch2) < 0) {
      throw new EndOfFileException();
    }
    return (ch1 << 0) + (ch2 << 8);
  }

  /**
  * Read a big endian long.
  *
  * @return The long.
  * @throws IOException If an I/O error occurs.
  */
  default long readLong() throws IOException {
    return ((long)read() << 56) + ((long)(read() & 255) << 48) + ((long)(read() & 255) << 40)
      + ((long)(read() & 255) << 32) + ((long)(read() & 255) << 24) + ((read() & 255) << 16)
      + ((read() & 255) << 8) + ((read() & 255) << 0);
  }

  /**
  * Read a big endian short.
  *
  * @return The short.
  * @throws IOException If an I/O error occurs.
  */
  default short readShort() throws IOException {
    final int ch1 = read();
    final int ch2 = read();
    if ((ch1 | ch2) < 0) {
      throw new EndOfFileException();
    }
    return (short)((ch1 << 8) + (ch2 << 0));
  }

  default String readString(final int byteCount, final Charset charset) throws IOException {
    final byte[] bytes = new byte[byteCount];
    final int readCount = read(bytes);
    int i = 0;
    for (; i < readCount; i++) {
      final byte character = bytes[i];
      if (character == 0) {
        return new String(bytes, 0, i, charset);
      }
    }
    return new String(bytes, 0, i, charset);
  }

  default int readUnsignedByte() throws IOException {
    final int ch = read();
    if (ch < 0) {
      throw new EndOfFileException();
    }
    return ch;
  }

  default int readUnsignedShort() throws IOException {
    final int ch1 = read();
    final int ch2 = read();
    if ((ch1 | ch2) < 0) {
      throw new EndOfFileException();
    }
    return (ch1 << 8) + (ch2 << 0);
  }

  default String readUsAsciiString(final int byteCount) throws IOException {
    return readString(byteCount, StandardCharsets.US_ASCII);
  }

  default int skipBytes(final int byteCount) throws IOException {
    int i = 0;
    for (; i < byteCount; i++) {
      if (read() == -1) {
        return i;
      }
    }
    return i;
  }
}
