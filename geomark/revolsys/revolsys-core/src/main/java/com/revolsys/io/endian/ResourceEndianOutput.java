package com.revolsys.io.endian;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.FileUtil;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;

public class ResourceEndianOutput implements EndianOutput {
  private final File file;

  private EndianOutput out;

  private final Resource resource;

  private OutputStream resourceOut;

  public ResourceEndianOutput(final Resource resource) throws IOException {
    this.resource = resource;
    if (!(resource instanceof PathResource)) {
      this.resourceOut = resource.newBufferedOutputStream();
    }
    this.file = Resource.getFileOrCreateTempFile(resource);
    final OutputStream out = new FileOutputStream(this.file);
    final BufferedOutputStream bufferedOut = new BufferedOutputStream(out);
    this.out = new EndianOutputStream(bufferedOut);
  }

  @Override
  public void close() {
    try {
      this.out.close();
    } catch (final Throwable e) {
      throw Exceptions.wrap(e);
    } finally {
      if (!(this.resource instanceof PathResource)) {
        try {
          FileUtil.copy(this.file, this.resourceOut);
          this.resourceOut.flush();
        } catch (final Throwable e) {
          throw Exceptions.wrap(e);
        } finally {
          FileUtil.closeSilent(this.resourceOut);
          if (!(this.resource instanceof PathResource)) {
            this.file.delete();
          }
        }
      }
    }
  }

  @Override
  public void flush() {
    this.out.flush();

  }

  @Override
  public long getFilePointer() throws IOException {
    return this.out.getFilePointer();
  }

  @Override
  public long length() throws IOException {
    return this.out.length();
  }

  public void seek(final long pos) throws IOException {
    final LittleEndianRandomAccessFile raOut;
    if (this.out instanceof LittleEndianRandomAccessFile) {
      raOut = (LittleEndianRandomAccessFile)this.out;
    } else {
      this.out.flush();
      this.out.close();
      raOut = new LittleEndianRandomAccessFile(this.file, "rw");
      this.out = raOut;
    }
    raOut.seek(pos);
  }

  @Override
  public void write(final byte[] bytes) {
    this.out.write(bytes);
  }

  @Override
  public void write(final byte[] bytes, final int offset, final int length) {
    this.out.write(bytes, offset, length);
  }

  @Override
  public void write(final int i) {
    this.out.write(i);
  }

  @Override
  public void writeBytes(final String s) {
    this.out.writeBytes(s);
  }

  @Override
  public void writeDouble(final double d) {
    this.out.writeDouble(d);
  }

  @Override
  public void writeFloat(final float f) {
    this.out.writeFloat(f);
  }

  @Override
  public void writeInt(final int i) {
    this.out.writeInt(i);
  }

  @Override
  public void writeLEDouble(final double d) {
    this.out.writeLEDouble(d);
  }

  @Override
  public void writeLEFloat(final float f) {
    this.out.writeLEFloat(f);
  }

  @Override
  public void writeLEInt(final int i) {
    this.out.writeLEInt(i);
  }

  @Override
  public void writeLELong(final long l) {
    this.out.writeLELong(l);

  }

  @Override
  public void writeLEShort(final short s) {
    this.out.writeLEShort(s);
  }

  @Override
  public void writeLEUnsignedShort(final int s) {
    this.out.writeLEUnsignedShort(s);
  }

  @Override
  public void writeLong(final long l) {
    this.out.writeLong(l);
  }

  @Override
  public void writeShort(final short s) {
    this.out.writeShort(s);
  }
}
