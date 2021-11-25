package com.revolsys.elevation.cloud.las.zip.context;

import org.jeometry.common.number.Integers;

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingCodec;
import com.revolsys.math.arithmeticcoding.ArithmeticDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticEncoder;
import com.revolsys.math.arithmeticcoding.ArithmeticModel;

public class LasZipContextRgb {

  public boolean unused;

  private ArithmeticModel rgbBytesUsed;

  private ArithmeticModel diffRedLower;

  private ArithmeticModel diffRedUpper;

  private ArithmeticModel diffGreenLower;

  private ArithmeticModel diffGreenUpper;

  private ArithmeticModel diffBlueUpper;

  private ArithmeticModel diffBlueLower;

  public final RgbUpperLower lastRgb = new RgbUpperLower();

  public void initPoint(final ArithmeticCodingCodec codec, final LasPoint point) {
    this.unused = false;
    if (this.rgbBytesUsed == null) {
      this.rgbBytesUsed = codec.createSymbolModel(128);
      this.diffRedLower = codec.createSymbolModel(256);
      this.diffRedUpper = codec.createSymbolModel(256);
      this.diffGreenLower = codec.createSymbolModel(256);
      this.diffGreenUpper = codec.createSymbolModel(256);
      this.diffBlueLower = codec.createSymbolModel(256);
      this.diffBlueUpper = codec.createSymbolModel(256);
    } else {
      this.rgbBytesUsed.init();
      this.diffRedLower.init();
      this.diffRedUpper.init();
      this.diffGreenLower.init();
      this.diffGreenUpper.init();
      this.diffBlueLower.init();
      this.diffBlueUpper.init();
    }

    this.lastRgb.setValues(point);
    ;
  }

  public void initRgb(final ArithmeticCodingCodec codec, final RgbUpperLower rgb) {
    this.unused = false;
    if (this.rgbBytesUsed == null) {
      this.rgbBytesUsed = codec.createSymbolModel(128);
      this.diffRedLower = codec.createSymbolModel(256);
      this.diffRedUpper = codec.createSymbolModel(256);
      this.diffGreenLower = codec.createSymbolModel(256);
      this.diffGreenUpper = codec.createSymbolModel(256);
      this.diffBlueLower = codec.createSymbolModel(256);
      this.diffBlueUpper = codec.createSymbolModel(256);
    } else {
      this.rgbBytesUsed.init();
      this.diffRedLower.init();
      this.diffRedUpper.init();
      this.diffGreenLower.init();
      this.diffGreenUpper.init();
      this.diffBlueLower.init();
      this.diffBlueUpper.init();
    }

    this.lastRgb.setValues(rgb);
  }

  public void readRgb(final ArithmeticDecoder decoder, final LasPoint point) {
    readRgb(decoder, point, this.lastRgb);
  }

  public void readRgb(final ArithmeticDecoder decoder, final LasPoint point,
    final RgbUpperLower lastRgb) {
    int redLower = lastRgb.redLower;
    int redUpper = lastRgb.redUpper;
    int greenLower = lastRgb.greenLower;
    int greenUpper = lastRgb.greenUpper;
    int blueLower = lastRgb.blueLower;
    int blueUpper = lastRgb.blueUpper;

    int redLowerDiff = 0;
    final int sym = decoder.decodeSymbol(this.rgbBytesUsed);
    if ((sym & 0b1) != 0) {
      final int corr = decoder.decodeSymbol(this.diffRedLower);
      redLower = Integers.U8_FOLD(redLower + corr);
      redLowerDiff = redLower - lastRgb.redLower;
      lastRgb.redLower = redLower;
    }

    int redUpperDiff = 0;
    if ((sym & 0b10) != 0) {
      final int corr = decoder.decodeSymbol(this.diffRedUpper);
      redUpper = Integers.U8_FOLD(redUpper + corr);
      redUpperDiff = redUpper - lastRgb.redUpper;
      lastRgb.redUpper = redUpper;
    }
    final int red = redUpper << 8 | redLower;

    if ((sym & 0b1000000) != 0) {
      int greenLowerDiff = 0;
      if ((sym & 0b100) != 0) {
        final int corr = decoder.decodeSymbol(this.diffGreenLower);
        greenLower = Integers.U8_FOLD(Integers.U8_CLAMP(redLowerDiff + greenLower) + corr);
        greenLowerDiff = (redLowerDiff + greenLower - lastRgb.greenLower) / 2;
        lastRgb.greenLower = greenLower;
      }

      if ((sym & 0b10000) != 0) {
        final int corr = decoder.decodeSymbol(this.diffBlueLower);
        blueLower = Integers.U8_FOLD(Integers.U8_CLAMP(greenLowerDiff + blueLower) + corr);
        lastRgb.blueLower = blueLower;
      }

      int greenUpperDiff = 0;
      if ((sym & 0b1000) != 0) {
        final int corr = decoder.decodeSymbol(this.diffGreenUpper);
        greenUpper = Integers.U8_FOLD(Integers.U8_CLAMP(redUpperDiff + greenUpper) + corr);
        greenUpperDiff = (redUpperDiff + greenUpper - lastRgb.greenUpper) / 2;
        lastRgb.greenUpper = greenUpper;
      }

      if ((sym & 0b100000) != 0) {
        final int corr = decoder.decodeSymbol(this.diffBlueUpper);
        blueUpper = Integers.U8_FOLD(Integers.U8_CLAMP(greenUpperDiff + blueUpper) + corr);
        lastRgb.blueUpper = blueUpper;
      }
      final int green = greenUpper << 8 | greenLower;
      final int blue = blueUpper << 8 | blueLower;
      point.setGreen(green);
      point.setBlue(blue);
    } else {
      point.setGreen(red);
      point.setBlue(red);
      lastRgb.greenLower = redLower;
      lastRgb.greenUpper = redUpper;
      lastRgb.blueLower = redLower;
      lastRgb.blueUpper = redUpper;
    }

    point.setRed(red);
  }

