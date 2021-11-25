package com.revolsys.raster.io.format.tiff.compression;

import java.io.IOException;
import java.io.InputStream;

public class TiffThunderscanInputStream extends InputStream {

  private static final int[] TWO_BIT_DELTAS = {
    0, 1, 0, -1
  };

  private static final int[] THREE_BIT_DELTAS = {
    0, 1, 2, 3, 0, -3, -2, -1
  };

  private final InputStream in;

  private int count = 0;

  private int deltaCount = 0;

  private int previousValue;

  private int currentFlag = -1;

  private final int[] deltas = new int[3];

  public TiffThunderscanInputStream(final InputStream in) {
    this.in = in;
  }

  public void clear() {
    this.count = 0;
  }

  @Override
  public void close() throws IOException {
    this.in.close();
  }

  private int getDeltaValue() {
    final int index = this.deltaCount - this.count;
    this.count--;
    final int delta = this.deltas[index];
    final int value = this.previousValue + delta;
    this.previousValue = value & 0b1111;
    return this.previousValue;
  }

  @Override
  public int read() throws IOException {
    final int upper = readNibble();
    final int lower = readNibble();
    if (upper == -1) {
      return 0;
    } else if (lower == -1) {
      return upper << 4;
    } else {
      return upper << 4 | lower;
    }
  }

  private int readNibble() throws IOException {
    if (this.count > 0) {
      switch (this.currentFlag) {
        case 0b00:
          this.count--;
          return this.previousValue;
        case 0b01:
          return getDeltaValue();
        case 0b10:
          return getDeltaValue();
        default:
          throw new IllegalStateException();
      }
    } else {
      this.count = 0;
      final int nextByte = this.in.read();
      if (nextByte == -1) {
        return -1;
      } else {
        this.currentFlag = nextByte >> 6;
        switch (this.currentFlag) {
          case 0b00:
            this.count = nextByte & 0b111111 - 1;
            return this.previousValue;
          case 0b01:
            for (int shift = 4; shift >= 0; shift -= 2) {
              final int deltaIndex = nextByte >> shift & 0b11;

              final int delta = TWO_BIT_DELTAS[deltaIndex];
              if (delta != 0) {
                this.deltas[this.count] = delta;
                this.count++;
              }
            }
            this.deltaCount = this.count;
            return getDeltaValue();
          case 0b10:
            for (int shift = 3; shift >= 0; shift -= 3) {
              final int deltaIndex = nextByte >> shift & 0b111;
              final int delta = THREE_BIT_DELTAS[deltaIndex];
              if (delta != 0) {
                this.deltas[this.count] = delta;
                this.count++;
              }
            }
            this.deltaCount = this.count;
            return getDeltaValue();
          case 0b11:

            this.previousValue = nextByte & 0b1111;
            return this.previousValue;
          default:
            throw new IllegalStateException();
        }
      }
    }
  }
}
