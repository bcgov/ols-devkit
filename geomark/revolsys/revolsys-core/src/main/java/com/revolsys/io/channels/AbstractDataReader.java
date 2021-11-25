package com.revolsys.io.channels;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.DelegatingInputStream;
import com.revolsys.io.EndOfFileException;
import com.revolsys.util.Debug;

public abstract class AbstractDataReader extends InputStream implements DataReader {

  public static final int DEFAULT_BUFFER_SIZE = 8192;

  private int available = 0;

  protected ByteBuffer buffer;

  private int inAvailable = 0;

  protected ByteBuffer inBuffer;

  protected final int inSize;

  private long readPosition = 0;

  private final boolean seekable;

  private final byte[] tempBytes = new byte[8];

  private boolean closed;

  protected ByteBuffer tempBuffer = ByteBuffer.wrap(this.tempBytes);

  private byte[] unreadBytes;

  private ByteBuffer unreadBuffer;

  private InputStream wrapStream;

  public AbstractDataReader() {
    this(null, false);
  }

  public AbstractDataReader(final ByteBuffer buffer, final boolean seekable) {
    if (buffer == null) {
      this.inBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
    } else {
      this.inBuffer = buffer;
      this.inBuffer.clear();
    }
    this.inSize = this.inBuffer.capacity();
    this.buffer = this.inBuffer;
    this.tempBuffer.order(this.inBuffer.order());
    this.seekable = seekable;
  }

  protected void afterSeek() {
    clearUnreadBuffer();
    this.available = 0;
    this.buffer.clear();
  }

  @Override
  public InputStream asInputStream() {
    return this;
  }

  private void clearUnreadBuffer() {
    if (this.unreadBuffer != null) {
      this.unreadBuffer.position(this.unreadBuffer.capacity());
    }
    this.buffer = this.inBuffer;
  }

  @Override
  public void close() {
    this.closed = true;
    this.buffer = null;
    this.unreadBuffer = null;
    this.inBuffer = null;
    this.tempBuffer = null;
  }

  @Override
  public int getAvailable() {
    return this.available;
  }

  @Override
  public byte getByte() {
    if (this.available == 0) {
      if (!readDo(1)) {
        throw new EndOfFileException();
      }
    }
    this.available--;
    return this.buffer.get();
  }

  @Override
  public ByteOrder getByteOrder() {
    return this.buffer.order();
  }

  @Override
  public int getBytes(final byte[] bytes, final int offset, final int byteCount) {
    if (this.available < byteCount) {
      int readOffset = this.available;
      this.buffer.get(bytes, offset, readOffset);
      this.available = 0;
      do {
        int bytesToRead = byteCount - readOffset;
        final int limit = this.buffer.limit();
        if (bytesToRead > limit) {
          bytesToRead = limit;
        }
        if (!readDo(bytesToRead)) {
          if (readOffset == 0) {
            return -1;
          } else {
            return readOffset;
          }
        }
        if (bytesToRead > this.available) {
          bytesToRead = this.available;
        }
        this.available -= bytesToRead;
        this.buffer.get(bytes, offset + readOffset, bytesToRead);
        readOffset += bytesToRead;
      } while (readOffset < byteCount);
    } else {
      this.available -= byteCount;
      this.buffer.get(bytes);
    }
    return byteCount;
  }

  @Override
  public byte[] getBytes(final int byteCount) {
    final byte[] bytes = new byte[byteCount];
    getBytes(bytes);
    return bytes;
  }

  @Override
  public byte[] getBytes(final long offset, final int byteCount) {
    throw new IllegalArgumentException("getBytes at offset Not supported");
  }

  @Override
  public double getDouble() {
    if (this.available < 8) {
      final ByteBuffer tempBuffer = readTempBytes(8);
      return tempBuffer.getDouble();
    } else {
      this.available -= 8;
      return this.buffer.getDouble();
    }
  }

  @Override
  public float getFloat() {
    if (this.available < 4) {
      final ByteBuffer tempBuffer = readTempBytes(4);
      return tempBuffer.getFloat();
    } else {
      this.available -= 4;
      return this.buffer.getFloat();
    }
  }

  @Override
  public InputStream getInputStream(final long offset, final int size) {
    throw new IllegalArgumentException("Channel not seekable");
  }

