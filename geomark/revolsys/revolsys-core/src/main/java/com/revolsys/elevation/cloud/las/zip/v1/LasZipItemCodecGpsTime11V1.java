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

public class LasZipItemCodecGpsTime11V1 implements LasZipItemCodec {

  private static final int LASZIP_GPSTIME_MULTIMAX = 512;

  private ArithmeticDecoder decoder;

  private ArithmeticEncoder encoder;

  private final ArithmeticCodingInteger ic_gpstime;

  private long last_gpstime;

  private int last_gpstime_diff;

  private final ArithmeticModel m_gpstime_0diff;

  private final ArithmeticModel m_gpstime_multi;

  private int multi_extreme_counter;

  public LasZipItemCodecGpsTime11V1(final ArithmeticCodingCodec codec, final int size) {
    if (codec instanceof ArithmeticDecoder) {
      this.decoder = (ArithmeticDecoder)codec;
    } else if (codec instanceof ArithmeticEncoder) {
      this.encoder = (ArithmeticEncoder)codec;
    } else {
      throw new IllegalArgumentException("Not supported:" + codec.getClass());
    }
    this.m_gpstime_multi = codec.createSymbolModel(LASZIP_GPSTIME_MULTIMAX);
    this.m_gpstime_0diff = codec.createSymbolModel(3);
    this.ic_gpstime = codec.newCodecInteger(32, 6);
  }

  @Override
  public int init(final LasPoint point, final int context) {
    this.last_gpstime_diff = 0;
    this.multi_extreme_counter = 0;

    this.m_gpstime_multi.init();
    this.m_gpstime_0diff.init();
    this.ic_gpstime.init();

    this.last_gpstime = point.getGpsTimeLong();
    return context;
  }

  @Override
  public int read(final LasPoint point, final int context) {
    int multi;
    if (this.last_gpstime_diff == 0) {
      // if the last integer difference was zero
      multi = this.decoder.decodeSymbol(this.m_gpstime_0diff);
      if (multi == 1) {
        // the difference can be represented with 32 bits
        this.last_gpstime_diff = this.ic_gpstime.decompress(0, 0);
        this.last_gpstime += this.last_gpstime_diff;
      } else if (multi == 2) {
        // the difference is huge
        this.last_gpstime = this.decoder.readInt64();
      }
    } else {
      multi = this.decoder.decodeSymbol(this.m_gpstime_multi);

      if (multi < LASZIP_GPSTIME_MULTIMAX - 2) {
        int gpstime_diff;
        if (multi == 1) {
          gpstime_diff = this.ic_gpstime.decompress(this.last_gpstime_diff, 1);
          this.last_gpstime_diff = gpstime_diff;
          this.multi_extreme_counter = 0;
        } else if (multi == 0) {
          gpstime_diff = this.ic_gpstime.decompress(this.last_gpstime_diff / 4, 2);
          this.multi_extreme_counter++;
          if (this.multi_extreme_counter > 3) {
            this.last_gpstime_diff = gpstime_diff;
            this.multi_extreme_counter = 0;
          }
        } else if (multi < 10) {
          gpstime_diff = this.ic_gpstime.decompress(multi * this.last_gpstime_diff, 3);
        } else if (multi < 50) {
          gpstime_diff = this.ic_gpstime.decompress(multi * this.last_gpstime_diff, 4);
        } else {
          gpstime_diff = this.ic_gpstime.decompress(multi * this.last_gpstime_diff, 5);
          if (multi == LASZIP_GPSTIME_MULTIMAX - 3) {
            this.multi_extreme_counter++;
            if (this.multi_extreme_counter > 3) {
              this.last_gpstime_diff = gpstime_diff;
              this.multi_extreme_counter = 0;
            }
          }
        }
        this.last_gpstime += gpstime_diff;
      } else if (multi < LASZIP_GPSTIME_MULTIMAX - 1) {
        this.last_gpstime = this.decoder.readInt64();
      }
    }
    point.setGpsTimeLong(this.last_gpstime);
    return context;
  }

