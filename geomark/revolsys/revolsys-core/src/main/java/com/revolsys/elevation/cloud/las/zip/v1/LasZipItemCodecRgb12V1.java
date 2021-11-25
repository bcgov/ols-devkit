/*
 * Copyright 2007-2014, martin isenburg, rapidlasso - fast tools to catch reality
 *
 * This is free software; you can redistribute and/or modify it under the
 * terms of the GNU Lesser General Licence as published by the Free Software
 * Foundation. See the LICENSE.txt file for more information.
 *
 * This software is distributed WITHOUT ANY WARRANTY and without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.revolsys.elevation.cloud.las.zip.v1;

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.elevation.cloud.las.zip.LasZipItemCodec;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingCodec;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingInteger;
import com.revolsys.math.arithmeticcoding.ArithmeticDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticEncoder;
import com.revolsys.math.arithmeticcoding.ArithmeticModel;

public class LasZipItemCodecRgb12V1 implements LasZipItemCodec {

  private final ArithmeticCodingInteger ic_rgb;

  private ArithmeticDecoder decoder;

  private ArithmeticEncoder encoder;

  private final ArithmeticModel byteUsed;

  private int lastRedLower;

  private int lastRedUpper;

  private int lastGreenLower;

  private int lastGreenUpper;

  private int lastBlueLower;

  private int lastBlueUpper;

  public LasZipItemCodecRgb12V1(final ArithmeticCodingCodec codec, final int size) {
    if (codec instanceof ArithmeticDecoder) {
      this.decoder = (ArithmeticDecoder)codec;
    } else if (codec instanceof ArithmeticEncoder) {
      this.encoder = (ArithmeticEncoder)codec;
    } else {
      throw new IllegalArgumentException("Not supported:" + codec.getClass());
    }
    this.byteUsed = codec.createSymbolModel(64);
    this.ic_rgb = codec.newCodecInteger(8, 6);
  }

  @Override
  public int init(final LasPoint point, final int context) {
    this.byteUsed.init();
    this.ic_rgb.init();
    final int red = point.getRed();
    final int green = point.getGreen();
    final int blue = point.getBlue();
    this.lastRedLower = red & 0xFF;
    this.lastRedUpper = red >>> 8;
    this.lastGreenLower = green & 0xFF;
    this.lastGreenUpper = green >>> 8;
    this.lastBlueLower = blue & 0xFF;
    this.lastBlueUpper = blue >>> 8;
    return context;
  }

  @Override
  public int read(final LasPoint point, final int context) {
    final int sym = this.decoder.decodeSymbol(this.byteUsed);

    int redLower;
    int redUpper;
    if ((sym & 0b1) != 0) {
      redLower = this.ic_rgb.decompress(this.lastRedLower, 0);
      this.lastRedLower = redLower;
    } else {
      redLower = this.lastRedLower;
    }
    if ((sym & 0b10) != 0) {
      redUpper = this.ic_rgb.decompress(this.lastRedUpper, 1);
      this.lastRedUpper = redUpper;
    } else {
      redUpper = this.lastRedUpper;
    }

    int greenLower;
    int greenUpper;
    if ((sym & 0b100) != 0) {
      greenLower = this.ic_rgb.decompress(this.lastGreenLower, 2);
      this.lastGreenLower = greenLower;
    } else {
      greenLower = this.lastGreenLower;
    }
    if ((sym & 0b1000) != 0) {
      greenUpper = this.ic_rgb.decompress(this.lastGreenUpper, 3);
      this.lastGreenUpper = greenUpper;
    } else {
      greenUpper = this.lastGreenUpper;
    }

    int blueLower;
    int blueUpper;
    if ((sym & 0b10000) != 0) {
      blueLower = this.ic_rgb.decompress(this.lastBlueLower, 4);
      this.lastBlueLower = blueLower;
    } else {
      blueLower = this.lastBlueLower;
    }
    if ((sym & 0b100000) != 0) {
      blueUpper = this.ic_rgb.decompress(this.lastBlueUpper, 5);
      this.lastBlueUpper = blueUpper;
    } else {
      blueUpper = this.lastBlueUpper;
    }

    final int red = redUpper << 8 | redLower;
    final int green = greenUpper << 8 | greenLower;
    final int blue = blueUpper << 8 | blueLower;
    point.setRed(red);
    point.setGreen(green);
    point.setBlue(blue);
    return context;
  }

  @Override
  public int write(final LasPoint point, final int context) {
    final int red = point.getRed();
    final int green = point.getGreen();
    final int blue = point.getBlue();
    final int redLower = red & 0xFF;
    final int redUpper = red >>> 8;
    final int greenLower = green & 0xFF;
    final int greenUpper = green >>> 8;
    final int blueLower = blue & 0xFF;
    final int blueUpper = blue >>> 8;
    int sym = 0;
    final boolean redLowerChanged = this.lastRedLower != redLower;
    if (redLowerChanged) {
      sym |= 0b1;
    }
    final boolean redUpperChanged = this.lastRedUpper != redUpper;
    if (redUpperChanged) {
      sym |= 0b10;
    }
    final boolean greenLowerChanged = this.lastGreenLower != greenLower;
    if (greenLowerChanged) {
      sym |= 0b100;
    }
    final boolean greenUpperChanged = this.lastGreenUpper != greenUpper;
    if (greenUpperChanged) {
      sym |= 0b1000;
    }
    final boolean blueLowerChanged = this.lastBlueLower != blueLower;
    if (blueLowerChanged) {
      sym |= 0b10000;
    }
    final boolean blueUpperChanged = this.lastBlueUpper != blueUpper;
    if (blueUpperChanged) {
      sym |= 0b100000;
    }

    this.encoder.encodeSymbol(this.byteUsed, sym);
    if (redLowerChanged) {
      this.ic_rgb.compress(this.lastRedLower, redLower, 0);
      this.lastRedLower = redLower;
    }
    if (redUpperChanged) {
      this.ic_rgb.compress(this.lastRedUpper, redUpper, 1);
      this.lastRedUpper = redUpper;
    }
    if (greenLowerChanged) {
      this.ic_rgb.compress(this.lastGreenLower, greenLower, 2);
      this.lastGreenLower = greenLower;
    }
    if (greenUpperChanged) {
      this.ic_rgb.compress(this.lastGreenUpper, greenUpper, 3);
      this.lastGreenUpper = greenUpper;
    }
    if (blueLowerChanged) {
      this.ic_rgb.compress(this.lastBlueLower, blueLower, 4);
      this.lastBlueLower = blueLower;
    }
    if (blueUpperChanged) {
      this.ic_rgb.compress(this.lastBlueUpper, blueUpper, 5);
      this.lastBlueUpper = blueUpper;
    }
    return context;
  }

}