  @Override
  public int getInt() {
    if (this.available < 4) {
      final ByteBuffer tempBuffer = readTempBytes(4);
      return tempBuffer.getInt();
    } else {
      this.available -= 4;
      return this.buffer.getInt();
    }
  }

  @Override
  public long getLong() {
    if (this.available < 8) {
      final ByteBuffer tempBuffer = readTempBytes(8);
      return tempBuffer.getLong();
    } else {
      this.available -= 8;
      return this.buffer.getLong();
    }
  }

  @Override
  public short getShort() {
    if (this.available < 2) {
      final ByteBuffer tempBuffer = readTempBytes(2);
      return tempBuffer.getShort();
    } else {
      this.available -= 2;
      return this.buffer.getShort();
    }
  }

  @Override
  public String getString(final int byteCount, final Charset charset) {
    final byte[] bytes = getBytes(byteCount);
    int i = 0;
    for (; i < bytes.length; i++) {
      final byte character = bytes[i];
      if (character == 0) {
        return new String(bytes, 0, i, charset);
      }
    }
    return new String(bytes, 0, i, charset);
  }

  @Override
  public String getStringUtf8ByteCount() {
    final int byteCount = getInt();
    if (byteCount < 0) {
      return null;
    } else if (byteCount == 0) {
      return "";
    } else {
      return getString(byteCount, StandardCharsets.UTF_8);
    }
  }

  @Override
  public short getUnsignedByte() {
    final byte signedByte = getByte();
    return (short)Byte.toUnsignedInt(signedByte);
  }

  @Override
  public long getUnsignedInt() {
    final int signedInt = getInt();
    return Integer.toUnsignedLong(signedInt);
  }

  /**
   * Unsigned longs don't actually work channel Java
   * @return
   */
  @Override
  public long getUnsignedLong() {
    final long signedLong = getLong();
    return signedLong;
  }

  @Override
  public int getUnsignedShort() {
    final short signedShort = getShort();
    return Short.toUnsignedInt(signedShort);
  }

  @Override
  public String getUsAsciiString(final int byteCount) {
    return getString(byteCount, StandardCharsets.US_ASCII);
  }

  @Override
  public InputStream getWrapStream() {
    if (this.wrapStream == null) {
      this.wrapStream = new DelegatingInputStream(this) {
        @Override
        public void close() throws IOException {
        }
      };
    }
    return this.wrapStream;
  }

  @Override
  public boolean isByte(final byte expected) {
    if (this.available == 0) {
      if (!readDo(1)) {
        return false;
      }
    }
    final ByteBuffer buffer = this.buffer;
    buffer.mark();
    final byte b = buffer.get();
    buffer.reset();
    return expected == b;
  }

  @Override
  public boolean isByte(final char expected) {
    if (this.available == 0) {
      if (!readDo(1)) {
        return false;
      }
    }
    final ByteBuffer buffer = this.buffer;
    buffer.mark();
    final char b = (char)buffer.get();
    buffer.reset();
    return expected == b;
  }

  @Override
  public boolean isClosed() {
    return this.closed;
  }

  @Override
  public boolean isSeekable() {
    return this.seekable;
  }

  @Override
  public long position() {
    return this.readPosition - this.available - this.inAvailable;
  }

  @Override
  public int read() {
    if (this.available == 0) {
      if (!readDo(1)) {
        return -1;
      }
    }
    this.available--;
    final byte b = this.buffer.get();
    return b & 0xff;
  }

  @Override
  public int read(final byte[] bytes, final int offset, int length) throws IOException {
    if (this.available == 0) {
      if (!readDo(1)) {
        return -1;
      }
    }
    if (length > this.available) {
      length = this.available;
    }
    this.buffer.get(bytes, offset, length);
    this.available -= length;
    return length;
  }

  @Override
  public int read(final ByteBuffer buffer) {
    if (this.available == 0) {
      if (!readDo(1)) {
        return -1;
      }
    }
    final ByteBuffer readBuffer = this.buffer;
    final int readRemaining = readBuffer.remaining();
    final int writerRemaining = buffer.remaining();
    if (readRemaining <= writerRemaining) {
      buffer.put(readBuffer);
      this.available -= readRemaining;
      return readRemaining;
    } else {
      final int readLimit = readBuffer.limit();
      readBuffer.limit(readBuffer.position() + writerRemaining);
      buffer.put(readBuffer);
      readBuffer.limit(readLimit);
      this.available -= writerRemaining;
      return writerRemaining;
    }
  }

