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
import com.revolsys.math.arithmeticcoding.ArithmeticCodingCodec;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingInteger;
import com.revolsys.math.arithmeticcoding.ArithmeticDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticEncoder;
import com.revolsys.math.arithmeticcoding.ArithmeticModel;

public class LasZipItemCodecGpsTime11V2 implements LasZipItemCodec {

  private static int LASZIP_GPSTIME_MULTI = 500;

  private static int LASZIP_GPSTIME_MULTI_MINUS = -10;

  private static int LASZIP_GPSTIME_MULTI_UNCHANGED = LASZIP_GPSTIME_MULTI
    - LASZIP_GPSTIME_MULTI_MINUS + 1;

  private static int LASZIP_GPSTIME_MULTI_CODE_FULL = LASZIP_GPSTIME_MULTI
    - LASZIP_GPSTIME_MULTI_MINUS + 2;

  private static int LASZIP_GPSTIME_MULTI_TOTAL = LASZIP_GPSTIME_MULTI - LASZIP_GPSTIME_MULTI_MINUS
    + 6;

  private int last;

  private int next;

  private final long[] lastGpsTime = new long[4];

  private final int[] lastGpsTimeDiff = new int[4];

  private final int[] multiExtremeCounter = new int[4];

  private ArithmeticDecoder decoder;

  private ArithmeticEncoder encoder;

  private final ArithmeticModel gpsTimeMulti;

  private final ArithmeticModel gpsTime0Diff;

  private final ArithmeticCodingInteger ic_gpstime;

  public LasZipItemCodecGpsTime11V2(final ArithmeticCodingCodec codec, final int size) {
    if (codec instanceof ArithmeticDecoder) {
      this.decoder = (ArithmeticDecoder)codec;
    } else if (codec instanceof ArithmeticEncoder) {
      this.encoder = (ArithmeticEncoder)codec;
    } else {
      throw new IllegalArgumentException("Not supported:" + codec.getClass());
    }
    this.gpsTimeMulti = codec.createSymbolModel(LASZIP_GPSTIME_MULTI_TOTAL);
    this.gpsTime0Diff = codec.createSymbolModel(6);
    this.ic_gpstime = codec.newCodecInteger(32, 9);
  }

  @Override
  public int getVersion() {
    return 2;
  }

  @Override
  public int init(final LasPoint point, final int context) {
    this.gpsTimeMulti.init();
    this.gpsTime0Diff.init();
    this.ic_gpstime.init();

    this.last = 0;
    this.next = 0;
    this.lastGpsTimeDiff[0] = 0;
    this.lastGpsTimeDiff[1] = 0;
    this.lastGpsTimeDiff[2] = 0;
    this.lastGpsTimeDiff[3] = 0;
    this.multiExtremeCounter[0] = 0;
    this.multiExtremeCounter[1] = 0;
    this.multiExtremeCounter[2] = 0;
    this.multiExtremeCounter[3] = 0;

    this.lastGpsTime[0] = Double.doubleToLongBits(point.getGpsTime());
    this.lastGpsTime[1] = 0;
    this.lastGpsTime[2] = 0;
    this.lastGpsTime[3] = 0;
    return context;
  }

