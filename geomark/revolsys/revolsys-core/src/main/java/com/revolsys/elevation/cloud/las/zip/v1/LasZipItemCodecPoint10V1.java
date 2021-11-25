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
import com.revolsys.elevation.cloud.las.zip.Median;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingCodec;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingInteger;
import com.revolsys.math.arithmeticcoding.ArithmeticDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticEncoder;
import com.revolsys.math.arithmeticcoding.ArithmeticModel;

public class LasZipItemCodecPoint10V1 implements LasZipItemCodec {
  private ArithmeticDecoder decoder;

  private ArithmeticEncoder encoder;

  private final ArithmeticCodingInteger ic_dx;

  private final ArithmeticCodingInteger ic_dy;

  private final ArithmeticCodingInteger ic_intensity;

  private final ArithmeticCodingInteger ic_point_source_ID;

  private final ArithmeticCodingInteger ic_scan_angle_rank;

  private final ArithmeticCodingInteger ic_z;

  private int lastClassificationField;

  private int lastIntensity;

  private int lastPointSourceID;

  private int lastReturnField;

  private int lastScanAngleRank;

  private short lastUserData;

  private int lastX;

  private final Median lastXDiff = new Median();

  private int lastY;

  private final Median lastYDiff = new Median();

  private int lastZ;

  private final ArithmeticModel[] m_bit_byte = new ArithmeticModel[256];

  private final ArithmeticModel m_changed_values;

  private final ArithmeticModel[] m_classification = new ArithmeticModel[256];

  private final ArithmeticModel[] m_user_data = new ArithmeticModel[256];

  public LasZipItemCodecPoint10V1(final ArithmeticCodingCodec codec, final int size) {
    if (codec instanceof ArithmeticDecoder) {
      this.decoder = (ArithmeticDecoder)codec;
    } else if (codec instanceof ArithmeticEncoder) {
      this.encoder = (ArithmeticEncoder)codec;
    } else {
      throw new IllegalArgumentException("Not supported:" + codec.getClass());
    }

    this.ic_dx = codec.newCodecInteger(32);
    this.ic_dy = codec.newCodecInteger(32, 20);
    this.ic_z = codec.newCodecInteger(32, 20);
    this.ic_intensity = codec.newCodecInteger(16);
    this.ic_scan_angle_rank = codec.newCodecInteger(8, 2);
    this.ic_point_source_ID = codec.newCodecInteger(16);
    this.m_changed_values = codec.createSymbolModel(64);
  }

