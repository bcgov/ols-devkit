package com.revolsys.oracle.recordstore.esri;

import java.io.IOException;
import java.io.InputStream;

public class PackedIntegerInputStream extends InputStream {
  private final InputStream in;

  public PackedIntegerInputStream(final InputStream in) {
    this.in = in;
  }

  @Override
  public int available() throws IOException {
    return this.in.available();
  }

  @Override
  public void close() throws IOException {
    this.in.close();
  }

  @Override
  public synchronized void mark(final int readlimit) {
    this.in.mark(readlimit);
  }

  @Override
  public boolean markSupported() {
    return this.in.markSupported();
  }

  @Override
  public int read() throws IOException {
    return this.in.read();
  }

  @Override
  public int read(final byte[] b) throws IOException {
    return this.in.read(b);
  }

  @Override
  public int read(final byte[] b, final int off, final int len) throws IOException {
    return this.in.read(b, off, len);
  }

  public long readLong() throws IOException {
    int b = this.in.read();
    final boolean positive = (b & 0x40) == 0;
    long value = b & 0x3F;
    byte shift = 6;
    while ((b & 0x80) != 0) {
      b = this.in.read();
      if (b == -1) {
        throw new IllegalStateException("Reached end of file");
      }
      final long byteValue = b & 0x7F;
      final long shiftedValue = byteValue << shift;
      value += shiftedValue;
      shift += 7;
    }
    if (positive) {
      return value;
    } else {
      return -value;
    }
  }

  public long readLong5() throws IOException {
    byte count = 1;
    int b = this.in.read();
    final boolean positive = (b & 0x40) == 0;
    long value = b & 0x3F;
    byte shift = 6;
    while ((b & 0x80) != 0) {
      b = this.in.read();
      final long byteValue = b & 0x7F;
      final long shiftedValue = byteValue << shift;
      value += shiftedValue;
      shift += 7;
      count++;
    }
    while (count < 5) {
      this.in.read();
      count++;
    }
    if (positive) {
      return value;
    } else {
      return -value;
    }
  }

  @Override
  public void reset() throws IOException {
    this.in.reset();
  }

  @Override
  public long skip(final long n) throws IOException {
    return this.in.skip(n);
  }
}