  @Override
  public int read(final LasPoint point, final int context) {
    int multi;
    final int[] lastGpsTimeDiff = this.lastGpsTimeDiff;
    final int lastDiff = lastGpsTimeDiff[this.last];
    final ArithmeticDecoder decoder = this.decoder;
    final long[] lastGpsTime = this.lastGpsTime;
    final int[] multiExtremeCounter = this.multiExtremeCounter;
    int last = this.last;
    int next = this.next;
    final ArithmeticCodingInteger decompressGpsTime = this.ic_gpstime;
    if (lastDiff == 0) { // if the last integer difference was zero
      multi = decoder.decodeSymbol(this.gpsTime0Diff);
      if (multi == 1) {// the difference can be represented with 32 bits
        final int gpsTimeDiff = decompressGpsTime.decompress(0, 0);
        lastGpsTimeDiff[last] = gpsTimeDiff;
        lastGpsTime[last] += gpsTimeDiff;
        multiExtremeCounter[last] = 0;
      } else if (multi == 2) {// the difference is huge
        long gpsTime = decompressGpsTime.decompress((int)(lastGpsTime[last] >>> 32), 8);
        gpsTime <<= 32;
        gpsTime |= Integer.toUnsignedLong(decoder.readInt());

        next = this.next + 1;
        next &= 3;
        this.next = next;
        lastGpsTime[next] = gpsTime;

        last = next;
        this.last = next;
        lastGpsTimeDiff[last] = 0;
        multiExtremeCounter[last] = 0;
      } else if (multi > 2) {// we switch to another sequence
        last = last + multi - 2;
        this.last = last & 3;
        read(point, context);
      }
    } else {
      multi = decoder.decodeSymbol(this.gpsTimeMulti);
      if (multi == 1) {
        final int gpsTimeDiff = lastGpsTimeDiff[last];
        lastGpsTime[last] += decompressGpsTime.decompress(gpsTimeDiff, 1);
        multiExtremeCounter[last] = 0;
      } else if (multi < LASZIP_GPSTIME_MULTI_UNCHANGED) {
        int gpsTimeDiff;
        if (multi == 0) {
          gpsTimeDiff = decompressGpsTime.decompress(0, 7);
          multiExtremeCounter[last]++;
          if (multiExtremeCounter[last] > 3) {
            lastGpsTimeDiff[last] = gpsTimeDiff;
            multiExtremeCounter[last] = 0;
          }
        } else if (multi < LASZIP_GPSTIME_MULTI) {
          gpsTimeDiff = lastGpsTimeDiff[last];
          if (multi < 10) {
            gpsTimeDiff = decompressGpsTime.decompress(multi * gpsTimeDiff, 2);
          } else {
            gpsTimeDiff = decompressGpsTime.decompress(multi * gpsTimeDiff, 3);
          }
        } else if (multi == LASZIP_GPSTIME_MULTI) {
          gpsTimeDiff = lastGpsTimeDiff[last];
          gpsTimeDiff = decompressGpsTime.decompress(LASZIP_GPSTIME_MULTI * gpsTimeDiff, 4);
          multiExtremeCounter[last]++;
          if (multiExtremeCounter[last] > 3) {
            lastGpsTimeDiff[last] = gpsTimeDiff;
            multiExtremeCounter[last] = 0;
          }
        } else {
          gpsTimeDiff = lastGpsTimeDiff[last];
          multi = LASZIP_GPSTIME_MULTI - multi;
          if (multi > LASZIP_GPSTIME_MULTI_MINUS) {
            gpsTimeDiff = decompressGpsTime.decompress(multi * gpsTimeDiff, 5);
          } else {
            gpsTimeDiff = decompressGpsTime.decompress(LASZIP_GPSTIME_MULTI_MINUS * gpsTimeDiff, 6);
            multiExtremeCounter[last]++;
            if (multiExtremeCounter[last] > 3) {
              lastGpsTimeDiff[last] = gpsTimeDiff;
              multiExtremeCounter[last] = 0;
            }
          }
        }
        lastGpsTime[last] += gpsTimeDiff;
      } else if (multi == LASZIP_GPSTIME_MULTI_CODE_FULL) {
        next += 1;
        next &= 3;
        this.next = next;
        long gpsTime = lastGpsTime[last];
        gpsTime >>= 32;
        gpsTime = decompressGpsTime.decompress((int)gpsTime, 8);
        gpsTime <<= 32;
        gpsTime |= Integer.toUnsignedLong(decoder.readInt());
        lastGpsTime[next] = gpsTime;

        last = next;
        this.last = last;
        lastGpsTimeDiff[last] = 0;
        multiExtremeCounter[last] = 0;
      } else if (multi >= LASZIP_GPSTIME_MULTI_CODE_FULL) {
        last += multi - LASZIP_GPSTIME_MULTI_CODE_FULL;
        this.last = last & 3;
        return read(point, context);
      }
    }
    point.setGpsTimeLong(lastGpsTime[this.last]);
    return context;
  }

