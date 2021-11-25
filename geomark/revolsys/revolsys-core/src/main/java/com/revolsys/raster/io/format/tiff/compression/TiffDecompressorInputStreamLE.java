package com.revolsys.raster.io.format.tiff.compression;

import java.io.IOException;
import java.io.InputStream;

import com.revolsys.io.EndOfFileException;

public class TiffDecompressorInputStreamLE extends TiffDecompressorInputStream {

  public TiffDecompressorInputStreamLE(final InputStream in) {
    super(in);
  }

  @Override
  public int getBytesAsInt(final int byteCount) throws IOException {
    int value = 0;
    int shift = 0;
    for (int i = 0; i < byteCount; i++) {
      final int b = getByte();
      if (b == -1) {
        throw new EndOfFileException();
      }
      value |= b << shift;
      shift += 8;
    }
    return value;
  }

  @Override
  public int getShort() throws IOException {
    final int ch1 = getByte();
    final int ch2 = getByte();
    if ((ch1 | ch2) < 0) {
      throw new EndOfFileException();
    }
    return ch1 | ch2 << 8;
  }

  @Override
  public long getUnsignedInt() throws IOException {
    final long b1 = getByte();
    final long b2 = getByte();
    final long b3 = getByte();
    final long b4 = getByte();
    if ((b1 | b2 | b3 | b4) < 0) {
      throw new EndOfFileException();
    }
    return (b4 << 24) + (b3 << 16) + (b2 << 8) + b1;
  }
}
