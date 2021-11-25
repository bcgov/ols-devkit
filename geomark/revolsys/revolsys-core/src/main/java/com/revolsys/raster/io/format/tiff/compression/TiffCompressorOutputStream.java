package com.revolsys.raster.io.format.tiff.compression;

import java.io.IOException;
import java.io.OutputStream;

import org.jeometry.common.exception.Exceptions;

public abstract class TiffCompressorOutputStream implements TiffCompressor {

  private final OutputStream out;

  public TiffCompressorOutputStream(final OutputStream out) {
    this.out = out;
  }

  @Override
  public void close() {
    try {
      this.out.close();
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public void endRow() {
    // this.bitsRemaining = 0;
  }

  @Override
  public OutputStream getOututStream() {
    return this.out;
  }

  @Override
  public void writeByte(final int value) throws IOException {
    this.out.write(value);
  }

}
