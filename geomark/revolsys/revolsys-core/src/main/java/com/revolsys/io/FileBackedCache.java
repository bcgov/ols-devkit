package com.revolsys.io;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class FileBackedCache implements BaseCloseable {

  private byte[] buffer;

  private final int maxSize;

  private final OutputStream outputStream = new OutputStream() {
    @Override
    public void close() {
    }

    @Override
    public void flush() {
      FileBackedCache.this.flush();
    }

    @Override
    public synchronized void write(final byte[] data, final int offset, final int length) {
      FileBackedCache.this.write(data, offset, length);
    }

    @Override
    public synchronized void write(final int b) {
      FileBackedCache.this.write(b);
    }
  };

  private int size;

  private Writer writer;

  public FileBackedCache() {
    this(1024 * 1024);
  }

  public FileBackedCache(final int maxSize) {
    this.maxSize = maxSize;
    if (maxSize > 4096) {
      this.buffer = new byte[4096];
    } else {
      this.buffer = new byte[maxSize];
    }
  }

  @Override
  public void close() {
  }

  public void flush() {
  }

  public InputStream getInputStream() {
    return new ByteArrayInputStream(this.buffer, 0, this.size);
  }

  public OutputStream getOutputStream() {
    return this.outputStream;
  }

  public int getSize() {
    return this.size;
  }

  public synchronized Writer getWriter() {
    if (this.writer == null) {
      this.writer = FileUtil.newUtf8Writer(this.outputStream);
    }
    return this.writer;
  }

  @Override
  public String toString() {
    return new String(this.buffer, 0, this.size, StandardCharsets.UTF_8);
  }

  public synchronized void write(final byte[] data, final int offset, final int length) {
    final int newSize = this.size + length;
    if (newSize >= this.buffer.length) {
      if (newSize >= this.maxSize) {
        throw new RuntimeException("Buffer too large");
      } else {
        int newLength = this.buffer.length + 4096;
        if (newLength < newSize) {
          newLength = newSize;
        }
        if (newLength > this.maxSize) {
          newLength = this.maxSize;
        }
        final byte[] buffer = new byte[newLength];
        System.arraycopy(this.buffer, 0, buffer, 0, this.buffer.length);
        this.buffer = buffer;
      }
    }
    System.arraycopy(data, offset, this.buffer, this.size, length);
    this.size = newSize;
  }

  public synchronized void write(final int b) {
    if (this.size > this.buffer.length) {
      if (this.size > this.maxSize) {
        throw new RuntimeException("Buffer too large");
      } else {
        final byte[] buffer = new byte[Math.max(this.maxSize, this.buffer.length + 4096)];
        System.arraycopy(this.buffer, 0, buffer, 0, buffer.length);
        this.buffer = buffer;
      }
    }
    this.buffer[this.size++] = (byte)b;
  }
}
