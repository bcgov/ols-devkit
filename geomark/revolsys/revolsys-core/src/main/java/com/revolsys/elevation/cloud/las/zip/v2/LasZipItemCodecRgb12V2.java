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
package com.revolsys.elevation.cloud.las.zip.v2;

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.elevation.cloud.las.zip.LasZipItemCodec;
import com.revolsys.elevation.cloud.las.zip.context.LasZipContextRgb;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingCodec;
import com.revolsys.math.arithmeticcoding.ArithmeticDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticEncoder;

public class LasZipItemCodecRgb12V2 implements LasZipItemCodec {

  private ArithmeticDecoder decoder;

  private ArithmeticEncoder encoder;

  private final LasZipContextRgb context = new LasZipContextRgb();

  public LasZipItemCodecRgb12V2(final ArithmeticCodingCodec codec, final int size) {
    if (codec instanceof ArithmeticDecoder) {
      this.decoder = (ArithmeticDecoder)codec;
    } else if (codec instanceof ArithmeticEncoder) {
      this.encoder = (ArithmeticEncoder)codec;
    } else {
      throw new IllegalArgumentException("Not supported:" + codec.getClass());
    }
  }

  @Override
  public int getVersion() {
    return 2;
  }

  @Override
  public int init(final LasPoint point, final int contextIndex) {
    if (this.encoder != null) {
      this.context.initPoint(this.encoder, point);
    }
    if (this.decoder != null) {
      this.context.initPoint(this.decoder, point);
    }
    return contextIndex;
  }

  @Override
  public int read(final LasPoint point, final int contextIndex) {
    this.context.readRgb(this.decoder, point);
    return contextIndex;
  }

  @Override
  public int write(final LasPoint point, final int contextIndex) {
    this.context.writeRgb(this.encoder, point);
    return contextIndex;
  }

}
