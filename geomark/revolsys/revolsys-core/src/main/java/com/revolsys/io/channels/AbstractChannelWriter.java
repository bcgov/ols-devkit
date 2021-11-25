package com.revolsys.io.channels;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import com.revolsys.io.BaseCloseable;

public class AbstractChannelWriter implements BaseCloseable {

  protected int available = 0;

  protected ByteBuffer buffer;

  protected final int capacity;

  protected final boolean closeChannel;

  public AbstractChannelWriter(final boolean closeChannel, final ByteBuffer buffer) {
    this.closeChannel = closeChannel;
    if (buffer == null) {
      this.capacity = 8192;
      this.available = this.capacity;
      this.buffer = ByteBuffer.allocateDirect(this.capacity);
    } else {
      buffer.clear();
      this.capacity = buffer.capacity();
      this.available = this.capacity;
      this.buffer = buffer;
    }
  }

  /**
   * Close this writer but not the underlying channel.
   */
  @Override
  public void close() {
    try {
      if (this.buffer != null) {
        write();
      }
      this.buffer = null;
    } finally {
      closeDo();
    }
  }

  protected void closeDo() {
  }

  public void flush() {
    write();
  }

  public ByteOrder getByteOrder() {
    return this.buffer.order();
  }

  public void putByte(final byte b) {
    if (this.available == 0) {
      write();
    }
    this.available--;
    this.buffer.put(b);
  }

  public void putBytes(final byte[] bytes) {
    putBytes(bytes, bytes.length);
  }

  public void putBytes(final byte[] bytes, final int length) {
    if (length <= this.available) {
      this.available -= length;
      this.buffer.put(bytes, 0, length);
    } else {
      this.buffer.put(bytes, 0, this.available);
      int offset = this.available;
      do {
        int bytesToWrite = length - offset;
        write();
        if (bytesToWrite > this.available) {
          bytesToWrite = this.available;
        }
        this.available -= bytesToWrite;
        this.buffer.put(bytes, offset, bytesToWrite);
        offset += bytesToWrite;
      } while (offset < length);
    }
  }

  public void putBytes(final byte[] bytes, final int offset, final int length) {
    if (length <= this.available) {
      this.available -= length;
      this.buffer.put(bytes, offset, length);
    } else {
      this.buffer.put(bytes, offset, this.available);
      int fileOffset = this.available;
      do {
        int bytesToWrite = length - fileOffset;
        write();
        if (bytesToWrite > this.available) {
          bytesToWrite = this.available;
        }
        this.available -= bytesToWrite;
        this.buffer.put(bytes, offset + fileOffset, bytesToWrite);
        fileOffset += bytesToWrite;
      } while (fileOffset < length);
    }
  }

  public void putDouble(final double d) {
    if (this.available < 8) {
      write();
    }
    this.available -= 8;
    this.buffer.putDouble(d);
  }

  public void putFloat(final float f) {
    if (this.available < 4) {
      write();
    }
    this.available -= 4;
    this.buffer.putFloat(f);
  }

  public void putInt(final int i) {
    if (this.available < 4) {
      write();
    }
    this.available -= 4;
    this.buffer.putInt(i);
  }

  public void putLong(final long l) {
    if (this.available < 8) {
      write();
    }
    this.available -= 8;
    this.buffer.putLong(l);
  }

  public void putShort(final short s) {
    if (this.available < 2) {
      write();
    }
    this.available -= 2;
    this.buffer.putShort(s);
  }

  public void putString(final String text, final int maxLength) {
    final byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
    final int length = bytes.length;
    if (length <= maxLength) {
      putBytes(bytes, length);
      for (int i = length; i < maxLength; i++) {
        putByte((byte)0);
      }
    } else {
      putBytes(bytes, maxLength);
    }
  }

  /**
   * Convert the string to UTF-8 bytes and write the number of bytes followed by the bytes.
   */
  public void putStringUtf8ByteCount(final String string) {
    if (string == null) {
      putInt(-1);
    } else {
      final byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
      putInt(bytes.length);
      putBytes(bytes);
    }
  }

  public void putUnsignedByte(final short b) {
    putByte((byte)b);
  }

  public void putUnsignedInt(final long i) {
    putInt((int)i);
  }

  /**
   * Unsigned longs don't actually work in Java
   * @return
   */
  public void putUnsignedLong(final long l) {
    putLong(l);
  }

  public void putUnsignedShort(final int s) {
    putShort((short)s);
  }

  public void setByteOrder(final ByteOrder byteOrder) {
    this.buffer.order(byteOrder);
  }

  protected void write() {

  }

}
