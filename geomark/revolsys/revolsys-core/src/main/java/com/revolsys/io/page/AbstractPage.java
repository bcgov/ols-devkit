package com.revolsys.io.page;

public abstract class AbstractPage implements Page {

  private final int index;

  private final PageManager pageManager;

  public AbstractPage(final PageManager pageManager, final int index) {
    this.index = index;
    this.pageManager = pageManager;
  }

  @Override
  public void clear() {
    clearBytes(0);
  }

  @Override
  public void clearBytes(final int startIndex) {
    setOffset(startIndex);
    for (int i = startIndex; i < getSize(); i++) {
      writeByte(0);
    }
    setOffset(startIndex);
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
    this.pageManager.write(this);
  }

  @Override
  public int getIndex() {
    return this.index;
  }

  @Override
  public PageManager getPageManager() {
    return this.pageManager;
  }

  @Override
  public int hashCode() {
    return getIndex();
  }

  @Override
  public byte readByte() {
    if (getOffset() + 1 > getSize()) {
      throw new ArrayIndexOutOfBoundsException("Unable to read past end of record");
    }
    final int b1 = readNextByte();
    return (byte)b1;
  }

  @Override
  public byte[] readBytes(final byte[] bytes, final int offset, final int count) {
    System.arraycopy(getContent(), getOffset(), bytes, offset, count);
    setOffset(getOffset() + count);
    return bytes;
  }

  @Override
  public byte[] readBytes(final int size) {
    final byte[] bytes = new byte[size];
    final byte[] content = getContent();
    System.arraycopy(content, getOffset(), bytes, 0, size);
    setOffset(getOffset() + size);
    return bytes;
  }

  @Override
  public double readDouble() {
    final long l = readLong();
    return Double.longBitsToDouble(l);
  }

  @Override
  public float readFloat() {
    final int i = readInt();
    return Float.intBitsToFloat(i);
  }

  @Override
  public int readInt() {
    if (getOffset() + 4 > getSize()) {
      throw new ArrayIndexOutOfBoundsException("Unable to read past end of record");
    } else {
      final int b1 = readNextByte();
      final int b2 = readNextByte();
      final int b3 = readNextByte();
      final int b4 = readNextByte();
      final int i = (b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0);
      return i;
    }
  }

  @Override
  public long readLong() {
    if (getOffset() + 8 > getSize()) {
      throw new ArrayIndexOutOfBoundsException("Unable to read past end of record");
    } else {
      final int b1 = readNextByte();
      final int b2 = readNextByte();
      final int b3 = readNextByte();
      final int b4 = readNextByte();
      final int b5 = readNextByte();
      final int b6 = readNextByte();
      final int b7 = readNextByte();
      final int b8 = readNextByte();
      return ((long)b1 << 56) + ((long)(b2 & 255) << 48) + ((long)(b3 & 255) << 40)
        + ((long)(b4 & 255) << 32) + ((long)(b5 & 255) << 24) + ((b6 & 255) << 16)
        + ((b7 & 255) << 8) + ((b8 & 255) << 0);

    }
  }

  protected abstract int readNextByte();

  @Override
  public short readShort() {
    if (getOffset() + 2 > getSize()) {
      throw new ArrayIndexOutOfBoundsException("Unable to read past end of record");
    } else {
      final int b1 = readNextByte();
      final int b2 = readNextByte();
      return (short)((b1 << 8) + (b2 << 0));
    }
  }

  @Override
  public void writeByte(final byte b) {
    if (getOffset() > getSize()) {
      throw new ArrayIndexOutOfBoundsException("Unable to write past end of record");
    } else {
      writeByte((int)b);
    }
  }

  protected abstract void writeByte(final int b);

  @Override
  public void writeBytes(final byte[] bytes) {
    for (final byte b : bytes) {
      writeByte(b);
    }
  }

  @Override
  public void writeBytes(final byte[] bytes, final int offset, final int count) {
    for (int i = 0; i < count; i++) {
      final int b = bytes[offset + i];
      writeByte(b);
    }
  }

  @Override
  public final void writeDouble(final double d) {
    writeLong(Double.doubleToLongBits(d));
  }

  @Override
  public final void writeFloat(final float f) {
    writeInt(Float.floatToIntBits(f));
  }

  @Override
  public void writeInt(final int i) {
    if (getOffset() + 4 > getSize()) {
      throw new ArrayIndexOutOfBoundsException("Unable to write past end of record");
    } else {
      writeByte(i >>> 24 & 0xFF);
      writeByte(i >>> 16 & 0xFF);
      writeByte(i >>> 8 & 0xFF);
      writeByte(i >>> 0 & 0xFF);
    }
  }

  @Override
  public final void writeLong(final long l) {
    if (getOffset() + 4 > getSize()) {
      throw new ArrayIndexOutOfBoundsException("Unable to write past end of record");
    } else {
      writeByte((byte)(l >>> 56));
      writeByte((byte)(l >>> 48));
      writeByte((byte)(l >>> 40));
      writeByte((byte)(l >>> 32));
      writeByte((byte)(l >>> 24));
      writeByte((byte)(l >>> 16));
      writeByte((byte)(l >>> 8));
      writeByte((byte)(l >>> 0));
    }
  }

  @Override
  public final void writeShort(final short s) {
    if (getOffset() + 2 > getSize()) {
      throw new ArrayIndexOutOfBoundsException("Unable to write past end of record");
    } else {
      writeByte(s >>> 8 & 0xFF);
      writeByte(s >>> 0 & 0xFF);
    }
  }

}
