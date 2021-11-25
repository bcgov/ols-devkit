package com.revolsys.raster.io.format.tiff.compression;

import java.io.IOException;
import java.io.InputStream;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.EndOfFileException;

public abstract class TiffDecompressorInputStream implements TiffDecompressor {

  private static final int[] MASKS = {
    0, 0b1, 0b11, 0b111, 0b1111, 0b11111, 0b111111, 0b1111111, 0b11111111
  };

  private final InputStream in;

  private int bits = -1;

  private int bitsRemaining = 0;

  public TiffDecompressorInputStream(final InputStream in) {
    this.in = in;
  }

  @Override
  public void close() {
    try {
      this.in.close();
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public void endRow() {
    this.bitsRemaining = 0;
  }

  @Override
  public int getBitsAsInt(int bitCount) throws IOException {
    int value = 0;
    while (bitCount > 0) {
      if (this.bitsRemaining == 0) {
        this.bits = this.in.read();
        if (this.bits == -1) {
          throw new EndOfFileException();
        }
        this.bitsRemaining = 8;
      }
      int countToRead = bitCount;
      if (countToRead > this.bitsRemaining) {
        countToRead = this.bitsRemaining;
        bitCount -= this.bitsRemaining;
      } else {
        bitCount = 0;
      }
      value <<= countToRead;

      this.bitsRemaining -= countToRead;
      final int bits = this.bits >> this.bitsRemaining;

      final int mask = MASKS[countToRead];
      value |= bits & mask;
    }
    return value;
  }

  @Override
  public int getByte() throws IOException {
    return this.in.read();
  }

  @Override
  public InputStream getInputStream() {
    return this.in;
  }

}
