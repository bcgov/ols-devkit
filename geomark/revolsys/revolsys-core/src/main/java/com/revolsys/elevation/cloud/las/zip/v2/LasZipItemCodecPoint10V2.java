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

import static com.revolsys.elevation.cloud.las.zip.StreamingMedian5.newStreamingMedian5;

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.elevation.cloud.las.zip.LasZipItemCodec;
import com.revolsys.elevation.cloud.las.zip.StreamingMedian5;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingCodec;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingInteger;
import com.revolsys.math.arithmeticcoding.ArithmeticDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticEncoder;
import com.revolsys.math.arithmeticcoding.ArithmeticModel;

public class LasZipItemCodecPoint10V2 implements LasZipItemCodec {
  private static final byte[][] number_return_level = {
    {
      0, 1, 2, 3, 4, 5, 6, 7
    }, {
      1, 0, 1, 2, 3, 4, 5, 6
    }, {
      2, 1, 0, 1, 2, 3, 4, 5
    }, {
      3, 2, 1, 0, 1, 2, 3, 4
    }, {
      4, 3, 2, 1, 0, 1, 2, 3
    }, {
      5, 4, 3, 2, 1, 0, 1, 2
    }, {
      6, 5, 4, 3, 2, 1, 0, 1
    }, {
      7, 6, 5, 4, 3, 2, 1, 0
    }
  };

  private static final byte[][] number_return_map = {
    {
      15, 14, 13, 12, 11, 10, 9, 8
    }, {
      14, 0, 1, 3, 6, 10, 10, 9
    }, {
      13, 1, 2, 4, 7, 11, 11, 10
    }, {
      12, 3, 4, 5, 8, 12, 12, 11
    }, {
      11, 6, 7, 8, 9, 13, 13, 12
    }, {
      10, 10, 11, 12, 13, 14, 14, 13
    }, {
      9, 10, 11, 12, 13, 14, 15, 14
    }, {
      8, 9, 10, 11, 12, 13, 14, 15
    }
  };

  private ArithmeticDecoder decoder;

  private ArithmeticEncoder encoder;

  private final ArithmeticCodingInteger ic_z;

  private final ArithmeticCodingInteger ic_dx;

  private final ArithmeticCodingInteger ic_dy;

  private final ArithmeticCodingInteger ic_intensity;

  private final ArithmeticCodingInteger ic_point_source_ID;

  private final int[] last_height = new int[8]; // signed

  private final int[] last_intensity = new int[16];

  private final StreamingMedian5[] last_x_diff_median5 = newStreamingMedian5(16);

  private final StreamingMedian5[] last_y_diff_median5 = newStreamingMedian5(16);

  private final ArithmeticModel[] m_bit_byte = new ArithmeticModel[256];

  private final ArithmeticModel m_changed_values;

  private final ArithmeticModel[] m_classification = new ArithmeticModel[256];

  private final ArithmeticModel[] m_scan_angle_rank = new ArithmeticModel[2];

  private final ArithmeticModel[] m_user_data = new ArithmeticModel[256];

  private int lastClassificationField;

  private int lastIntensity;

  private int lastPointSourceID;

  private int lastReturnField;

  private int lastScanAngleRank;

  private int lastUserData;

  private int lastX;

  private int lastY;

  private int lastZ;

  public LasZipItemCodecPoint10V2(final ArithmeticCodingCodec codec, final int size) {
    if (codec instanceof ArithmeticDecoder) {
      this.decoder = (ArithmeticDecoder)codec;
    } else if (codec instanceof ArithmeticEncoder) {
      this.encoder = (ArithmeticEncoder)codec;
    } else {
      throw new IllegalArgumentException("Not supported:" + codec.getClass());
    }
    this.m_changed_values = codec.createSymbolModel(64);
    this.ic_intensity = codec.newCodecInteger(16, 4);
    this.m_scan_angle_rank[0] = codec.createSymbolModel(256);
    this.m_scan_angle_rank[1] = codec.createSymbolModel(256);
    this.ic_point_source_ID = codec.newCodecInteger(16);
    for (int i = 0; i < 256; i++) {
      this.m_bit_byte[i] = null;
      this.m_classification[i] = null;
      this.m_user_data[i] = null;
    }
    this.ic_dx = codec.newCodecInteger(32, 2);
    this.ic_dy = codec.newCodecInteger(32, 22);
    this.ic_z = codec.newCodecInteger(32, 20);
  }

  private int getLastNumberOfReturns() {
    return this.lastReturnField >>> 3 & 0x7;
  }

  private int getLastReturnNumber() {
    return this.lastReturnField & 0x7;
  }

  private int getLastScanDirectionFlag() {
    return this.lastReturnField >>> 6 & 0x1;
  }

  @Override
  public int getVersion() {
    return 2;
  }

