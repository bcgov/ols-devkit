package com.revolsys.math.arithmeticcoding;

public interface ArithmeticCodingCodec {

  ArithmeticModel createSymbolModel(int symbolCount);

  default ArithmeticModel createSymbolModelInit(final int symbolCount) {
    final ArithmeticModel model = createSymbolModel(symbolCount);
    model.init();
    return model;
  }

  default void initModel(final ArithmeticModel model) {
    if (model != null) {
      model.init();
    }
  }

  default ArithmeticCodingInteger newCodecInteger(final int bits) {
    return newCodecInteger(bits, 1);
  }

  default ArithmeticCodingInteger newCodecInteger(final int bits, final int contexts) {
    return new ArithmeticCodingInteger(this, bits, contexts);
  }
}
