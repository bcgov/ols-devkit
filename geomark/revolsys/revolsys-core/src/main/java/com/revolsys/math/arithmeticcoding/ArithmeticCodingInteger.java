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
package com.revolsys.math.arithmeticcoding;

import static java.lang.Integer.compareUnsigned;

public class ArithmeticCodingInteger {

  private int k;

  private final int contexts;

  private final int bits_high;

  private int corr_bits;

  private int corr_range;

  private int corr_min;

  private ArithmeticDecoder decoder;

  private ArithmeticEncoder encoder;

  private final ArithmeticModel[] mBits;

  private final ArithmeticBitModel mCorrector0;

  private final ArithmeticModel[] mCorrector;

  private int corr_max;

  public ArithmeticCodingInteger(final ArithmeticCodingCodec codec, final int bits) {
    this(codec, bits, 1, 8, 0);
  }

  public ArithmeticCodingInteger(final ArithmeticCodingCodec codec, final int bits,
    final int contexts) {
    this(codec, bits, contexts, 8, 0);
  }

  private ArithmeticCodingInteger(final ArithmeticCodingCodec codec, final int bits,
    final int contexts, final int bits_high, int range) {
    if (codec instanceof ArithmeticDecoder) {
      this.decoder = (ArithmeticDecoder)codec;
    } else if (codec instanceof ArithmeticEncoder) {
      this.encoder = (ArithmeticEncoder)codec;
    } else {
      throw new IllegalArgumentException(codec.getClass().getName());
    }
    this.contexts = contexts;
    this.bits_high = bits_high;

    if (range != 0) {
      this.corr_bits = 0;
      this.corr_range = range;
      while (range != 0) {
        range = range >>> 1;
        this.corr_bits++;
      }
      if (this.corr_range == 1 << this.corr_bits - 1) {
        this.corr_bits--;
      }
      this.corr_min = -Integer.divideUnsigned(this.corr_range, 2);
      this.corr_max = this.corr_min + this.corr_range - 1;
    } else if (bits != 0 && compareUnsigned(bits, 32) < 0) {
      this.corr_bits = bits;
      this.corr_range = 1 << bits;
      this.corr_min = -Integer.divideUnsigned(this.corr_range, 2);
      this.corr_max = this.corr_min + this.corr_range - 1;
    } else {
      this.corr_bits = 32;
      this.corr_range = 0;
      this.corr_min = Integer.MIN_VALUE;
      this.corr_max = Integer.MAX_VALUE;
    }

    this.k = 0;

    this.mBits = new ArithmeticModel[this.contexts];
    for (int i = 0; i < this.contexts; i++) {
      this.mBits[i] = codec.createSymbolModel(this.corr_bits + 1);
    }
    this.mCorrector0 = new ArithmeticBitModel();
    this.mCorrector = new ArithmeticModel[this.corr_bits + 1];
    for (int i = 1; i <= this.corr_bits; i++) {
      if (i <= this.bits_high) {
        this.mCorrector[i] = codec.createSymbolModel(1 << i);
      } else {
        this.mCorrector[i] = codec.createSymbolModel(1 << this.bits_high);
      }
    }

  }

  public void compress(final int pred, final int real) {
    compress(pred, real, 0);
  }

  public void compress(final int pred, final int real, final int context) {
    int corr = real - pred;
    if (corr < this.corr_min) {
      corr += this.corr_range;
    } else if (corr > this.corr_max) {
      corr -= this.corr_range;
    }
    writeCorrector(corr, this.mBits[context]);
  }

  public int decompress(final int pred) {
    return decompress(pred, 0);
  }

  public int decompress(final int pred, final int context) {
    assert this.decoder != null;
    int real = pred + readCorrector(this.mBits[context]);
    if (real < 0) {
      real += this.corr_range;
    } else if (compareUnsigned(real, this.corr_range) >= 0) {
      real -= this.corr_range;
    }
    return real;
  }

  public int getK() {
    return this.k;
  }

  public void init() {
    for (final ArithmeticModel model : this.mBits) {
      model.init();
    }
    this.mCorrector0.reset();
    for (int i = 1; i <= this.corr_bits; i++) {
      final ArithmeticModel model = this.mCorrector[i];
      model.init();
    }
  }

  public void initCompressor() {
    init();
  }

  public void initDecompressor() {
    init();
  }

  private int readCorrector(final ArithmeticModel mBits) {
    final ArithmeticDecoder decoder = this.decoder;

    final int k = decoder.decodeSymbol(mBits);
    this.k = k;

    int c;
    if (k != 0) {
      if (compareUnsigned(k, 32) < 0) {
        if (compareUnsigned(k, this.bits_high) <= 0) {
          c = decoder.decodeSymbol(this.mCorrector[k]);
        } else {
          final int k1 = k - this.bits_high;
          c = decoder.decodeSymbol(this.mCorrector[k]);
          final int c1 = decoder.readBits(k1);
          c = c << k1 | c1;
        }
        if (c >= 1 << k - 1) {
          c += 1;
        } else {
          c -= (1 << k) - 1;
        }
      } else {
        c = this.corr_min;
      }
    } else {
      c = decoder.decodeBit(this.mCorrector0);
    }

    return c;
  }

  void writeCorrector(int c, final ArithmeticModel mBits) {
    final ArithmeticEncoder encoder = this.encoder;
    final int bitsHigh = this.bits_high;

    // find the tighest interval [ - (2^k - 1) ... + (2^k) ] that contains c

    int k = 0;

    // do this by checking the absolute value of c (adjusted for the case that c
    // is 2^k)

    int c1 = c <= 0 ? -c : c - 1;

    // this loop could be replaced with more efficient code

    while (c1 != 0) {
      c1 = c1 >>> 1;
      k++;
    }
    this.k = k;

    // the number k is between 0 and corr_bits and describes the interval the
    // corrector falls into
    // we can compress the exact location of c within this interval using k bits

    encoder.encodeSymbol(mBits, k);

    if (k != 0) {// then c is either smaller than 0 or bigger than 1
      if (k < 32) {
        // translate the corrector c into the k-bit interval [ 0 ... 2^k - 1 ]
        if (c < 0) // then c is in the interval [ - (2^k - 1) ... - (2^(k-1))
                   // ]
        {
          // so we translate c into the interval [ 0 ... + 2^(k-1) - 1 ] by
          // adding (2^k - 1)
          c += (1 << k) - 1;
        } else // then c is in the interval [ 2^(k-1) + 1 ... 2^k ]
        {
          // so we translate c into the interval [ 2^(k-1) ... + 2^k - 1 ] by
          // subtracting 1
          c -= 1;
        }
        if (k <= bitsHigh) // for small k we code the interval in
        // one step
        {
          // compress c with the range coder
          encoder.encodeSymbol(this.mCorrector[k], c);
        } else // for larger k we need to code the interval in two steps
        {
          // figure out how many lower bits there are
          final int k1 = k - bitsHigh;
          // c1 represents the lowest k-bits_high+1 bits
          c1 = c & (1 << k1) - 1;
          // c represents the highest bits_high bits
          c = c >> k1;
          // compress the higher bits using a context table
          encoder.encodeSymbol(this.mCorrector[k], c);
          // store the lower k1 bits raw
          encoder.writeBits(k1, c1);
        }
      }
    } else {// then c is 0 or 1
      encoder.encodeBit(this.mCorrector0, c);
    }
  }
}
