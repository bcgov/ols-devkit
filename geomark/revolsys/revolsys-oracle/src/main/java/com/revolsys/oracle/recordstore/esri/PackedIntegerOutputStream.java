package com.revolsys.oracle.recordstore.esri;

import java.io.ByteArrayOutputStream;

public class PackedIntegerOutputStream extends ByteArrayOutputStream {
  public PackedIntegerOutputStream() {
    super(8000);
  }

  @Override
  public synchronized byte[] toByteArray() {
    final int count = this.count;
    this.count = 0;
    writeLong5(count - 8);
    this.count = count;
    final byte[] data = super.toByteArray();
    return data;
  }

  public void writeLong(final long value) {
    final boolean positive = value >= 0;
    long newValue;
    byte nextByte;
    if (positive) {
      newValue = value;
      nextByte = 0;
    } else {
      newValue = -value;
      nextByte = 0x40;
    }
    nextByte |= newValue & 0x3F;
    newValue = newValue >> 6;
    boolean hasMore = newValue > 0;
    if (hasMore) {
      nextByte |= 0x80;
    }
    write(nextByte);
    while (hasMore) {
      nextByte = (byte)(newValue & 0x7F);
      newValue = newValue >> 7;
      hasMore = newValue > 0;
      if (hasMore) {
        nextByte |= 0x80;
      }
      write(nextByte);
    }
  }

  public void writeLong5(final long value) {
    byte count = 1;
    final boolean positive = value >= 0;
    long newValue;
    int nextByte;
    if (positive) {
      newValue = value;
      nextByte = 0;
    } else {
      newValue = -value;
      nextByte = 0x40;
    }
    nextByte |= newValue & 0x3F;
    newValue = newValue >> 6;
    boolean hasMore = newValue > 0;
    if (hasMore) {
      nextByte |= 0x80;
    }
    write(nextByte);
    while (hasMore) {
      nextByte = (byte)(newValue & 0x7F);
      newValue = newValue >> 7;
      hasMore = newValue > 0;
      if (hasMore) {
        nextByte |= 0x80;
      }
      write(nextByte);
      count++;
    }
    while (count < 5) {
      write(0);
      count++;
    }
  }
}
