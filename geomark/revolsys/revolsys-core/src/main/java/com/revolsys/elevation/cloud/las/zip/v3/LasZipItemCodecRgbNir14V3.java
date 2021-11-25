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
package com.revolsys.elevation.cloud.las.zip.v3;

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.elevation.cloud.las.zip.ArithmeticDecoderByteArray;
import com.revolsys.elevation.cloud.las.zip.ArithmeticEncoderByteArray;
import com.revolsys.elevation.cloud.las.zip.LasZipDecompressSelective;
import com.revolsys.elevation.cloud.las.zip.LasZipItemCodec;
import com.revolsys.elevation.cloud.las.zip.context.LasZipContextRgb;
import com.revolsys.elevation.cloud.las.zip.context.LasZipContextRgbNir;
import com.revolsys.elevation.cloud.las.zip.context.RgbUpperLower;
import com.revolsys.elevation.cloud.las.zip.context.ShortUpperLower;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.io.channels.DataReader;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingCodec;
import com.revolsys.math.arithmeticcoding.ArithmeticDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticEncoder;

public class LasZipItemCodecRgbNir14V3 implements LasZipItemCodec {

  private final LasZipContextRgbNir[] contexts = new LasZipContextRgbNir[4];

  private int currentContextIndex;

  private ArithmeticDecoder decoder;

  private ArithmeticEncoder encoder;

  private boolean nirChanged;

  private final ArithmeticDecoderByteArray nirDecoder = new ArithmeticDecoderByteArray();

  private final ArithmeticEncoderByteArray nirEncoder = new ArithmeticEncoderByteArray();

  private boolean rgbChanged;

  private final ArithmeticDecoderByteArray rgbDecoder = new ArithmeticDecoderByteArray();

  private final ArithmeticEncoderByteArray rgbEncoder = new ArithmeticEncoderByteArray();

  private final int version;

  public LasZipItemCodecRgbNir14V3(final ArithmeticCodingCodec codec, final int version) {
    if (codec instanceof ArithmeticDecoder) {
      this.decoder = (ArithmeticDecoder)codec;
    } else if (codec instanceof ArithmeticEncoder) {
      this.encoder = (ArithmeticEncoder)codec;
    } else {
      throw new IllegalArgumentException("Not supported:" + codec.getClass());
    }
    this.version = version;

    for (int i = 0; i < this.contexts.length; i++) {
      this.contexts[i] = new LasZipContextRgbNir();
    }
    final int decompressSelective = LasZipDecompressSelective.ALL;
    this.rgbDecoder.setEnabled(decompressSelective, LasZipDecompressSelective.RGB);
    this.nirDecoder.setEnabled(decompressSelective, LasZipDecompressSelective.NIR);
  }

  @Override
  public int getVersion() {
    return this.version;
  }

  @Override
  public int init(final LasPoint point, final int contextIndex) {
    this.currentContextIndex = contextIndex;
    if (this.encoder != null) {
      writeInit(point);
    }

    if (this.decoder != null) {
      readInit(point);
    }
    return this.currentContextIndex;
  }

  @Override
  public int read(final LasPoint point, final int contextIndex) {
    LasZipContextRgbNir context = this.contexts[this.currentContextIndex];
    RgbUpperLower lastRgb = context.lastRgb;
    ShortUpperLower lastNir = context.lastNir;
    if (this.currentContextIndex != contextIndex) {
      this.currentContextIndex = contextIndex;
      context = this.contexts[this.currentContextIndex];
      if (context.unused) {
        context.initRgb(this.decoder, lastRgb);
        if (this.version < 4) {
          lastRgb = context.lastRgb;
          lastNir = context.lastNir;
        }
      }
      if (this.version >= 4) {
        lastRgb = context.lastRgb;
        lastNir = context.lastNir;
      }
    }
    if (this.rgbChanged) {
      context.readRgb(this.rgbDecoder, point, lastRgb);
    }
    if (this.nirChanged) {
      context.readNir(this.nirDecoder, point, lastNir);
    }
    return contextIndex;
  }

  @Override
  public void readChunkSizes() {
    final DataReader in = this.decoder.getIn();
    this.rgbDecoder.readSize(in);
    this.nirDecoder.readSize(in);
  }

  public int readInit(final LasPoint point) {
    final DataReader in = this.decoder.getIn();

    this.rgbChanged = this.rgbDecoder.readBytes(in);

    this.nirChanged = this.nirDecoder.readBytes(in);

    for (final LasZipContextRgb context : this.contexts) {
      context.unused = true;
    }
    final LasZipContextRgb context = this.contexts[this.currentContextIndex];
    context.initPoint(this.decoder, point);

    return this.currentContextIndex;
  }

  @Override
  public int write(final LasPoint point, final int contextIndex) {
    LasZipContextRgbNir context = this.contexts[this.currentContextIndex];
    ShortUpperLower lastNir = context.lastNir;
    RgbUpperLower lastRgb = context.lastRgb;
    if (this.currentContextIndex != contextIndex) {
      this.currentContextIndex = contextIndex;
      context = this.contexts[this.currentContextIndex];
      if (context.unused) {
        context.initRgb(this.encoder, lastRgb);
        if (this.version < 4) {
          lastRgb = context.lastRgb;
          lastNir = context.lastNir;
        }
      }
      if (this.version >= 4) {
        lastRgb = context.lastRgb;
        lastNir = context.lastNir;
      }
    }

    if (context.writeRgb(this.rgbEncoder, point, lastRgb)) {
      this.rgbChanged = true;
    }
    if (context.writeNir(this.nirEncoder, point, lastNir)) {
      this.nirChanged = true;
    }
    return contextIndex;

  }

  @Override
  public void writeChunkBytes() {
    final ChannelWriter writer = this.encoder.getWriter();
    this.rgbEncoder.writeBytes(writer);
    this.nirEncoder.writeBytes(writer);
  }

  @Override
  public void writeChunkSizes() {
    final ChannelWriter writer = this.encoder.getWriter();
    this.rgbEncoder.writeSize(writer, this.rgbChanged);
    this.nirEncoder.writeSize(writer, this.nirChanged);
  }

  public void writeInit(final LasPoint point) {
    this.rgbEncoder.init();
    this.rgbChanged = false;
    this.nirEncoder.init();
    this.nirChanged = false;
    for (final LasZipContextRgb context : this.contexts) {
      context.unused = true;
    }
    final LasZipContextRgb context = this.contexts[this.currentContextIndex];
    context.initPoint(this.encoder, point);
  }
}