  public boolean writeRgb(final ArithmeticEncoder encoder, final LasPoint point) {
    return writeRgb(encoder, point, this.lastRgb);

  }

  public boolean writeRgb(final ArithmeticEncoder encoder, final LasPoint point,
    final RgbUpperLower lastRgb) {
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
    final boolean redLowerChanged = lastRgb.redLower != redLower;
    if (redLowerChanged) {
      sym |= 0b1;
    }
    final boolean redUpperChanged = lastRgb.redUpper != redUpper;
    if (redUpperChanged) {
      sym |= 0b10;
    }
    final boolean greenLowerChanged = lastRgb.greenLower != greenLower;
    if (greenLowerChanged) {
      sym |= 0b100;
    }
    final boolean greenUpperChanged = lastRgb.greenUpper != greenUpper;
    if (greenUpperChanged) {
      sym |= 0b1000;
    }
    final boolean blueLowerChanged = lastRgb.blueLower != blueLower;
    if (blueLowerChanged) {
      sym |= 0b10000;
    }
    final boolean blueUpperChanged = lastRgb.blueUpper != blueUpper;
    if (blueUpperChanged) {
      sym |= 0b100000;
    }

    final boolean redDiffFromGreenAndBlue = redLower != greenLower || redLower != blueLower
      || redUpper != greenUpper || redUpper != blueUpper;
    if (redDiffFromGreenAndBlue) {
      sym |= 0b1000000;
    }

    encoder.encodeSymbol(this.rgbBytesUsed, sym);
    int redLowerDiff = 0;
    if (redLowerChanged) {
      redLowerDiff = redLower - lastRgb.redLower;
      encoder.encodeSymbol(this.diffRedLower, Integers.U8_FOLD(redLowerDiff));
      lastRgb.redLower = redLower;
    }
    int redUpperDiff = 0;
    if (redUpperChanged) {
      redUpperDiff = redUpper - lastRgb.redUpper;
      encoder.encodeSymbol(this.diffRedUpper, Integers.U8_FOLD(redUpperDiff));
      lastRgb.redUpper = redUpper;
    }
    if (redDiffFromGreenAndBlue) {
      int greenLowerDiff = 0;
      if (greenLowerChanged) {
        greenLowerDiff = (redLowerDiff + greenLower - lastRgb.greenLower) / 2;
        final int corr = greenLower - Integers.U8_CLAMP(redLowerDiff + lastRgb.greenLower);
        encoder.encodeSymbol(this.diffGreenLower, Integers.U8_FOLD(corr));
        lastRgb.greenLower = greenLower;
      }
      if (blueLowerChanged) {
        final int corr = blueLower - Integers.U8_CLAMP(greenLowerDiff + lastRgb.blueLower);
        encoder.encodeSymbol(this.diffBlueLower, Integers.U8_FOLD(corr));
        lastRgb.blueLower = blueLower;
      }
      int greenUpperDiff = 0;
      if (greenUpperChanged) {
        greenUpperDiff = (redUpperDiff + greenUpper - lastRgb.greenUpper) / 2;
        final int corr = greenUpper - Integers.U8_CLAMP(redUpperDiff + lastRgb.greenUpper);
        encoder.encodeSymbol(this.diffGreenUpper, Integers.U8_FOLD(corr));
        lastRgb.greenUpper = greenUpper;
      }
      if (blueUpperChanged) {
        final int corr = blueUpper - Integers.U8_CLAMP(greenUpperDiff + lastRgb.blueUpper);
        encoder.encodeSymbol(this.diffBlueUpper, Integers.U8_FOLD(corr));
        lastRgb.blueUpper = blueUpper;
      }
    } else {
      lastRgb.greenLower = redLower;
      lastRgb.greenUpper = redUpper;
      lastRgb.blueLower = redLower;
      lastRgb.blueUpper = redUpper;
    }
    return sym != 0;
  }

}