  @Override
  public int init(final LasPoint point, final int context) {
    int i; // unsigned

    /* init state */
    for (i = 0; i < 16; i++) {
      this.last_x_diff_median5[i].init();
      this.last_y_diff_median5[i].init();
      this.last_intensity[i] = 0;
      this.last_height[i / 2] = 0;
    }

    /* init models and integer compressors */
    this.m_changed_values.init();
    this.ic_intensity.init();
    this.m_scan_angle_rank[0].init();
    this.m_scan_angle_rank[1].init();
    this.ic_point_source_ID.init();
    for (i = 0; i < 256; i++) {
      if (this.m_bit_byte[i] != null) {
        this.m_bit_byte[i].init();
      }
      if (this.m_classification[i] != null) {
        this.m_classification[i].init();
      }
      if (this.m_user_data[i] != null) {
        this.m_user_data[i].init();
      }
    }
    this.ic_dx.init();
    this.ic_dy.init();
    this.ic_z.init();

    this.lastX = point.getXInt();
    this.lastY = point.getYInt();
    this.lastZ = point.getZInt();

    /* but set intensity to zero */
    this.lastIntensity = 0;
    this.lastReturnField = Byte.toUnsignedInt(point.getReturnByte());
    this.lastClassificationField = Byte.toUnsignedInt(point.getClassificationByte());
    this.lastScanAngleRank = point.getScanAngleRank();
    this.lastUserData = point.getUserData();
    this.lastPointSourceID = point.getPointSourceID();
    return context;
  }

  @Override
  public int read(final LasPoint point, final int context) {
    int r, n, m, l; // unsigned
    int k_bits; // unsigned
    int median, diff; // signed

    // decompress which other values have changed
    final int changed_values = this.decoder.decodeSymbol(this.m_changed_values);

    if (changed_values != 0) {
      // decompress the edge_of_flight_line, scan_direction_flag, ... if it has
      // changed
      if ((changed_values & 32) != 0) {
        if (this.m_bit_byte[this.lastReturnField] == null) {
          this.m_bit_byte[this.lastReturnField] = this.decoder.createSymbolModel(256);
          this.m_bit_byte[this.lastReturnField].init();
        }
        this.lastReturnField = this.decoder.decodeSymbol(this.m_bit_byte[this.lastReturnField]);
      }

      r = getLastReturnNumber();
      n = getLastNumberOfReturns();
      m = number_return_map[n][r];
      l = number_return_level[n][r];

      // decompress the intensity if it has changed
      if ((changed_values & 16) != 0) {
        this.lastIntensity = this.ic_intensity.decompress(this.last_intensity[m], m < 3 ? m : 3);
        this.last_intensity[m] = this.lastIntensity;
      } else {
        this.lastIntensity = this.last_intensity[m];
      }

      // decompress the classification ... if it has changed
      if ((changed_values & 8) != 0) {
        if (this.m_classification[this.lastClassificationField] == null) {
          this.m_classification[this.lastClassificationField] = this.decoder.createSymbolModel(256);
          this.m_classification[this.lastClassificationField].init();
        }
        this.lastClassificationField = this.decoder
          .decodeSymbol(this.m_classification[this.lastClassificationField]);
      }

      // decompress the scan_angle_rank ... if it has changed
      if ((changed_values & 4) != 0) {
        final int val = this.decoder
          .decodeSymbol(this.m_scan_angle_rank[getLastScanDirectionFlag()]);
        this.lastScanAngleRank = U8_FOLD(val + this.lastScanAngleRank);
      }

      // decompress the user_data ... if it has changed
      if ((changed_values & 2) != 0) {
        if (this.m_user_data[this.lastUserData] == null) {
          this.m_user_data[this.lastUserData] = this.decoder.createSymbolModel(256);
          this.m_user_data[this.lastUserData].init();
        }
        this.lastUserData = this.decoder.decodeSymbol(this.m_user_data[this.lastUserData]);
      }

      // decompress the point_source_ID ... if it has changed
      if ((changed_values & 1) != 0) {
        this.lastPointSourceID = this.ic_point_source_ID.decompress(this.lastPointSourceID);
      }
    } else {
      r = getLastReturnNumber();
      n = getLastNumberOfReturns();
      m = number_return_map[n][r];
      l = number_return_level[n][r];
    }

    // decompress x coordinate
    median = this.last_x_diff_median5[m].get();
    diff = this.ic_dx.decompress(median, n == 1 ? 1 : 0);
    this.lastX += +diff;
    this.last_x_diff_median5[m].add(diff);

    // decompress y coordinate
    median = this.last_y_diff_median5[m].get();
    k_bits = this.ic_dx.getK();
    diff = this.ic_dy.decompress(median,
      (n == 1 ? 1 : 0) + (k_bits < 20 ? U32_ZERO_BIT_0(k_bits) : 20));
    this.lastY += diff;
    this.last_y_diff_median5[m].add(diff);

    // decompress z coordinate
    k_bits = (this.ic_dx.getK() + this.ic_dy.getK()) / 2;
    this.lastZ = this.ic_z.decompress(this.last_height[l],
      (n == 1 ? 1 : 0) + (k_bits < 18 ? U32_ZERO_BIT_0(k_bits) : 18));
    this.last_height[l] = this.lastZ;

    point.setXYZ(this.lastX, this.lastY, this.lastZ);
    point.setIntensity(this.lastIntensity);
    point.setReturnByte((byte)this.lastReturnField);

    point.setClassificationByte((byte)this.lastClassificationField);
    point.setScanAngleRank((byte)this.lastScanAngleRank);
    point.setUserData((short)this.lastUserData);
    point.setPointSourceID(this.lastPointSourceID);
    return context;
  }

