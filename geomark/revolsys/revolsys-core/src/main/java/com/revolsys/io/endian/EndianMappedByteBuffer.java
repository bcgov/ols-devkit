/*
 * $URL$
 * $Author$
 * $Date$
 * $Revision$

 * Copyright 2004-2005 Revolution Systems Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.io.endian;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import org.jeometry.common.exception.Exceptions;

public class EndianMappedByteBuffer implements EndianInputOutput {
  private final MappedByteBuffer buffer;

  private final RandomAccessFile randomAccessFile;

  public EndianMappedByteBuffer(final File file, final MapMode mapMode) throws IOException {
    String mode = "r";
    if (mapMode.equals(MapMode.READ_WRITE)) {
      mode = "rw";
    }
    this.randomAccessFile = new RandomAccessFile(file, mode);
    final FileChannel channel = this.randomAccessFile.getChannel();
    this.buffer = channel.map(mapMode, 0, this.randomAccessFile.length());
    this.buffer.order(ByteOrder.BIG_ENDIAN);
  }

  public EndianMappedByteBuffer(final String name, final MapMode mapMode) throws IOException {
    this(new File(name), mapMode);
  }

  @Override
  public void close() {
    try {
      this.randomAccessFile.close();
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public void flush() {
  }

  @Override
  public long getFilePointer() {
    return this.buffer.position();
  }

  @Override
  public long length() throws IOException {
    return this.randomAccessFile.length();
  }

  @Override
  public int read() throws IOException {
    return this.buffer.get();
  }

  @Override
  public int read(final byte[] bytes) throws IOException {
    this.buffer.get(bytes);
    return bytes.length;
  }

  @Override
  public double readDouble() throws IOException {
    return this.buffer.getDouble();
  }

  @Override
  public int readInt() throws IOException {
    return this.buffer.getInt();
  }

  @Override
  public double readLEDouble() throws IOException {
    this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    try {
      return this.buffer.getDouble();
    } finally {
      this.buffer.order(ByteOrder.BIG_ENDIAN);
    }
  }

  @Override
  public float readLEFloat() throws IOException {
    final int value = readLEInt();
    return Float.intBitsToFloat(value);
  }

  @Override
  public int readLEInt() throws IOException {
    this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    try {
      return this.buffer.getInt();
    } finally {
      this.buffer.order(ByteOrder.BIG_ENDIAN);
    }
  }

  @Override
  public long readLELong() throws IOException {
    this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    try {
      return this.buffer.getLong();
    } finally {
      this.buffer.order(ByteOrder.BIG_ENDIAN);
    }
  }

  @Override
  public short readLEShort() throws IOException {
    this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    try {
      return this.buffer.getShort();
    } finally {
      this.buffer.order(ByteOrder.BIG_ENDIAN);
    }
  }

  @Override
  public long readLong() throws IOException {
    return this.buffer.getLong();
  }

  @Override
  public short readShort() throws IOException {
    return this.buffer.getShort();
  }

  @Override
  public void seek(final long index) throws IOException {
    this.buffer.position((int)index);
  }

  @Override
  public void setLength(final long length) throws IOException {
    this.randomAccessFile.setLength(length);
  }

  @Override
  public int skipBytes(final int i) throws IOException {
    this.buffer.position(this.buffer.position() + i);
    return this.buffer.position();
  }

  @Override
  public void write(final byte[] bytes) {
    this.buffer.put(bytes);
  }

  @Override
  public void write(final byte[] bytes, final int offset, final int length) {
    this.buffer.put(bytes, offset, length);
  }

  @Override
  public void write(final int i) {
    this.buffer.put((byte)i);
  }

  @Override
  public void writeBytes(final String s) {
    final int len = s.length();
    final byte[] bytes = new byte[len];
    s.getBytes(0, len, bytes, 0);
    write(bytes, 0, len);
  }

  @Override
  public void writeDouble(final double value) {
    this.buffer.putDouble(value);
  }

  @Override
  public void writeFloat(final float value) {
    this.buffer.putFloat(value);
  }

  @Override
  public void writeInt(final int value) {
    this.buffer.putInt(value);
  }

  @Override
  public void writeLEDouble(final double value) {
    this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    try {
      this.buffer.putDouble(value);
    } finally {
      this.buffer.order(ByteOrder.BIG_ENDIAN);
    }
  }

  @Override
  public void writeLEFloat(final float value) {
    this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    try {
      this.buffer.putFloat(value);
    } finally {
      this.buffer.order(ByteOrder.BIG_ENDIAN);
    }
  }

  @Override
  public void writeLEInt(final int value) {
    this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    try {
      this.buffer.putInt(value);
    } finally {
      this.buffer.order(ByteOrder.BIG_ENDIAN);
    }
  }

  @Override
  public void writeLELong(final long value) {
    this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    try {
      this.buffer.putLong(value);
    } finally {
      this.buffer.order(ByteOrder.BIG_ENDIAN);
    }
  }

  @Override
  public void writeLEShort(final short value) {
    this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    try {
      this.buffer.putShort(value);
    } finally {
      this.buffer.order(ByteOrder.BIG_ENDIAN);
    }
  }

  @Override
  public void writeLEUnsignedShort(final int s) {
    this.buffer.put((byte)(s >>> 0));
    this.buffer.put((byte)(s >>> 8));
  }

  @Override
  public void writeLong(final long value) {
    this.buffer.putLong(value);
  }

  @Override
  public void writeShort(final short value) {
    this.buffer.putShort(value);
  }
}