  @Override
  public int init(final LasPoint point, final int context) {
    /* init state */
    this.lastXDiff.reset();
    this.lastYDiff.reset();

    this.ic_dx.init();
    this.ic_dy.init();
    this.ic_z.init();
    this.ic_intensity.init();
    this.ic_scan_angle_rank.init();
    this.ic_point_source_ID.init();
    this.m_changed_values.init();
    for (int i = 0; i < 256; i++) {
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

    this.lastX = point.getXInt();
    this.lastY = point.getYInt();
    this.lastZ = point.getZInt();

    /* but set intensity to zero */
    this.lastIntensity = point.getIntensity();
    this.lastReturnField = Byte.toUnsignedInt(point.getReturnByte());
    this.lastClassificationField = Byte.toUnsignedInt(point.getClassificationByte());
    this.lastScanAngleRank = Byte.toUnsignedInt(point.getScanAngleRank());
    this.lastUserData = point.getUserData();
    this.lastPointSourceID = point.getPointSourceID();
    return context;
  }

  @Override
  public int read(final LasPoint point, final int context) {
    // find median difference for x and y from 3 preceding differences
    final int median_x = this.lastXDiff.median();
    final int median_y = this.lastYDiff.median();

    // decompress x y z coordinates
    final int x_diff = this.ic_dx.decompress(median_x);
    this.lastX += x_diff;
    // we use the number k of bits corrector bits to switch contexts
    final int kBitsX = this.ic_dx.getK(); // unsigned
    final int y_diff = this.ic_dy.decompress(median_y, kBitsX < 19 ? kBitsX : 19);
    this.lastY += y_diff;
    final int kBitsY = (kBitsX + this.ic_dy.getK()) / 2;
    this.lastZ = this.ic_z.decompress(this.lastZ, kBitsY < 19 ? kBitsY : 19);

    // System.out.println(this.i++ + "\t" + this.lastX + "\t" + this.lastY +
    // "\t" + x_diff + "\t"
    // + y_diff + "\t" + median_x + "\t" + median_y);

    // decompress which other values have changed
    final int changed_values = this.decoder.decodeSymbol(this.m_changed_values);

    if (changed_values != 0) {
      // decompress the intensity if it has changed
      if ((changed_values & 32) != 0) {
        this.lastIntensity = this.ic_intensity.decompress(this.lastIntensity);
      }

      // decompress the edge_of_flight_line, scan_direction_flag, ... if it has
      // changed
      if ((changed_values & 16) != 0) {
        if (this.m_bit_byte[this.lastReturnField] == null) {
          this.m_bit_byte[this.lastReturnField] = this.decoder.createSymbolModel(256);
          this.m_bit_byte[this.lastReturnField].init();
        }
        this.lastReturnField = this.decoder.decodeSymbol(this.m_bit_byte[this.lastReturnField]);
      }

      // decompress the classification ... if it has changed
      if ((changed_values & 8) != 0) {
        if (this.m_classification[this.lastClassificationField] == null) {
          this.m_classification[this.lastClassificationField] = this.decoder.createSymbolModel(256);
        }
        this.lastClassificationField = this.decoder
          .decodeSymbol(this.m_classification[this.lastClassificationField]);
      }

      // decompress the scan_angle_rank ... if it has changed
      if ((changed_values & 4) != 0) {
        this.lastScanAngleRank = this.ic_scan_angle_rank.decompress(this.lastScanAngleRank,
          kBitsY < 3 ? 1 : 0);
      }

      // decompress the user_data ... if it has changed
      if ((changed_values & 2) != 0) {
        if (this.m_user_data[this.lastUserData] == null) {
          this.m_user_data[this.lastUserData] = this.decoder.createSymbolModel(256);
        }
        this.lastUserData = (short)this.decoder.decodeSymbol(this.m_user_data[this.lastUserData]);
      }

      // decompress the point_source_ID ... if it has changed
      if ((changed_values & 1) != 0) {
        this.lastPointSourceID = this.ic_point_source_ID.decompress(this.lastPointSourceID);
      }
    }

    // record the difference
    this.lastXDiff.addValue(x_diff);
    this.lastYDiff.addValue(y_diff);

    point.setXYZ(this.lastX, this.lastY, this.lastZ);
    point.setIntensity(this.lastIntensity);
    point.setReturnByte((byte)this.lastReturnField);
    point.setClassificationByte((byte)this.lastClassificationField);
    point.setScanAngleRank((byte)this.lastScanAngleRank);
    point.setUserData(this.lastUserData);
    point.setPointSourceID(this.lastPointSourceID);
    return context;
  }

  @Override
  public int write(final LasPoint item, final int context) {
    final int x = item.getXInt();
    final int y = item.getYInt();
    final int z = item.getZInt();
    final int intensity = item.getIntensity();
    final int returnField = Byte.toUnsignedInt(item.getReturnByte());
    final int classificationField = Byte.toUnsignedInt(item.getClassificationByte());
    final int scanAngleRank = Byte.toUnsignedInt(item.getScanAngleRank());
    final short userData = item.getUserData();
    final int pointSourceID = item.getPointSourceID();

    // find median difference for x and y from 3 preceding differences
    final int median_x = this.lastXDiff.median();
    final int median_y = this.lastYDiff.median();

    // compress x y z coordinates
    final int x_diff = x - this.lastX;
    final int y_diff = y - this.lastY;
    // System.out.println(this.i++ + "\t" + x + "\t" + y + "\t" + x_diff + "\t"
    // + y_diff + "\t"
    // + median_x + "\t" + median_y);

    this.ic_dx.compress(median_x, x_diff);
    // we use the number k of bits corrector bits to switch contexts
    final int kBitsX = this.ic_dx.getK();
    this.ic_dy.compress(median_y, y_diff, kBitsX < 19 ? kBitsX : 19);
    final int kBitsY = (kBitsX + this.ic_dy.getK()) / 2;
    this.ic_z.compress(this.lastZ, z, kBitsY < 19 ? kBitsY : 19);

    final boolean intensityChanged = this.lastIntensity != intensity;
    final boolean returnChanged = this.lastReturnField != returnField;
    final boolean classificationChanged = this.lastClassificationField != classificationField;
    final boolean scanAngleRankChanged = this.lastScanAngleRank != scanAngleRank;
    final boolean userDataChanged = this.lastUserData != userData;
    final boolean pointSourceIdChanged = this.lastPointSourceID != pointSourceID;

    // compress which other values have changed
    int changed_values = 0;
    if (intensityChanged) {
      changed_values |= 32;
    }
    if (returnChanged) {
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

    // compress the intensity if it has changed
    if (intensityChanged) {
      this.ic_intensity.compress(this.lastIntensity, intensity);
      this.lastIntensity = intensity;
    }

    // compress the edge_of_flight_line, scan_direction_flag, ... if it has
    // changed
    if (returnChanged) {
      if (this.m_bit_byte[this.lastReturnField] == null) {
        this.m_bit_byte[this.lastReturnField] = this.encoder.createSymbolModel(256);
      }
      this.encoder.encodeSymbol(this.m_bit_byte[this.lastReturnField], returnField);
      this.lastReturnField = returnField;
    }

    // compress the classification ... if it has changed
    if (classificationChanged) {
      if (this.m_classification[this.lastClassificationField] == null) {
        this.m_classification[this.lastClassificationField] = this.encoder.createSymbolModel(256);
      }
      this.encoder.encodeSymbol(this.m_classification[this.lastClassificationField],
        classificationField);
      this.lastClassificationField = classificationField;
    }

    // compress the scan_angle_rank ... if it has changed
    if (scanAngleRankChanged) {
      int scanAngleContext;
      if (kBitsY < 3) {
        scanAngleContext = 1;
      } else {
        scanAngleContext = 0;
      }
      this.ic_scan_angle_rank.compress(this.lastScanAngleRank, scanAngleRank, scanAngleContext);
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

    // compress the point_source_ID ... if it has changed
    if (pointSourceIdChanged) {
      this.ic_point_source_ID.compress(this.lastPointSourceID, pointSourceID);
      this.lastPointSourceID = pointSourceID;
    }

    // record the difference
    this.lastXDiff.addValue(x_diff);
    this.lastYDiff.addValue(y_diff);

    this.lastX = x;
    this.lastY = y;
    this.lastZ = z;
    return context;
  }
}
