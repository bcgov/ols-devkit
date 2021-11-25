/*
 * Copyright 2007-2012, martin isenburg, rapidlasso - fast tools to catch reality
 *
 * This is free software; you can redistribute and/or modify it under the
 * terms of the GNU Lesser General Licence as published by the Free Software
 * Foundation. See the LICENSE.txt file for more information.
 *
 * This software is distributed WITHOUT ANY WARRANTY and without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.revolsys.elevation.cloud.las.zip;

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;

public interface LasZipItemCodec {
  default int getVersion() {
    return 1;
  }

  default int I16_QUANTIZE(final float n) {
    if (n >= 0) {
      return (short)(n + 0.5f);
    } else {
      return (short)(n - 0.5f);
    }
  }

  default int I32_QUANTIZE(final float n) {
    return n >= 0 ? (int)(n + 0.5f) : (int)(n - 0.5f);
  }

  default byte I8_CLAMP(final int n) {
    if (n <= -128) {
      return (byte)-128;
    } else if (n >= 127) {
      return (byte)127;
    } else {
      return (byte)n;
    }
  }

  int init(LasPoint point, int context);

  int read(LasPoint point, int context);

  default void readChunkSizes() {
  }

  default int U32_ZERO_BIT_0(final int n) {
    return n & 0xFFFFFFFE;
  }

  default int U8_CLAMP(final int n) {
    if (n < 0) {
      return 0;
    } else if (n > 255) {
      return 255;
    } else {
      return n;
    }
  }

  default int U8_FOLD(final int n) {
    if (n < 0) {
      return n + 256;
    } else if (n > 255) {
      return n - 256;
    } else {
      return n;
    }
  }

  int write(LasPoint point, int context);

  default void writeChunkBytes() {
  }

  default void writeChunkSizes() {
  }
}
