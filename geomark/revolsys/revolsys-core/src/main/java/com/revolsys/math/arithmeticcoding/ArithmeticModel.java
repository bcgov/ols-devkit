/*
 * Copyright 2005-2014, martin isenburg, rapidlasso - fast tools to catch reality
 *
 * This is free software; you can redistribute and/or modify it under the
 * terms of the GNU Lesser General Licence as published by the Free Software
 * Foundation. See the LICENSE.txt file for more information.
 *
 * This software is distributed WITHOUT ANY WARRANTY and without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.revolsys.math.arithmeticcoding;

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//                                                                           -
// Fast arithmetic coding implementation                                     -
// -> 32-bit variables, 32-bit product, periodic updates, table decoding     -
//                                                                           -
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//                                                                           -
// Version 1.00  -  April 25, 2004                                           -
//                                                                           -
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//                                                                           -
//                                  WARNING                                  -
//                                 =========                                 -
//                                                                           -
// The only purpose of this program is to demonstrate the basic principles   -
// of arithmetic coding. It is provided as is, without any express or        -
// implied warranty, without even the warranty of fitness for any particular -
// purpose, or that the implementations are correct.                         -
//                                                                           -
// Permission to copy and redistribute this code is hereby granted, provided -
// that this warning and copyright notices are not removed or altered.       -
//                                                                           -
// Copyright (c) 2004 by Amir Said (said@ieee.org) &                         -
//                       William A. Pearlman (pearlw@ecse.rpi.edu)           -
//                                                                           -
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//                                                                           -
// A description of the arithmetic coding method used here is available in   -
//                                                                           -
// Lossless Compression Handbook, ed. K. Sayood                              -
// Chapter 5: Arithmetic Coding (A. Said), pp. 101-152, Academic Press, 2003 -
//                                                                           -
// A. Said, Introduction to Arithetic Coding Theory and Practice             -
// HP Labs report HPL-2004-76  -  http://www.hpl.hp.com/techreports/         -
//                                                                           -
// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

public class ArithmeticModel {
  static final int AC_BUFFER_SIZE = 1024;

  static final int AC__MinLength = 0x01000000; // threshold for renormalization

  static final int AC__MaxLength = 0xFFFFFFFF; // maximum AC interval length

  static final int BM__LengthShift = 13; // length bits discarded before mult.

  static final int BM__MaxCount = 1 << BM__LengthShift; // for adaptive models

  static final int DM__LengthShift = 15; // length bits discarded before mult.

  static final int DM__MaxCount = 1 << DM__LengthShift; // for adaptive models

  public static void initModel(final ArithmeticModel model) {
    if (model != null) {
      model.init();
    }
  }

  public static void initModels(final ArithmeticModel[] models) {
    for (final ArithmeticModel model : models) {
      if (model != null) {
        model.init();
      }
    }
  }

  public int[] decoderTable;

  public int[] distribution;

  public int[] symbolCounts;

  private int totalCount;

  private int updateCycle;

  public int symbolsUntilUpdate;

  public final int symbolCount;

  public int lastSymbol;

  private int tableSize;

  public int tableShift;

  private final boolean compress;

  public ArithmeticModel(final int symbolCount, final boolean compress) {
    if (symbolCount < 2 || symbolCount > 1 << 11) {
      throw new IllegalArgumentException("sumbolCount=" + symbolCount);
    }
    this.symbolCount = symbolCount;
    this.compress = compress;
    this.lastSymbol = symbolCount - 1;
    if (symbolCount > 16) {
      int table_bits = 3;
      while (symbolCount > 1 << table_bits + 2) {
        ++table_bits;
      }
      this.tableSize = 1 << table_bits;
      this.tableShift = DM__LengthShift - table_bits;
      this.distribution = new int[symbolCount];
      this.decoderTable = new int[this.tableSize + 2];
    } else {
      this.decoderTable = null;
      this.tableSize = this.tableShift = 0;
      this.distribution = new int[symbolCount];
    }
    this.symbolCounts = new int[symbolCount];
    init();
  }

  public void init() {
    this.totalCount = 0;
    this.updateCycle = this.symbolCount;
    for (int k = 0; k < this.symbolCount; k++) {
      this.symbolCounts[k] = 1;
    }

    update();
    this.updateCycle = this.symbolCount + 6 >>> 1;
    this.symbolsUntilUpdate = this.updateCycle;
  }

  public void update() {
    if ((this.totalCount += this.updateCycle) > DM__MaxCount) {
      this.totalCount = 0;
      for (int n = 0; n < this.symbolCount; n++) {
        this.totalCount += this.symbolCounts[n] = this.symbolCounts[n] + 1 >>> 1;
      }
    }

    int sum = 0;
    int s = 0;
    final int scale = Integer.divideUnsigned(0x80000000, this.totalCount);

    if (this.compress || this.tableSize == 0) {
      for (int k = 0; k < this.symbolCount; k++) {
        this.distribution[k] = scale * sum >>> 31 - DM__LengthShift;
        sum += this.symbolCounts[k];
      }
    } else {
      for (int k = 0; k < this.symbolCount; k++) {
        this.distribution[k] = scale * sum >>> 31 - DM__LengthShift;
        sum += this.symbolCounts[k];
        final int w = this.distribution[k] >>> this.tableShift;
        while (s < w) {
          this.decoderTable[++s] = k - 1;
        }
      }
      this.decoderTable[0] = 0;
      while (s <= this.tableSize) {
        this.decoderTable[++s] = this.symbolCount - 1;
      }
    }

    this.updateCycle = 5 * this.updateCycle >>> 2;
    final int max_cycle = this.symbolCount + 6 << 3;
    if (Integer.compareUnsigned(this.updateCycle, max_cycle) > 0) {
      this.updateCycle = max_cycle;
    }
    this.symbolsUntilUpdate = this.updateCycle;
  }
}