  @Override
  public int write(final LasPoint point, final int context) {
    final long this_gpstime = point.getGpsTimeLong();

    if (this.last_gpstime_diff == 0) {
      // if the last integer difference was zero
      if (this_gpstime == this.last_gpstime) {
        // the doubles have not changed
        this.encoder.encodeSymbol(this.m_gpstime_0diff, 0);
      } else {
        // calculate the difference between the two doubles as an integer
        final long curr_gpstime_diff_64 = this_gpstime - this.last_gpstime;
        final int curr_gpstime_diff = (int)curr_gpstime_diff_64;
        if (curr_gpstime_diff_64 == curr_gpstime_diff) {
          // the difference can be represented with 32 bits
          this.encoder.encodeSymbol(this.m_gpstime_0diff, 1);
          this.ic_gpstime.compress(0, curr_gpstime_diff, 0);
          this.last_gpstime_diff = curr_gpstime_diff;
        } else {
          // the difference is huge
          this.encoder.encodeSymbol(this.m_gpstime_0diff, 2);
          this.encoder.writeInt64(this_gpstime);
        }
        this.last_gpstime = this_gpstime;
      }
    } else {
      // the last integer difference was *not* zero
      if (this_gpstime == this.last_gpstime) {
        // if the doubles have not changed use a special symbol
        this.encoder.encodeSymbol(this.m_gpstime_multi, LASZIP_GPSTIME_MULTIMAX - 1);
      } else {
        // calculate the difference between the two doubles as an integer
        final long curr_gpstime_diff_64 = this_gpstime - this.last_gpstime;
        final int curr_gpstime_diff = (int)curr_gpstime_diff_64;
        // if the current gpstime difference can be represented with 32 bits
        if (curr_gpstime_diff_64 == curr_gpstime_diff) {
          // compute multiplier between current and last integer difference
          int multi = (int)((float)curr_gpstime_diff / (float)this.last_gpstime_diff + 0.5f);

          // limit the multiplier into some bounds
          if (multi >= LASZIP_GPSTIME_MULTIMAX - 3) {
            multi = LASZIP_GPSTIME_MULTIMAX - 3;
          } else if (multi <= 0) {
            multi = 0;
          }
          // compress this multiplier
          this.encoder.encodeSymbol(this.m_gpstime_multi, multi);
          // compress the residual curr_gpstime_diff in dependance on the
          // multiplier
          if (multi == 1) {
            // this is the case we assume we get most often
            this.ic_gpstime.compress(this.last_gpstime_diff, curr_gpstime_diff, 1);
            this.last_gpstime_diff = curr_gpstime_diff;
            this.multi_extreme_counter = 0;
          } else {
            if (multi == 0) {
              this.ic_gpstime.compress(this.last_gpstime_diff / 4, curr_gpstime_diff, 2);
              this.multi_extreme_counter++;
              if (this.multi_extreme_counter > 3) {
                this.last_gpstime_diff = curr_gpstime_diff;
                this.multi_extreme_counter = 0;
              }
            } else if (multi < 10) {
              this.ic_gpstime.compress(multi * this.last_gpstime_diff, curr_gpstime_diff, 3);
            } else if (multi < 50) {
              this.ic_gpstime.compress(multi * this.last_gpstime_diff, curr_gpstime_diff, 4);
            } else {
              this.ic_gpstime.compress(multi * this.last_gpstime_diff, curr_gpstime_diff, 5);
              if (multi == LASZIP_GPSTIME_MULTIMAX - 3) {
                this.multi_extreme_counter++;
                if (this.multi_extreme_counter > 3) {
                  this.last_gpstime_diff = curr_gpstime_diff;
                  this.multi_extreme_counter = 0;
                }
              }
            }
          }
        } else {
          // if difference is so huge ... we simply write the double
          this.encoder.encodeSymbol(this.m_gpstime_multi, LASZIP_GPSTIME_MULTIMAX - 2);
          this.encoder.writeInt64(this_gpstime); // u64??
        }
        this.last_gpstime = this_gpstime;
      }
    }
    return context;
  }
}
