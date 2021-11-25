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

import com.revolsys.collection.ArrayUtil;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.elevation.cloud.las.zip.ArithmeticDecoderByteArray;
import com.revolsys.elevation.cloud.las.zip.ArithmeticEncoderByteArray;
import com.revolsys.elevation.cloud.las.zip.LasZipDecompressSelective;
import com.revolsys.elevation.cloud.las.zip.LasZipItemCodec;
import com.revolsys.elevation.cloud.las.zip.context.LasZipContextByte;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.io.channels.DataReader;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingCodec;
import com.revolsys.math.arithmeticcoding.ArithmeticDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticEncoder;

public class LasZipItemCodecByte14V3 implements LasZipItemCodec {

  private final LasZipContextByte[] contexts = new LasZipContextByte[4];

  private int currentContextIndex;

  private ArithmeticDecoder decoder;

  private ArithmeticEncoder encoder;

  private ArithmeticDecoderByteArray[] byteDecoders;

  private ArithmeticEncoderByteArray[] byteEncoders;

  private final boolean[] bytesChanged;

  private final int version;

  public LasZipItemCodecByte14V3(final ArithmeticCodingCodec codec, final int version,
    final int size) {
    if (codec instanceof ArithmeticDecoder) {
      this.decoder = (ArithmeticDecoder)codec;
      this.byteDecoders = new ArithmeticDecoderByteArray[size];
      ArrayUtil.fill(this.byteDecoders, ArithmeticDecoderByteArray::new);
    } else if (codec instanceof ArithmeticEncoder) {
      this.encoder = (ArithmeticEncoder)codec;
      this.byteEncoders = new ArithmeticEncoderByteArray[size];
      ArrayUtil.fill(this.byteEncoders, ArithmeticEncoderByteArray::new);
    } else {
      throw new IllegalArgumentException("Not supported:" + codec.getClass());
    }
    this.version = version;
    this.bytesChanged = new boolean[size];

    for (int i = 0; i < this.contexts.length; i++) {
      this.contexts[i] = new LasZipContextByte(codec, size);
    }
    final int decompressSelective = LasZipDecompressSelective.ALL;
    for (int i = 0; i < this.byteDecoders.length; i++) {
      final ArithmeticDecoderByteArray byteDecoder = this.byteDecoders[i];
      byteDecoder.setEnabled(decompressSelective, LasZipDecompressSelective.BYTE0 << i);

    }
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
    LasZipContextByte context = this.contexts[this.currentContextIndex];
    final byte[] lastItem = context.lastItem;
    if (this.currentContextIndex != contextIndex) {
      this.currentContextIndex = contextIndex;
      context = this.contexts[this.currentContextIndex];
      if (context.unused) {
        context.initPoint(this.decoder, point);
      }
    }
    return contextIndex;
  }

  @Override
  public void readChunkSizes() {
    final DataReader in = this.decoder.getIn();
    for (final ArithmeticDecoderByteArray byteDecoder : this.byteDecoders) {
      byteDecoder.readSize(in);
    }
  }

  public int readInit(final LasPoint point) {
    final DataReader in = this.decoder.getIn();

    for (int i = 0; i < this.byteDecoders.length; i++) {
      final ArithmeticDecoderByteArray byteDecoder = this.byteDecoders[i];
      if (byteDecoder.readBytes(in)) {
        this.bytesChanged[i] = true;
      }
    }

    for (final LasZipContextByte context : this.contexts) {
      context.unused = true;
    }
    final LasZipContextByte context = this.contexts[this.currentContextIndex];
    context.initPoint(this.decoder, point);

    return this.currentContextIndex;
  }

  @Override
  public int write(final LasPoint point, final int contextIndex) {
    return contextIndex;
  }

  @Override
  public void writeChunkBytes() {
    final ChannelWriter writer = this.encoder.getWriter();
  }

  @Override
  public void writeChunkSizes() {
    final ChannelWriter writer = this.encoder.getWriter();
  }

  public void writeInit(final LasPoint point) {
  }
}