  @Override
  public int write(final LasPoint point, final int context) {
    final int x = point.getXInt();
    final int y = point.getYInt();
    final int z = point.getZInt();
    final int intensity = point.getIntensity();
    final int returnByte = Byte.toUnsignedInt(point.getReturnByte());
    final int classificationByte = Byte.toUnsignedInt(point.getClassificationByte());
    final int scanAngleRank = Byte.toUnsignedInt(point.getScanAngleRank());
    final int userData = point.getUserData();
    final int pointSourceID = point.getPointSourceID();

    final int r = point.getReturnNumber();
    final int n = point.getNumberOfReturns();
    final int m = number_return_map[n][r];
    final int l = number_return_level[n][r];

    final boolean intensityChanged = this.last_intensity[m] != intensity;
    final boolean returnChanged = this.lastReturnField != returnByte;
    final boolean classificationChanged = this.lastClassificationField != classificationByte;
    final boolean scanAngleRankChanged = this.lastScanAngleRank != scanAngleRank;
    final boolean userDataChanged = this.lastUserData != userData;
    final boolean pointSourceIdChanged = this.lastPointSourceID != pointSourceID;

    int changed_values = 0;
    if (returnChanged) {
      changed_values |= 32;
    }
    if (intensityChanged) {
      changed_values |= 16;
    }
    if (classificationChanged) {
      changed_values |= 8;
    }
    if (scanAngleRankChanged) {
      changed_values |= 4;
    }
    if (userDataChanged) {
      changed_values |= 2;
    }
    if (pointSourceIdChanged) {
      changed_values |= 1;
    }

    this.encoder.encodeSymbol(this.m_changed_values, changed_values);

    // compress the bit_byte (edge_of_flight_line, scan_direction_flag, returns,
    // ...) if it has changed
    if (returnChanged) {
      if (this.m_bit_byte[this.lastReturnField] == null) {
        this.m_bit_byte[this.lastReturnField] = this.encoder.createSymbolModel(256);
      }
      this.encoder.encodeSymbol(this.m_bit_byte[this.lastReturnField], returnByte);
      this.lastReturnField = returnByte;
    }

    // compress the intensity if it has changed
    if (intensityChanged) {
      this.ic_intensity.compress(this.last_intensity[m], intensity, m < 3 ? m : 3);
      this.last_intensity[m] = point.getIntensity();
      this.lastIntensity = intensity;
    }

    // compress the classification ... if it has changed
    if (classificationChanged) {
      if (this.m_classification[this.lastClassificationField] == null) {
        this.m_classification[this.lastClassificationField] = this.encoder.createSymbolModel(256);
      }
      this.encoder.encodeSymbol(this.m_classification[this.lastClassificationField],
        classificationByte);
      this.lastClassificationField = classificationByte;
    }

    // compress the scan_angle_rank ... if it has changed
    if (scanAngleRankChanged) {
      final int i = point.isScanDirectionFlag() ? 1 : 0;
      this.encoder.encodeSymbol(this.m_scan_angle_rank[i],
        U8_FOLD(scanAngleRank - this.lastScanAngleRank));
      this.lastScanAngleRank = scanAngleRank;
    }

    // compress the user_data ... if it has changed
    if (userDataChanged) {
      if (this.m_user_data[this.lastUserData] == null) {
        this.m_user_data[this.lastUserData] = this.encoder.createSymbolModel(256);
      }
      this.encoder.encodeSymbol(this.m_user_data[this.lastUserData], userData);
      this.lastUserData = userData;
    }

    if (pointSourceIdChanged) {
      this.ic_point_source_ID.compress(this.lastPointSourceID, pointSourceID);
      this.lastPointSourceID = pointSourceID;
    }

    int nIndex;
    if (n == 1) {
      nIndex = 1;
    } else {
      nIndex = 0;
    }
    // compress x coordinate
    final int medianX = this.last_x_diff_median5[m].get();
    final int diffX = x - this.lastX;
    this.ic_dx.compress(medianX, diffX, nIndex);
    this.last_x_diff_median5[m].add(diffX);

    // compress y coordinate
    final int k_bitsY = this.ic_dx.getK();
    final int medianY = this.last_y_diff_median5[m].get();
    final int diffY = y - this.lastY;
    this.ic_dy.compress(medianY, diffY, nIndex + (k_bitsY < 20 ? U32_ZERO_BIT_0(k_bitsY) : 20));
    this.last_y_diff_median5[m].add(diffY);

    // compress z coordinate
    final int k_bits = (this.ic_dx.getK() + this.ic_dy.getK()) / 2;
    this.ic_z.compress(this.last_height[l], z,
      nIndex + (k_bits < 18 ? U32_ZERO_BIT_0(k_bits) : 18));
    this.last_height[l] = z;

    this.lastX = x;
    this.lastY = y;
    this.lastZ = z;
    return context;
  }

}
