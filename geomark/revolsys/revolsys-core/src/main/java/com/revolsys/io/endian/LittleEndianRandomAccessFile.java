package com.revolsys.io.endian;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.jeometry.common.exception.Exceptions;

public class LittleEndianRandomAccessFile implements EndianInputOutput {

  private final RandomAccessFile randomFile;

  public LittleEndianRandomAccessFile(final File file, final String mode) {
    try {
      this.randomFile = new RandomAccessFile(file, mode);
    } catch (final FileNotFoundException e) {
      throw Exceptions.wrap(e);
    }
  }

  public LittleEndianRandomAccessFile(final String name, final String mode) {
    try {
      this.randomFile = new RandomAccessFile(name, mode);
    } catch (final FileNotFoundException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public void close() {
    try {
      this.randomFile.close();
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public void flush() {
  }

  @Override
  public long getFilePointer() {
    try {
      return this.randomFile.getFilePointer();
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public long length() throws IOException {
    return this.randomFile.length();
  }

  @Override
  public int read() throws IOException {
    return this.randomFile.read();
  }

  @Override
  public int read(final byte[] buf) throws IOException {
    return this.randomFile.read(buf);
  }

  @Override
  public double readDouble() throws IOException {
    return this.randomFile.readDouble();
  }

  @Override
  public int readInt() throws IOException {
    return this.randomFile.readInt();
  }

  @Override
  public long readLong() throws IOException {
    return this.randomFile.readLong();
  }

  @Override
  public short readShort() throws IOException {
    return this.randomFile.readShort();
  }

  @Override
  public void seek(final long index) {
    try {
      this.randomFile.seek(index);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public void setLength(final long length) throws IOException {
    try {
      this.randomFile.setLength(length);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }

  }

  @Override
  public int skipBytes(final int i) throws IOException {
    return this.randomFile.skipBytes(i);
  }

  @Override
  public void write(final byte[] b) {
    try {
      this.randomFile.write(b);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public void write(final byte[] b, final int off, final int len) {
    try {
      this.randomFile.write(b, off, len);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public void write(final int b) {
    try {
      this.randomFile.write(b);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public final void writeBytes(final String s) {
    try {
      this.randomFile.writeBytes(s);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public final void writeDouble(final double v) {
    try {
      this.randomFile.writeDouble(v);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public final void writeFloat(final float v) {
    try {
      this.randomFile.writeFloat(v);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public final void writeInt(final int v) {
    try {
      this.randomFile.writeInt(v);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public void writeLEDouble(final double d) {
    final long l = Double.doubleToLongBits(d);
    writeLELong(l);
  }

  @Override
  public void writeLEFloat(final float f) {
    final int i = Float.floatToIntBits(f);
    writeLEInt(i);
  }

  @Override
  public void writeLEInt(final int i) {
    write(i & 0xFF);
    write(i >>> 8 & 0xFF);
    write(i >>> 16 & 0xFF);
    write(i >>> 24 & 0xFF);
  }

  @Override
  public void writeLELong(final long l) {
    write((int)l & 0xFF);
    write((int)(l >>> 8) & 0xFF);
    write((int)(l >>> 16) & 0xFF);
    write((int)(l >>> 24) & 0xFF);
    write((int)(l >>> 32) & 0xFF);
    write((int)(l >>> 40) & 0xFF);
    write((int)(l >>> 48) & 0xFF);
    write((int)(l >>> 56) & 0xFF);
  }

  @Override
  public void writeLEShort(final short s) {
    write(s & 0xFF);
    write(s >>> 8 & 0xFF);
  }

  @Override
  public void writeLEUnsignedShort(final int s) {
    write((byte)(s >>> 0));
    write((byte)(s >>> 8));
  }

  @Override
  public final void writeLong(final long v) {
    try {
      this.randomFile.writeLong(v);
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public void writeShort(final short s) {
    write(s >>> 8 & 0xFF);
    write(s & 0xFF);
  }
}