  @Override
  public int write(final LasPoint item, final int context) {
    final long gpsTime = item.getGpsTimeLong();
    writeGpsTime(gpsTime);
    return context;
  }

  private void writeGpsTime(final long gpsTime) {
    if (this.lastGpsTimeDiff[this.last] == 0) {
      // if the last integer difference was zero
      if (gpsTime == this.lastGpsTime[this.last]) {
        this.encoder.encodeSymbol(this.gpsTime0Diff, 0); // the doubles have not
        // changed
      } else {
        // calculate the difference between the two doubles as an integer
        final long curr_gpstime_diff_64 = gpsTime - this.lastGpsTime[this.last];
        final int curr_gpstime_diff = (int)curr_gpstime_diff_64;
        if (curr_gpstime_diff_64 == curr_gpstime_diff) {
          this.encoder.encodeSymbol(this.gpsTime0Diff, 1); // the difference can
          // be represented with
          // 32 bits
          this.ic_gpstime.compress(0, curr_gpstime_diff, 0);
          this.lastGpsTimeDiff[this.last] = curr_gpstime_diff;
          this.multiExtremeCounter[this.last] = 0;
        } else // the difference is huge
        {
          int i;
          // maybe the double belongs to another time sequence
          for (i = 1; i < 4; i++) {
            final long other_gpstime_diff_64 = gpsTime - this.lastGpsTime[this.last + i & 3];
            final int other_gpstime_diff = (int)other_gpstime_diff_64;
            if (other_gpstime_diff_64 == other_gpstime_diff) {
              this.encoder.encodeSymbol(this.gpsTime0Diff, i + 2); // it belongs
              // to another
              // sequence
              this.last = this.last + i & 3;
              writeGpsTime(gpsTime);
              return;
            }
          }
          // no other sequence found. start new sequence.
          this.encoder.encodeSymbol(this.gpsTime0Diff, 2);
          this.ic_gpstime.compress((int)(this.lastGpsTime[this.last] >>> 32), (int)(gpsTime >> 32),
            8);
          this.encoder.writeInt((int)gpsTime);
          this.next = this.next + 1 & 3;
          this.last = this.next;
          this.lastGpsTimeDiff[this.last] = 0;
          this.multiExtremeCounter[this.last] = 0;
        }
        this.lastGpsTime[this.last] = gpsTime;
      }
    } else // the last integer difference was *not* zero
    {
      if (gpsTime == this.lastGpsTime[this.last]) {
        // if the doubles have not changed use a special symbol
        this.encoder.encodeSymbol(this.gpsTimeMulti, LASZIP_GPSTIME_MULTI_UNCHANGED);
      } else {
        // calculate the difference between the two doubles as an integer
        final long curr_gpstime_diff_64 = gpsTime - this.lastGpsTime[this.last];
        final int curr_gpstime_diff = (int)curr_gpstime_diff_64;

        // if the current gpstime difference can be represented with 32 bits
        if (curr_gpstime_diff_64 == curr_gpstime_diff) {
          // compute multiplier between current and last integer difference
          final float multi_f = (float)curr_gpstime_diff / (float)this.lastGpsTimeDiff[this.last];
          final int multi = I32_QUANTIZE(multi_f);

          // compress the residual curr_gpstime_diff in dependance on the
          // multiplier
          if (multi == 1) {
            // this is the case we assume we get most often for regular spaced
            // pulses
            this.encoder.encodeSymbol(this.gpsTimeMulti, 1);
            this.ic_gpstime.compress(this.lastGpsTimeDiff[this.last], curr_gpstime_diff, 1);
            this.multiExtremeCounter[this.last] = 0;
          } else if (multi > 0) {
            if (multi < LASZIP_GPSTIME_MULTI) // positive multipliers up to
                                              // LASZIP_GPSTIME_MULTI are
                                              // compressed directly
            {
              this.encoder.encodeSymbol(this.gpsTimeMulti, multi);
              if (multi < 10) {
                this.ic_gpstime.compress(multi * this.lastGpsTimeDiff[this.last], curr_gpstime_diff,
                  2);
              } else {
                this.ic_gpstime.compress(multi * this.lastGpsTimeDiff[this.last], curr_gpstime_diff,
                  3);
              }
            } else {
              this.encoder.encodeSymbol(this.gpsTimeMulti, LASZIP_GPSTIME_MULTI);
              this.ic_gpstime.compress(LASZIP_GPSTIME_MULTI * this.lastGpsTimeDiff[this.last],
                curr_gpstime_diff, 4);
              this.multiExtremeCounter[this.last]++;
              if (this.multiExtremeCounter[this.last] > 3) {
                this.lastGpsTimeDiff[this.last] = curr_gpstime_diff;
                this.multiExtremeCounter[this.last] = 0;
              }
            }
          } else if (multi < 0) {
            if (multi > LASZIP_GPSTIME_MULTI_MINUS) // negative multipliers
                                                    // larger than
                                                    // LASZIP_GPSTIME_MULTI_MINUS
                                                    // are compressed directly
            {
              this.encoder.encodeSymbol(this.gpsTimeMulti, LASZIP_GPSTIME_MULTI - multi);
              this.ic_gpstime.compress(multi * this.lastGpsTimeDiff[this.last], curr_gpstime_diff,
                5);
            } else {
              this.encoder.encodeSymbol(this.gpsTimeMulti,
                LASZIP_GPSTIME_MULTI - LASZIP_GPSTIME_MULTI_MINUS);
              this.ic_gpstime.compress(LASZIP_GPSTIME_MULTI_MINUS * this.lastGpsTimeDiff[this.last],
                curr_gpstime_diff, 6);
              this.multiExtremeCounter[this.last]++;
              if (this.multiExtremeCounter[this.last] > 3) {
                this.lastGpsTimeDiff[this.last] = curr_gpstime_diff;
                this.multiExtremeCounter[this.last] = 0;
              }
            }
          } else {
            this.encoder.encodeSymbol(this.gpsTimeMulti, 0);
            this.ic_gpstime.compress(0, curr_gpstime_diff, 7);
            this.multiExtremeCounter[this.last]++;
            if (this.multiExtremeCounter[this.last] > 3) {
              this.lastGpsTimeDiff[this.last] = curr_gpstime_diff;
              this.multiExtremeCounter[this.last] = 0;
            }
          }
        } else // the difference is huge
        {
          // maybe the double belongs to another time sequence
          for (int i = 1; i < 4; i++) {
            final long other_gpstime_diff_64 = gpsTime - this.lastGpsTime[this.last + i & 3];
            final int other_gpstime_diff = (int)other_gpstime_diff_64;
            if (other_gpstime_diff_64 == other_gpstime_diff) {
              // it belongs to this sequence
              this.encoder.encodeSymbol(this.gpsTimeMulti, LASZIP_GPSTIME_MULTI_CODE_FULL + i);
              this.last = this.last + i & 3;
              writeGpsTime(gpsTime);
              return;
            }
          }
          // no other sequence found. start new sequence.
          this.encoder.encodeSymbol(this.gpsTimeMulti, LASZIP_GPSTIME_MULTI_CODE_FULL);
          this.ic_gpstime.compress((int)(this.lastGpsTime[this.last] >>> 32), (int)(gpsTime >> 32),
            8);
          this.encoder.writeInt((int)gpsTime);
          this.next = this.next + 1 & 3;
          this.last = this.next;
          this.lastGpsTimeDiff[this.last] = 0;
          this.multiExtremeCounter[this.last] = 0;
        }
        this.lastGpsTime[this.last] = gpsTime;
      }
    }
  }

}
