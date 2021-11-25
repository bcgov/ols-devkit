package com.revolsys.raster.io.format.tiff.compression;

import java.io.IOException;
import java.io.InputStream;

public class TiffPackbitsInputStream extends InputStream {

  private final InputStream in;

  private int count = 0;

  /** Copy the next bytes or repeat the same byte. */
  private boolean copyBytes;

  private int currentByte;

  public TiffPackbitsInputStream(final InputStream in) {
    this.in = in;
  }

  public void clear() {
    this.count = 0;
  }

  @Override
  public void close() throws IOException {
    this.in.close();
  }

  @Override
  public int read() throws IOException {
    if (this.count == 0) {
      final int countInt = this.in.read();
      if (countInt == -1) {
        return -1;
      } else {
        final byte countByte = (byte)countInt;
        if (countByte >= 0) {
          this.count = countByte + 1;
          this.copyBytes = true;
        } else if (countByte == -128) {
          throw new RuntimeException("Packbits cannot have count of -128");
        } else {
          this.count = -countByte + 1;
          this.currentByte = this.in.read();
          this.copyBytes = false;
        }
      }
    }
    this.count--;
    if (this.copyBytes) {
      return this.in.read();
    } else {
      return this.currentByte;
    }
  }
}
