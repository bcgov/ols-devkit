package com.revolsys.io.page;

import java.nio.MappedByteBuffer;

public class FileMappedPage implements Page {
  private final MappedByteBuffer buffer;

  private final int index;

  private final PageManager pageManager;

  public FileMappedPage(final PageManager pageManager, final int index,
    final MappedByteBuffer buffer) {
    this.pageManager = pageManager;
    this.index = index;
    this.buffer = buffer;
    buffer.position(0);
  }

  @Override
  public void clear() {
    clearBytes(0);
  }

  @Override
  public void clearBytes(final int startIndex) {
    this.buffer.mark();
    try {
      this.buffer.position(startIndex);
      for (int i = startIndex; i < getSize(); i++) {
        writeByte((byte)0);
      }
    } finally {
      this.buffer.reset();
    }
  }

  @Override
  public int compareTo(final Page page) {
    final int index = getIndex();
    final int index2 = page.getIndex();
    if (index == index2) {
      return 0;
    } else if (index < index2) {
      return -1;
    } else {
      return 1;
    }
  }

  @Override
  public void flush() {
  }

  @Override
  public byte[] getContent() {
    final byte[] data = new byte[getSize()];
    this.buffer.mark();
    try {
      this.buffer.position(0);
      this.buffer.get(data);
    } finally {
      this.buffer.reset();
    }
    return data;
  }

  @Override
  public int getIndex() {
    return this.index;
  }

  @Override
  public int getOffset() {
    return this.buffer.position();
  }

  @Override
  public PageManager getPageManager() {
    return this.pageManager;
  }

  @Override
  public int getSize() {
    return this.buffer.capacity();
  }

  @Override
  public byte readByte() {
    if (getOffset() + 1 > getSize()) {
      throw new ArrayIndexOutOfBoundsException("Unable to read past end of record");
    } else {
      return this.buffer.get();
    }
  }

  @Override
  public byte[] readBytes(final byte[] bytes, final int offset, final int count) {
    this.buffer.get(bytes, offset, count);
    return bytes;
  }

  @Override
  public byte[] readBytes(final int size) {
    final byte[] bytes = new byte[size];
    this.buffer.get(bytes, 0, size);
    return bytes;
  }

  @Override
  public double readDouble() {
    if (getOffset() + 4 > getSize()) {
      throw new ArrayIndexOutOfBoundsException("Unable to read past end of record");
    } else {
      return this.buffer.getDouble();
    }
  }

  @Override
  public float readFloat() {
    if (getOffset() + 4 > getSize()) {
      throw new ArrayIndexOutOfBoundsException("Unable to read past end of record");
    } else {
      return this.buffer.getFloat();
    }
  }

  @Override
  public int readInt() {
    if (getOffset() + 4 > getSize()) {
      throw new ArrayIndexOutOfBoundsException("Unable to read past end of record");
    } else {
      return this.buffer.getInt();
    }
  }

  @Override
  public long readLong() {
    if (getOffset() + 8 > getSize()) {
      throw new ArrayIndexOutOfBoundsException("Unable to read past end of record");
    } else {
      return this.buffer.getLong();

    }
  }

  @Override
  public short readShort() {
    if (getOffset() + 2 > getSize()) {
      throw new ArrayIndexOutOfBoundsException("Unable to read past end of record");
    } else {
      return this.buffer.getShort();
    }
  }

  @Override
  public void setContent(final Page page) {
    this.buffer.position(0);
    this.buffer.put(page.getContent());
  }

  @Override
  public void setOffset(final int offset) {
    this.buffer.position(offset);
  }

  @Override
  public void writeByte(final byte b) {
    if (getOffset() + 1 > getSize()) {
      throw new ArrayIndexOutOfBoundsException("Unable to read past end of record");
    } else {
      this.buffer.put(b);
    }
  }

  @Override
  public void writeBytes(final byte[] bytes) {
    if (getOffset() + bytes.length > getSize()) {
      throw new ArrayIndexOutOfBoundsException("Unable to read past end of record");
    } else {
      this.buffer.put(bytes);
    }
  }

  @Override
  public void writeBytes(final byte[] bytes, final int offset, final int count) {
    if (getOffset() + count > getSize()) {
      throw new ArrayIndexOutOfBoundsException("Unable to read past end of record");
    } else {
      this.buffer.put(bytes, offset, count);
    }
  }

  @Override
  public void writeDouble(final double d) {
    if (getOffset() + 8 > getSize()) {
      throw new ArrayIndexOutOfBoundsException("Unable to read past end of record");
    } else {
      this.buffer.putDouble(d);
    }
  }

  @Override
  public void writeFloat(final float f) {
    if (getOffset() + 4 > getSize()) {
      throw new ArrayIndexOutOfBoundsException("Unable to read past end of record");
    } else {
      this.buffer.putFloat(f);
    }
  }

  @Override
  public void writeInt(final int i) {
    if (getOffset() + 4 > getSize()) {
      throw new ArrayIndexOutOfBoundsException("Unable to read past end of record");
    } else {
      this.buffer.putInt(i);
    }
  }

  @Override
  public void writeLong(final long l) {
    if (getOffset() + 8 > getSize()) {
      throw new ArrayIndexOutOfBoundsException("Unable to read past end of record");
    } else {
      this.buffer.putLong(l);
    }
  }

  @Override
  public void writeShort(final short s) {
    if (getOffset() + 2 > getSize()) {
      throw new ArrayIndexOutOfBoundsException("Unable to read past end of record");
    } else {
      this.buffer.putShort(s);
    }
  }

}