  protected boolean readDo(final int minCount) {
    ByteBuffer buffer = this.buffer;
    if (buffer == this.unreadBuffer) {
      clearUnreadBuffer();
      buffer = this.buffer;
      this.available = this.inAvailable;
      this.inAvailable = 0;
      if (this.available > 0) {
        return true;
      }
    }
    int available = this.available;
    try {
      buffer.clear();
      while (available < minCount) {
        final int readCount = readInternal(buffer);
        if (readCount == -1) {
          return false;
        } else if (readCount == 0) {
          Debug.noOp();
        } else {
          this.readPosition += readCount;
          available += readCount;
        }
      }
      buffer.flip();
      return true;
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    } finally {
      this.available = available;
    }
  }

  protected abstract int readInternal(ByteBuffer buffer) throws IOException;

  protected ByteBuffer readTempBytes(final int count) {
    getBytes(this.tempBytes, 0, count);
    this.tempBuffer.clear();
    this.tempBuffer.limit(count);
    return this.tempBuffer;
  }

  @Override
  public void seek(final long position) {
    final long currentPosition = position();
    if (position >= currentPosition) {
      final long offset = position - currentPosition;

      skipBytes((int)offset);
    } else {
      throw new IllegalArgumentException("Seek no supported");
    }
  }

  @Override
  public void seekEnd(final long distance) {
    throw new IllegalArgumentException("Seek not supported");
  }

  @Override
  public void setByteOrder(final ByteOrder byteOrder) {
    this.buffer.order(byteOrder);
    this.tempBuffer.order(byteOrder);
  }

  @Override
  public DataReader setUnreadSize(final int unreadSize) {
    if (this.unreadBuffer == null) {
      this.unreadBytes = new byte[unreadSize];
      this.unreadBuffer = ByteBuffer.wrap(this.unreadBytes)
        .order(this.inBuffer.order())
        .position(unreadSize);
    } else {
      throw new IllegalArgumentException("Cannot change unread size");
    }
    return this;
  }

  @Override
  public void skipBytes(int count) {
    if (count < this.available) {
      this.available -= count;
      final int newPosition = this.buffer.position() + count;
      this.buffer.position(newPosition);
    } else if (isSeekable()) {
      final long newPosition = position() + count;
      seek(newPosition);
    } else {
      while (count > this.available) {
        count -= this.available;
        this.available = 0;
        readDo(count);
      }
      this.available -= count;
      final int position = this.buffer.position();
      this.buffer.position(position + count);
    }
  }

  @Override
  public boolean skipIfChar(final char c) {
    if (this.available == 0) {
      if (!readDo(1)) {
        return false;
      }
    }
    final ByteBuffer buffer = this.buffer;
    buffer.mark();
    final byte b = buffer.get();
    if (b == c) {
      this.available--;
      return true;
    } else {
      buffer.reset();
      return false;
    }
  }

  @Override
  public void skipWhitespace() {
    do {
      if (this.available == 0) {
        if (!readDo(1)) {
          return;
        }
      }
      final ByteBuffer buffer = this.buffer;
      buffer.mark();
      final byte b = buffer.get();
      switch (b) {
        case ' ':
        case '\n':
        case '\r':
        case '\t':
        case '\f':
        case 0:
          this.available--;
        break;

        default:
          buffer.reset();
          return;
      }
    } while (true);
  }

  @Override
  public void unreadByte(final byte b) {
    ByteBuffer buffer = this.buffer;
    final ByteBuffer unreadBuffer = this.unreadBuffer;
    if (this.available == buffer.limit()) {
      if (buffer == unreadBuffer) {
        throw new IllegalArgumentException("Exceeded unread capacity");
      } else {
        this.inAvailable = this.available;
        buffer = this.buffer = this.unreadBuffer;
        this.available = 0;
      }
    }

    final int newPosition = this.buffer.position() - 1;
    this.available++;
    if (buffer == unreadBuffer) {
      this.unreadBytes[newPosition] = b;
      unreadBuffer.position(newPosition);
    } else {
      this.buffer.position(newPosition);
      this.buffer.put(b);
      this.buffer.position(newPosition);
    }
  }

}
