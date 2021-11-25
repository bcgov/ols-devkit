package com.revolsys.elevation.cloud.las.zip.context;

import org.jeometry.common.number.Integers;

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingCodec;
import com.revolsys.math.arithmeticcoding.ArithmeticDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticEncoder;
import com.revolsys.math.arithmeticcoding.ArithmeticModel;

public class LasZipContextRgbNir extends LasZipContextRgb {

  private ArithmeticModel nirBytesUsed;

  private ArithmeticModel diffNirLower;

  private ArithmeticModel diffNirUpper;

  public ShortUpperLower lastNir = new ShortUpperLower();

  public LasZipContextRgbNir() {
  }

  @Override
  public void initPoint(final ArithmeticCodingCodec codec, final LasPoint point) {
    super.initPoint(codec, point);
    if (this.nirBytesUsed == null) {
      this.nirBytesUsed = codec.createSymbolModel(128);
      this.diffNirLower = codec.createSymbolModel(256);
      this.diffNirUpper = codec.createSymbolModel(256);
    } else {
      this.nirBytesUsed.init();
      this.diffNirLower.init();
      this.diffNirUpper.init();
    }
    final int nir = point.getNir();
    this.lastNir.setValues(nir);
  }

  public void readNir(final ArithmeticDecoder decoder, final LasPoint point,
    final ShortUpperLower lastNir) {
    int nirLower = lastNir.lower;
    int nirUpper = lastNir.upper;

    final int sym = decoder.decodeSymbol(this.nirBytesUsed);
    if ((sym & 0b1) != 0) {
      final int corr = decoder.decodeSymbol(this.diffNirLower);
      nirLower = Integers.U8_FOLD(nirLower + corr);
      lastNir.lower = nirLower;
    }

    if ((sym & 0b10) != 0) {
      final int corr = decoder.decodeSymbol(this.diffNirUpper);
      nirUpper = Integers.U8_FOLD(nirUpper + corr);
      lastNir.upper = nirUpper;
    }

    final int nir = nirUpper << 8 | nirLower;
    point.setNir(nir);
  }

  public boolean writeNir(final ArithmeticEncoder encoder, final LasPoint point,
    final ShortUpperLower lastNir) {
    final int nir = point.getNir();

    final int nirLower = nir & 0xFF;
    final int nirUpper = nir >>> 8;
    int sym = 0;
    final boolean nirLowerChanged = lastNir.lower != nirLower;
    if (nirLowerChanged) {
      sym |= 0b1;
    }
    final boolean nirUpperChanged = lastNir.upper != nirUpper;
    if (nirUpperChanged) {
      sym |= 0b10;
    }

    encoder.encodeSymbol(this.nirBytesUsed, sym);
    if (nirLowerChanged) {
      final int nirLowerDiff = nirLower - lastNir.lower;
      encoder.encodeSymbol(this.diffNirLower, Integers.U8_FOLD(nirLowerDiff));
      lastNir.lower = nirLower;
    }
    if (nirUpperChanged) {
      final int nirUpperDiff = nirUpper - lastNir.upper;
      encoder.encodeSymbol(this.diffNirUpper, Integers.U8_FOLD(nirUpperDiff));
      lastNir.upper = nirUpper;
    }

    return sym != 0;
  }

}
