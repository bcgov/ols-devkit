package com.revolsys.raster.io.format.tiff.compression;

import java.io.IOException;
import java.io.InputStream;

import com.revolsys.io.BaseCloseable;

public interface TiffDecompressor extends BaseCloseable {

  default void endRow() {
  }

  int getBitsAsInt(int bitCount) throws IOException;

  int getByte() throws IOException;

  int getBytesAsInt(int byteCount) throws IOException;

  InputStream getInputStream();

  int getShort() throws IOException;

  long getUnsignedInt() throws IOException;
}
