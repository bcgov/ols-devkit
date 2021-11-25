package com.revolsys.io;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileBackedOutputStreamBuffer extends OutputStream {

  private final ByteArrayOutputStream buffer;

  private OutputStream out;

  private boolean closed;

  private final int bufferSize;

  private int size = 0;

  private Path file;

  public FileBackedOutputStreamBuffer(final int bufferSize) {
    this.bufferSize = bufferSize;
    this.buffer = new ByteArrayOutputStream(bufferSize);
    this.out = this.buffer;
  }

  @Override
  public synchronized void close() throws IOException {
    if (!this.closed) {
      this.closed = true;
      try {
        if (this.out != null) {
          this.out.close();
        }
      } finally {
        if (this.file != null) {
          Files.deleteIfExists(this.file);
        }
      }
      this.out = null;
    }
  }

  private void ensureCapacity(final int count) throws IOException {
    this.size += count;
    if (this.file == null && this.size > this.bufferSize) {
      this.file = Files.createTempFile("file", ".bin");
      this.out = new BufferedOutputStream(Files.newOutputStream(this.file));
    }
  }

  @Override
  public synchronized void flush() throws IOException {
    if (!this.closed) {
      this.out.flush();
    }
  }

  public int getSize() {
    return this.size;
  }

  @Override
  public synchronized void write(final byte[] b, final int off, final int len) throws IOException {

    if (this.closed) {
      throw new IOException("Closed");
    } else {
      ensureCapacity(len);
      this.out.write(b, off, len);
    }
  }

  @Override
  public synchronized void write(final int b) throws IOException {
    if (this.closed) {
      throw new IOException("Closed");
    } else {
      ensureCapacity(1);
      this.out.write(b);
    }
  }

  public synchronized void writeTo(final OutputStream out) throws IOException {
    this.buffer.writeTo(out);
    if (this.file != null) {
      Files.copy(this.file, out);
    }
  }

}
