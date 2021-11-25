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
package com.revolsys.elevation.cloud.las.zip.v3;

import java.util.Arrays;

import org.jeometry.common.number.Longs;

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.elevation.cloud.las.zip.ArithmeticDecoderByteArray;
import com.revolsys.elevation.cloud.las.zip.ArithmeticEncoderByteArray;
import com.revolsys.elevation.cloud.las.zip.LasZipDecompressSelective;
import com.revolsys.elevation.cloud.las.zip.LasZipItemCodec;
import com.revolsys.elevation.cloud.las.zip.context.LasZipContextPoint14;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.io.channels.DataReader;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingCodec;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingInteger;
import com.revolsys.math.arithmeticcoding.ArithmeticDecoder;
import com.revolsys.math.arithmeticcoding.ArithmeticEncoder;
import com.revolsys.math.arithmeticcoding.ArithmeticModel;

public class LasZipItemCodecPoint14V3 implements LasZipItemCodec {
  private static final int FLAG_NUMBER_OF_RETURNS = 0b100;

  private static final int FLAG_SCANNER_CHANNEL = 0b1000000;

  private static final int FLAG_GPS_TIME = 0b10000;

  private static final int FLAG_POINT_SOURCE_ID = 0b100000;

  private static final int FLAG_SCAN_ANGLE = 0b1000;

  private static final int LASZIP_GPSTIME_MULTI = 500;

  private static final int LASZIP_GPSTIME_MULTI_MINUS = -10;

  private static final int LASZIP_GPSTIME_MULTI_CODE_FULL = LASZIP_GPSTIME_MULTI
    - LASZIP_GPSTIME_MULTI_MINUS + 1;

  private static final int LASZIP_GPSTIME_MULTI_TOTAL = LASZIP_GPSTIME_MULTI
    - LASZIP_GPSTIME_MULTI_MINUS + 5;

  public static final byte[][] NUMBER_RETURN_LEVEL_8CTX = {
    {
      0, 1, 2, 3, 4, 5, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7
    }, {
      1, 0, 1, 2, 3, 4, 5, 6, 7, 7, 7, 7, 7, 7, 7, 7
    }, {
      2, 1, 0, 1, 2, 3, 4, 5, 6, 7, 7, 7, 7, 7, 7, 7
    }, {
      3, 2, 1, 0, 1, 2, 3, 4, 5, 6, 7, 7, 7, 7, 7, 7
    }, {
      4, 3, 2, 1, 0, 1, 2, 3, 4, 5, 6, 7, 7, 7, 7, 7
    }, {
      5, 4, 3, 2, 1, 0, 1, 2, 3, 4, 5, 6, 7, 7, 7, 7
    }, {
      6, 5, 4, 3, 2, 1, 0, 1, 2, 3, 4, 5, 6, 7, 7, 7
    }, {
      7, 6, 5, 4, 3, 2, 1, 0, 1, 2, 3, 4, 5, 6, 7, 7
    }, {
      7, 7, 6, 5, 4, 3, 2, 1, 0, 1, 2, 3, 4, 5, 6, 7
    }, {
      7, 7, 7, 6, 5, 4, 3, 2, 1, 0, 1, 2, 3, 4, 5, 6
    }, {
      7, 7, 7, 7, 6, 5, 4, 3, 2, 1, 0, 1, 2, 3, 4, 5
    }, {
      7, 7, 7, 7, 7, 6, 5, 4, 3, 2, 1, 0, 1, 2, 3, 4
    }, {
      7, 7, 7, 7, 7, 7, 6, 5, 4, 3, 2, 1, 0, 1, 2, 3
    }, {
      7, 7, 7, 7, 7, 7, 7, 6, 5, 4, 3, 2, 1, 0, 1, 2
    }, {
      7, 7, 7, 7, 7, 7, 7, 7, 6, 5, 4, 3, 2, 1, 0, 1
    }, {
      7, 7, 7, 7, 7, 7, 7, 7, 7, 6, 5, 4, 3, 2, 1, 0
    }
  };

  public static final byte[][] NUMBER_RETURN_MAP_6CTX = {
    {
      0, 1, 2, 3, 4, 5, 3, 4, 4, 5, 5, 5, 5, 5, 5, 5
    }, {
      1, 0, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3
    }, {
      2, 1, 2, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3
    }, {
      3, 3, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4
    }, {
      4, 3, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4
    }, {
      5, 3, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4
    }, {
      3, 3, 4, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4
    }, {
      4, 3, 4, 4, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4
    }, {
      4, 3, 4, 4, 4, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4
    }, {
      5, 3, 4, 4, 4, 4, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4
    }, {
      5, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 4, 4, 4, 4, 4
    }, {
      5, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 4, 4, 4
    }, {
      5, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 4, 4
    }, {
      5, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 4
    }, {
      5, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5
    }, {
      5, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5
    }
  };

  private boolean changed_classification;

  private boolean changed_flags;

  private boolean changed_gps_time;

  private boolean changed_intensity;

  private boolean changed_point_source;

  private boolean changed_scan_angle;

  private boolean changed_user_data;

  private boolean changed_Z;

  private final LasZipContextPoint14[] contexts = new LasZipContextPoint14[4];

  private int current_context;

  private ArithmeticDecoder dec;

  private final ArithmeticDecoderByteArray dec_channel_returns_XY = new ArithmeticDecoderByteArray();

  private final ArithmeticDecoderByteArray dec_classification = new ArithmeticDecoderByteArray();

  private final ArithmeticDecoderByteArray dec_flags = new ArithmeticDecoderByteArray();

  private final ArithmeticDecoderByteArray dec_gps_time = new ArithmeticDecoderByteArray();

  private final ArithmeticDecoderByteArray dec_intensity = new ArithmeticDecoderByteArray();

  private final ArithmeticDecoderByteArray dec_point_source = new ArithmeticDecoderByteArray();

  private final ArithmeticDecoderByteArray dec_scan_angle = new ArithmeticDecoderByteArray();

  private final ArithmeticDecoderByteArray dec_user_data = new ArithmeticDecoderByteArray();

  private final ArithmeticDecoderByteArray dec_Z = new ArithmeticDecoderByteArray();

  private ArithmeticEncoder enc;

  private final ArithmeticEncoderByteArray enc_channel_returns_XY = new ArithmeticEncoderByteArray();

  private final ArithmeticEncoderByteArray enc_classification = new ArithmeticEncoderByteArray();

  private final ArithmeticEncoderByteArray enc_flags = new ArithmeticEncoderByteArray();

  private final ArithmeticEncoderByteArray enc_gps_time = new ArithmeticEncoderByteArray();

  private final ArithmeticEncoderByteArray enc_intensity = new ArithmeticEncoderByteArray();

  private final ArithmeticEncoderByteArray enc_point_source = new ArithmeticEncoderByteArray();

  private final ArithmeticEncoderByteArray enc_scan_angle = new ArithmeticEncoderByteArray();

  private final ArithmeticEncoderByteArray enc_user_data = new ArithmeticEncoderByteArray();

  private final ArithmeticEncoderByteArray enc_Z = new ArithmeticEncoderByteArray();

  private final int version;

  public LasZipItemCodecPoint14V3(final ArithmeticCodingCodec codec, final int version) {
    if (codec instanceof ArithmeticDecoder) {
      this.dec = (ArithmeticDecoder)codec;
    } else if (codec instanceof ArithmeticEncoder) {
      this.enc = (ArithmeticEncoder)codec;
    } else {
      throw new IllegalArgumentException("Not supported:" + codec.getClass());
    }
    this.version = version;

    for (int i = 0; i < this.contexts.length; i++) {
      this.contexts[i] = new LasZipContextPoint14();
    }
    final int decompressSelective = LasZipDecompressSelective.ALL;
    this.dec_Z.setEnabled(decompressSelective, LasZipDecompressSelective.Z);
    this.dec_classification.setEnabled(decompressSelective,
      LasZipDecompressSelective.CLASSIFICATION);
    this.dec_flags.setEnabled(decompressSelective, LasZipDecompressSelective.FLAGS);
    this.dec_intensity.setEnabled(decompressSelective, LasZipDecompressSelective.INTENSITY);
    this.dec_scan_angle.setEnabled(decompressSelective, LasZipDecompressSelective.SCAN_ANGLE);
    this.dec_user_data.setEnabled(decompressSelective, LasZipDecompressSelective.USER_DATA);
    this.dec_point_source.setEnabled(decompressSelective, LasZipDecompressSelective.POINT_SOURCE);
    this.dec_gps_time.setEnabled(decompressSelective, LasZipDecompressSelective.GPS_TIME);

  }

  @Override
  public int getVersion() {
    return this.version;
  }

  @Override
  public int init(final LasPoint point, final int contextIndex) {
    if (this.enc != null) {
      return writeInit(point);
    }

    if (this.dec != null) {
      return readInit(point);
    }
    return this.current_context;
  }

  private void initContext(final int contextIndex, final LasPoint point,
    final ArithmeticCodingCodec codecXY, final ArithmeticCodingCodec codecZ,
    final ArithmeticCodingCodec codecIntensity, final ArithmeticCodingCodec codecScanAngle,
    final ArithmeticCodingCodec codecPointSource, final ArithmeticCodingCodec codecGpsTime) {
    final LasZipContextPoint14 context = this.contexts[contextIndex];

    if (context.m_changed_values[0] == null) {
      /* for the channel_returns_XY layer */
      for (int i = 0; i <= 7; i++) {
        context.m_changed_values[i] = codecXY.createSymbolModel(128);
      }
      context.m_scanner_channel = codecXY.createSymbolModel(3);
      Arrays.fill(context.m_number_of_returns, null);
      Arrays.fill(context.m_return_number, null);

      context.m_return_number_gps_same = codecXY.createSymbolModel(13);

      context.ic_dX = new ArithmeticCodingInteger(codecXY, 32, 2);
      context.ic_dY = new ArithmeticCodingInteger(codecXY, 32, 22);
      context.ic_Z = new ArithmeticCodingInteger(codecZ, 32, 20);

      Arrays.fill(context.m_classification, null);
      Arrays.fill(context.m_flags, null);
      Arrays.fill(context.m_user_data, null);

      context.ic_intensity = new ArithmeticCodingInteger(codecIntensity, 16, 4);

      context.ic_scan_angle = new ArithmeticCodingInteger(codecScanAngle, 16, 2);

      context.ic_point_source_ID = new ArithmeticCodingInteger(codecPointSource, 16);

      context.m_gpstime_multi = codecGpsTime.createSymbolModel(LASZIP_GPSTIME_MULTI_TOTAL);
      context.m_gpstime_0diff = codecGpsTime.createSymbolModel(5);
      context.ic_gpstime = new ArithmeticCodingInteger(codecGpsTime, 32, 9);
    }

    ArithmeticModel.initModels(context.m_changed_values);
    ArithmeticModel.initModel(context.m_scanner_channel);
    ArithmeticModel.initModels(context.m_number_of_returns);
    ArithmeticModel.initModels(context.m_return_number);
    ArithmeticModel.initModel(context.m_return_number_gps_same);
    context.ic_dX.init();
    context.ic_dY.init();
    for (int i = 0; i < 12; i++) {
      context.last_X_diff_median5[i].init();
      context.last_Y_diff_median5[i].init();
    }
    context.ic_Z.init();
    for (int i = 0; i < 8; i++) {
      context.last_Z[i] = point.getZInt();
    }

    ArithmeticModel.initModels(context.m_classification);
    ArithmeticModel.initModels(context.m_flags);
    ArithmeticModel.initModels(context.m_user_data);

    context.ic_intensity.init();
    for (int i = 0; i < 8; i++) {
      context.last_intensity[i] = point.getIntensity();
    }

    context.ic_scan_angle.init();

    context.ic_point_source_ID.init();

    ArithmeticModel.initModel(context.m_gpstime_multi);
    ArithmeticModel.initModel(context.m_gpstime_0diff);
    context.ic_gpstime.init();
    context.last = 0;
    context.next = 0;
    context.last_gpstime_diff[0] = 0;
    context.last_gpstime_diff[1] = 0;
    context.last_gpstime_diff[2] = 0;
    context.last_gpstime_diff[3] = 0;
    context.multi_extreme_counter[0] = 0;
    context.multi_extreme_counter[1] = 0;
    context.multi_extreme_counter[2] = 0;
    context.multi_extreme_counter[3] = 0;
    context.last_gpstime[0] = point.getGpsTimeLong();
    context.last_gpstime[1] = 0;
    context.last_gpstime[2] = 0;
    context.last_gpstime[3] = 0;

    context.lastPoint = point;
    context.gps_time_change = false;

    context.unused = false;
  }

  @Override
  public int read(final LasPoint point, int contextIndex) {
    LasZipContextPoint14 context = this.contexts[this.current_context];
    LasPoint lastPoint = context.lastPoint;

    final byte lastNumberOfReturns = lastPoint.getNumberOfReturns();
    final byte lastReturnNumber = lastPoint.getReturnNumber();
    int lpr = 0;
    if (lastReturnNumber == 1) {
      lpr |= 0b1;
    }
    if (lastReturnNumber >= lastNumberOfReturns) {
      lpr |= 0b10;
    }
    if (context.gps_time_change) {
      lpr |= 0b100;
    }

    final int changedValues = this.dec_channel_returns_XY
      .decodeSymbol(context.m_changed_values[lpr]);

    if ((changedValues & FLAG_SCANNER_CHANNEL) != 0) {
      final int diff = this.dec_channel_returns_XY.decodeSymbol(context.m_scanner_channel);

      final int scanner_channel = (this.current_context + diff + 1) % 4;
      if (this.contexts[scanner_channel].unused) {
        readInitContext(scanner_channel, context.lastPoint);
      }
      this.current_context = scanner_channel;
      context = this.contexts[this.current_context];
      lastPoint = context.lastPoint;
      point.setScannerChannel((byte)scanner_channel);
      if (this.version < 4) {
        contextIndex = this.current_context;
      }
    }
    if (this.version >= 4) {
      contextIndex = this.current_context;
    }

    final boolean gps_time_change = (changedValues & FLAG_GPS_TIME) != 0;

    byte numberOfReturns;
    if ((changedValues & FLAG_NUMBER_OF_RETURNS) != 0) {
      numberOfReturns = (byte)this.dec_channel_returns_XY.decodeSymbol(context.m_number_of_returns,
        lastNumberOfReturns, 16);
    } else {
      numberOfReturns = lastNumberOfReturns;
    }
    point.setNumberOfReturns(numberOfReturns);

    byte returnNumber;
    switch (changedValues & 3) {
      case 0:
        returnNumber = lastReturnNumber;
      break;
      case 1:
        returnNumber = (byte)((lastReturnNumber + 1) % 16);
      break;
      case 2:
        returnNumber = (byte)((lastReturnNumber + 15) % 16);
      break;
      default:
        if (gps_time_change) {
          returnNumber = (byte)this.dec_channel_returns_XY.decodeSymbol(context.m_return_number,
            lastReturnNumber, 16);
        } else {
          final int sym = this.dec_channel_returns_XY
            .decodeSymbol(context.m_return_number_gps_same);
          returnNumber = (byte)((lastReturnNumber + sym + 2) % 16);
        }
      break;
    }
    point.setReturnNumber(returnNumber);

    final int m = NUMBER_RETURN_MAP_6CTX[numberOfReturns][returnNumber];
    final int l = NUMBER_RETURN_LEVEL_8CTX[numberOfReturns][returnNumber];

    int cpr = returnNumber == 1 ? 2 : 0;
    cpr += returnNumber >= numberOfReturns ? 1 : 0;

    final int gpsTimeChangeBit = gps_time_change ? 1 : 0;
    final int medianX = context.last_X_diff_median5[m << 1 | gpsTimeChangeBit].get();
    final int diffX = context.ic_dX.decompress(medianX, numberOfReturns == 1 ? 1 : 0);
    point.setXInt(lastPoint.getXInt() + diffX);
    context.last_X_diff_median5[m << 1 | gpsTimeChangeBit].add(diffX);

    final int medianY = context.last_Y_diff_median5[m << 1 | gpsTimeChangeBit].get();
    int k_bits = context.ic_dX.getK();
    final int diffY = context.ic_dY.decompress(medianY,
      (numberOfReturns == 1 ? 1 : 0) + (k_bits < 20 ? U32_ZERO_BIT_0(k_bits) : 20));
    point.setYInt(lastPoint.getYInt() + diffY);
    context.last_Y_diff_median5[m << 1 | gpsTimeChangeBit].add(diffY);

    int z = lastPoint.getZInt();
    if (this.changed_Z) {
      k_bits = (context.ic_dX.getK() + context.ic_dY.getK()) / 2;
      z = context.ic_Z.decompress(context.last_Z[l],
        (numberOfReturns == 1 ? 1 : 0) + (k_bits < 18 ? U32_ZERO_BIT_0(k_bits) : 18));
      context.last_Z[l] = z;
    }
    point.setZInt(z);

    short classification = lastPoint.getClassification();
    if (this.changed_classification) {
      final int last_classification = lastPoint.getClassification();
      final int ccc = ((last_classification & 0x1F) << 1) + (cpr == 3 ? 1 : 0);
      classification = (short)this.dec_classification.decodeSymbol(context.m_classification, ccc,
        256);

    }
    point.setClassification(classification);

    if (this.changed_flags) {
      final int last_flags = lastPoint.isEdgeOfFlightLine() ? 1 << 5
        : 0 | (lastPoint.isScanDirectionFlag() ? 0b10000 : 0) | lastPoint.getClassificationFlags();
      final int flags = this.dec_flags.decodeSymbol(context.m_flags, last_flags, 64);
      point.setEdgeOfFlightLine((flags & 1 << 5) != 0);
      point.setScanDirectionFlag((flags & 0b10000) != 0);
      point.setClassificationFlags((byte)(flags & 0x0F));
    }

    int intensity = lastPoint.getIntensity();
    if (this.changed_intensity) {
      intensity = context.ic_intensity
        .decompress(context.last_intensity[cpr << 1 | gpsTimeChangeBit], cpr);
      context.last_intensity[cpr << 1 | gpsTimeChangeBit] = intensity;
    }
    point.setIntensity(intensity);

    short scanAngle = lastPoint.getScanAngle();
    if (this.changed_scan_angle) {
      if ((changedValues & FLAG_SCAN_ANGLE) != 0) {
        scanAngle = (short)context.ic_scan_angle.decompress(scanAngle, gpsTimeChangeBit);
      }
    }
    point.setScanAngle(scanAngle);

    short userData = lastPoint.getUserData();
    if (this.changed_user_data) {
      final int lastUserDataDiv4 = lastPoint.getUserData() / 4;
      userData = (short)this.dec_user_data.decodeSymbol(context.m_user_data, lastUserDataDiv4, 256);
    }
    point.setUserData(userData);

    int pointSourceId = lastPoint.getPointSourceID();
    if (this.changed_point_source) {
      if ((changedValues & FLAG_POINT_SOURCE_ID) != 0) {
        pointSourceId = context.ic_point_source_ID.decompress(lastPoint.getPointSourceID());
      }
    }
    point.setPointSourceID(pointSourceId);

    long gpsTime = lastPoint.getGpsTimeLong();
    if (this.changed_gps_time) {
      if (gps_time_change) {
        readGpsTime();
        gpsTime = context.last_gpstime[context.last];
      }
    }
    point.setGpsTimeLong(gpsTime);

    context.lastPoint = point;
    context.gps_time_change = gps_time_change;
    return contextIndex;
  }

  @Override
  public void readChunkSizes() {
    final DataReader in = this.dec.getIn();
    this.dec_channel_returns_XY.readSize(in);
    this.dec_Z.readSize(in);
    this.dec_classification.readSize(in);
    this.dec_flags.readSize(in);
    this.dec_intensity.readSize(in);
    this.dec_scan_angle.readSize(in);
    this.dec_user_data.readSize(in);
    this.dec_point_source.readSize(in);
    this.dec_gps_time.readSize(in);
  }

  private void readGpsTime() {
    final LasZipContextPoint14 context = this.contexts[this.current_context];
    if (context.last_gpstime_diff[context.last] == 0) {
      final int multi = this.dec_gps_time.decodeSymbol(context.m_gpstime_0diff);
      if (multi == 0) {
        final int timeDiff = context.ic_gpstime.decompress(0, 0);
        context.last_gpstime_diff[context.last] = timeDiff;
        context.last_gpstime[context.last] += timeDiff;
        context.multi_extreme_counter[context.last] = 0;
      } else if (multi == 1) {
        context.next = context.next + 1 & 3;
        final int lastTimeUpper = (int)(context.last_gpstime[context.last] >>> 32);
        final int timeUpper = context.ic_gpstime.decompress(lastTimeUpper, 8);
        final int timeLower = this.dec_gps_time.readInt();
        context.last_gpstime[context.next] = Longs.toLong(timeUpper, timeLower);
        context.last = context.next;
        context.last_gpstime_diff[context.last] = 0;
        context.multi_extreme_counter[context.last] = 0;
      } else {
        context.last = context.last + multi - 1 & 3;
        readGpsTime();
      }
    } else {
      int multi = this.dec_gps_time.decodeSymbol(context.m_gpstime_multi);
      if (multi == 1) {
        context.last_gpstime[context.last] += context.ic_gpstime
          .decompress(context.last_gpstime_diff[context.last], 1);
        context.multi_extreme_counter[context.last] = 0;
      } else if (multi < LASZIP_GPSTIME_MULTI_CODE_FULL) {
        int gpstime_diff;
        if (multi == 0) {
          gpstime_diff = context.ic_gpstime.decompress(0, 7);
          context.multi_extreme_counter[context.last]++;
          if (context.multi_extreme_counter[context.last] > 3) {
            context.last_gpstime_diff[context.last] = gpstime_diff;
            context.multi_extreme_counter[context.last] = 0;
          }
        } else if (multi < LASZIP_GPSTIME_MULTI) {
          if (multi < 10) {
            gpstime_diff = context.ic_gpstime
              .decompress(multi * context.last_gpstime_diff[context.last], 2);
          } else {
            gpstime_diff = context.ic_gpstime
              .decompress(multi * context.last_gpstime_diff[context.last], 3);
          }
        } else if (multi == LASZIP_GPSTIME_MULTI) {
          gpstime_diff = context.ic_gpstime
            .decompress(LASZIP_GPSTIME_MULTI * context.last_gpstime_diff[context.last], 4);
          context.multi_extreme_counter[context.last]++;
          if (context.multi_extreme_counter[context.last] > 3) {
            context.last_gpstime_diff[context.last] = gpstime_diff;
            context.multi_extreme_counter[context.last] = 0;
          }
        } else {
          multi = LASZIP_GPSTIME_MULTI - multi;
          if (multi > LASZIP_GPSTIME_MULTI_MINUS) {
            gpstime_diff = context.ic_gpstime
              .decompress(multi * context.last_gpstime_diff[context.last], 5);
          } else {
            gpstime_diff = context.ic_gpstime
              .decompress(LASZIP_GPSTIME_MULTI_MINUS * context.last_gpstime_diff[context.last], 6);
            context.multi_extreme_counter[context.last]++;
            if (context.multi_extreme_counter[context.last] > 3) {
              context.last_gpstime_diff[context.last] = gpstime_diff;
              context.multi_extreme_counter[context.last] = 0;
            }
          }
        }
        context.last_gpstime[context.last] += gpstime_diff;
      } else if (multi == LASZIP_GPSTIME_MULTI_CODE_FULL) {
        context.next = context.next + 1 & 3;
        final int lastTimeUpper = (int)(context.last_gpstime[context.last] >>> 32);
        final int timeUpper = context.ic_gpstime.decompress(lastTimeUpper, 8);
        final int timeLower = this.dec_gps_time.readInt();
        context.last_gpstime[context.next] = Longs.toLong(timeUpper, timeLower);
        context.last = context.next;
        context.last_gpstime_diff[context.last] = 0;
        context.multi_extreme_counter[context.last] = 0;
      } else if (multi >= LASZIP_GPSTIME_MULTI_CODE_FULL) {
        context.last = context.last + multi - LASZIP_GPSTIME_MULTI_CODE_FULL & 3;
        readGpsTime();
      }
    }
  }

  public int readInit(final LasPoint point) {
    final DataReader in = this.dec.getIn();

    this.dec_channel_returns_XY.readBytes(in);

    this.changed_Z = this.dec_Z.readBytes(in);

    this.changed_classification = this.dec_classification.readBytes(in);

    this.changed_flags = this.dec_flags.readBytes(in);

    this.changed_intensity = this.dec_intensity.readBytes(in);

    this.changed_scan_angle = this.dec_scan_angle.readBytes(in);

    this.changed_user_data = this.dec_user_data.readBytes(in);

    this.changed_point_source = this.dec_point_source.readBytes(in);

    this.changed_gps_time = this.dec_gps_time.readBytes(in);

    for (final LasZipContextPoint14 context : this.contexts) {
      context.unused = true;
    }

    this.current_context = point.getScannerChannel();

    readInitContext(this.current_context, point);

    return this.current_context;
  }

  private void readInitContext(final int contextIndex, final LasPoint point) {
    initContext(contextIndex, point, this.dec_channel_returns_XY, this.dec_Z, this.dec_intensity,
      this.dec_scan_angle, this.dec_point_source, this.dec_gps_time);
  }

  @Override
  public int write(final LasPoint point, int contextIndex) {
    final LasZipContextPoint14 context = this.contexts[this.current_context];
    LasPoint lastPoint = context.lastPoint;

    final byte lastNumberOfReturns = lastPoint.getNumberOfReturns();
    final byte lastReturnNumber = lastPoint.getReturnNumber();
    int lpr = 0;
    if (lastReturnNumber == 1) {
      lpr |= 0b1;
    }
    if (lastReturnNumber >= lastNumberOfReturns) {
      lpr |= 0b10;
    }
    if (context.gps_time_change) {
      lpr |= 0b100;
    }

    final int scannerChannel = point.getScannerChannel();
    final boolean scannerChannelChanged = scannerChannel != this.current_context;
    if (scannerChannelChanged) {
      if (this.contexts[scannerChannel].unused == false) {
        lastPoint = this.contexts[scannerChannel].lastPoint;
      }
    }

    final int numberOfReturns = point.getNumberOfReturns();
    final int returnNumber = point.getReturnNumber();

    int changed_values = 0;
    if (returnNumber != lastReturnNumber) {
      if (returnNumber == (lastReturnNumber + 1) % 16) {
        changed_values |= 1;
      } else if (returnNumber == (lastReturnNumber + 15) % 16) {
        changed_values |= 2;
      } else {
        changed_values |= 3;
      }
    }
    final boolean numberOfReturnsChanged = numberOfReturns != lastNumberOfReturns;
    if (numberOfReturnsChanged) {
      changed_values |= FLAG_NUMBER_OF_RETURNS;
    }

    final short lastScanAngle = lastPoint.getScanAngle();
    final short scanAngle = point.getScanAngle();
    final boolean scan_angle_change = scanAngle != lastScanAngle;
    if (scan_angle_change) {
      changed_values |= FLAG_SCAN_ANGLE;
    }

    final long gpsTime = point.getGpsTimeLong();
    final boolean gps_time_change = gpsTime != lastPoint.getGpsTimeLong();
    if (gps_time_change) {
      changed_values |= FLAG_GPS_TIME;
    }

    final int lasPointSourceID = lastPoint.getPointSourceID();
    final int pointSourceID = point.getPointSourceID();
    final boolean point_source_change = pointSourceID != lasPointSourceID;
    if (point_source_change) {
      changed_values |= FLAG_POINT_SOURCE_ID;
    }

    if (scannerChannelChanged) {
      changed_values |= FLAG_SCANNER_CHANNEL;
    }

    this.enc_channel_returns_XY.encodeSymbol(context.m_changed_values[lpr], changed_values);

    if (scannerChannelChanged) {
      final int diff = scannerChannel - this.current_context;
      if (diff > 0) {
        this.enc_channel_returns_XY.encodeSymbol(context.m_scanner_channel, diff - 1);
      } else {
        this.enc_channel_returns_XY.encodeSymbol(context.m_scanner_channel, diff + 4 - 1);
      }
      if (this.contexts[scannerChannel].unused) {
        writeInitContext(scannerChannel, context.lastPoint);
        lastPoint = this.contexts[scannerChannel].lastPoint;
      }
      this.current_context = scannerChannel;
      if (this.version < 4) {
        contextIndex = this.current_context;
      }
    }
    if (this.version >= 4) {
      contextIndex = this.current_context;
    }

    if (numberOfReturnsChanged) {
      this.enc_channel_returns_XY.encodeSymbol(context.m_number_of_returns, lastNumberOfReturns, 16,
        numberOfReturns);
    }

    if ((changed_values & 3) == 3) {
      if (gps_time_change) {
        this.enc_channel_returns_XY.encodeSymbol(context.m_return_number, lastReturnNumber, 16,
          returnNumber);
      } else {
        final int diff = returnNumber - lastReturnNumber;
        if (diff > 1) {
          this.enc_channel_returns_XY.encodeSymbol(context.m_return_number_gps_same, diff - 2);
        } else {
          this.enc_channel_returns_XY.encodeSymbol(context.m_return_number_gps_same, diff + 16 - 2);
        }
      }
    }

    final int m = NUMBER_RETURN_MAP_6CTX[numberOfReturns][returnNumber];
    final int l = NUMBER_RETURN_LEVEL_8CTX[numberOfReturns][returnNumber];

    int cpr = returnNumber == 1 ? 2 : 0;
    cpr += returnNumber >= numberOfReturns ? 1 : 0;

    final int gpsTimeChangeFlag = gps_time_change ? 1 : 0;
    final int medianX = context.last_X_diff_median5[m << 1 | gpsTimeChangeFlag].get();
    final int diffX = point.getXInt() - lastPoint.getXInt();
    context.ic_dX.compress(medianX, diffX, numberOfReturns == 1 ? 1 : 0);
    context.last_X_diff_median5[m << 1 | gpsTimeChangeFlag].add(diffX);

    int k_bits = context.ic_dX.getK();
    final int medianY = context.last_Y_diff_median5[m << 1 | gpsTimeChangeFlag].get();
    final int diffY = point.getYInt() - lastPoint.getYInt();
    context.ic_dY.compress(medianY, diffY,
      (numberOfReturns == 1 ? 1 : 0) + (k_bits < 20 ? U32_ZERO_BIT_0(k_bits) : 20));
    context.last_Y_diff_median5[m << 1 | gpsTimeChangeFlag].add(diffY);

    k_bits = (context.ic_dX.getK() + context.ic_dY.getK()) / 2;
    context.ic_Z.compress(context.last_Z[l], point.getZInt(),
      (numberOfReturns == 1 ? 1 : 0) + (k_bits < 18 ? U32_ZERO_BIT_0(k_bits) : 18));
    context.last_Z[l] = point.getZInt();

    final int lastClassification = lastPoint.getClassification();
    final int classification = point.getClassification();

    if (classification != lastClassification) {
      this.changed_classification = true;
    }

    final int ccc = ((lastClassification & 0x1F) << 1) + (cpr == 3 ? 1 : 0);
    this.enc_classification.encodeSymbol(context.m_classification, ccc, 256, classification);

    final int last_flags = (lastPoint.isEdgeOfFlightLine() ? 1 : 0) << 5
      | (lastPoint.isScanDirectionFlag() ? 1 : 0) << 4 | lastPoint.getClassificationFlags() & 0xF;
    final int flags = (point.isEdgeOfFlightLine() ? 1 : 0) << 5
      | (point.isScanDirectionFlag() ? 1 : 0) << 4 | point.getClassificationFlags() & 0xF;

    if (flags != last_flags) {
      this.changed_flags = true;
    }

    this.enc_flags.encodeSymbol(context.m_flags, last_flags, 64, flags);

    final int intensity = point.getIntensity();
    final int lastIntensity = lastPoint.getIntensity();
    if (intensity != lastIntensity) {
      this.changed_intensity = true;
    }
    context.ic_intensity.compress(context.last_intensity[cpr << 1 | gpsTimeChangeFlag], intensity,
      cpr);
    context.last_intensity[cpr << 1 | gpsTimeChangeFlag] = intensity;

    if (scan_angle_change) {
      this.changed_scan_angle = true;
      context.ic_scan_angle.compress(lastScanAngle, scanAngle, gpsTimeChangeFlag);
    }

    final short lastUserData = lastPoint.getUserData();
    final short userData = point.getUserData();
    if (userData != lastUserData) {
      this.changed_user_data = true;
    }
    this.enc_user_data.encodeSymbol(context.m_user_data, lastUserData / 4, 256, userData);

    if (point_source_change) {
      this.changed_point_source = true;
      context.ic_point_source_ID.compress(lasPointSourceID, pointSourceID);
    }

    if (gps_time_change) {
      this.changed_gps_time = true;
      writeGpsTime(gpsTime);
    }

    context.lastPoint = point;
    context.gps_time_change = gps_time_change;

    return contextIndex;

  }

  @Override
  public void writeChunkBytes() {
    final ChannelWriter writer = this.enc.getWriter();

    this.enc_channel_returns_XY.writeBytes(writer);

    this.enc_Z.writeBytes(writer);

    this.enc_classification.writeBytes(writer, this.changed_classification);

    this.enc_flags.writeBytes(writer, this.changed_flags);

    this.enc_intensity.writeBytes(writer, this.changed_intensity);

    this.enc_scan_angle.writeBytes(writer, this.changed_scan_angle);

    this.enc_user_data.writeBytes(writer, this.changed_user_data);

    this.enc_point_source.writeBytes(writer, this.changed_point_source);

    this.enc_gps_time.writeBytes(writer, this.changed_gps_time);
  }

  @Override
  public void writeChunkSizes() {
    final ChannelWriter writer = this.enc.getWriter();

    this.enc_channel_returns_XY.writeSize(writer, true);

    this.enc_Z.writeSize(writer, true);

    this.enc_classification.writeSize(writer, this.changed_classification);

    this.enc_flags.writeSize(writer, this.changed_flags);

    this.enc_intensity.writeSize(writer, this.changed_intensity);

    this.enc_scan_angle.writeSize(writer, this.changed_scan_angle);

    this.enc_user_data.writeSize(writer, this.changed_user_data);

    this.enc_point_source.writeSize(writer, this.changed_point_source);

    this.enc_gps_time.writeSize(writer, this.changed_gps_time);
  }

  private void writeGpsTime(final long gpsTime) {
    final LasZipContextPoint14 context = this.contexts[this.current_context];
    if (context.last_gpstime_diff[context.last] == 0) {
      final long curr_gpstime_diff_64 = gpsTime - context.last_gpstime[context.last];
      final int curr_gpstime_diff = (int)curr_gpstime_diff_64;
      if (curr_gpstime_diff_64 == curr_gpstime_diff) {
        this.enc_gps_time.encodeSymbol(context.m_gpstime_0diff, 0);
        context.ic_gpstime.compress(0, curr_gpstime_diff, 0);
        context.last_gpstime_diff[context.last] = curr_gpstime_diff;
        context.multi_extreme_counter[context.last] = 0;
      } else {
        for (int i = 1; i < 4; i++) {
          final int lastIndex = context.last + i & 3;
          final long other_gpstime_diff_64 = gpsTime - context.last_gpstime[lastIndex];
          final int other_gpstime_diff = (int)other_gpstime_diff_64;
          if (other_gpstime_diff_64 == other_gpstime_diff) {
            this.enc_gps_time.encodeSymbol(context.m_gpstime_0diff, i + 1);
            context.last = lastIndex;
            writeGpsTime(gpsTime);
            return;
          }
        }
        this.enc_gps_time.encodeSymbol(context.m_gpstime_0diff, 1);
        context.ic_gpstime.compress((int)(context.last_gpstime[context.last] >>> 32),
          (int)(gpsTime >>> 32), 8);
        this.enc_gps_time.writeInt((int)(gpsTime & 0xFFFFFFFFL));
        context.next = context.next + 1 & 3;
        context.last = context.next;
        context.last_gpstime_diff[context.last] = 0;
        context.multi_extreme_counter[context.last] = 0;
      }
      context.last_gpstime[context.last] = gpsTime;
    } else {
      final long curr_gpstime_diff_64 = gpsTime - context.last_gpstime[context.last];
      final int curr_gpstime_diff = (int)curr_gpstime_diff_64;

      if (curr_gpstime_diff_64 == curr_gpstime_diff) {
        final float multi_f = (float)curr_gpstime_diff
          / (float)context.last_gpstime_diff[context.last];
        final int multi = I32_QUANTIZE(multi_f);

        if (multi == 1) {
          this.enc_gps_time.encodeSymbol(context.m_gpstime_multi, 1);
          context.ic_gpstime.compress(context.last_gpstime_diff[context.last], curr_gpstime_diff,
            1);
          context.multi_extreme_counter[context.last] = 0;
        } else if (multi > 0) {
          if (multi < LASZIP_GPSTIME_MULTI) {
            this.enc_gps_time.encodeSymbol(context.m_gpstime_multi, multi);
            if (multi < 10) {
              context.ic_gpstime.compress(multi * context.last_gpstime_diff[context.last],
                curr_gpstime_diff, 2);
            } else {
              context.ic_gpstime.compress(multi * context.last_gpstime_diff[context.last],
                curr_gpstime_diff, 3);
            }
          } else {
            this.enc_gps_time.encodeSymbol(context.m_gpstime_multi, LASZIP_GPSTIME_MULTI);
            context.ic_gpstime.compress(
              LASZIP_GPSTIME_MULTI * context.last_gpstime_diff[context.last], curr_gpstime_diff, 4);
            context.multi_extreme_counter[context.last]++;
            if (context.multi_extreme_counter[context.last] > 3) {
              context.last_gpstime_diff[context.last] = curr_gpstime_diff;
              context.multi_extreme_counter[context.last] = 0;
            }
          }
        } else if (multi < 0) {
          if (multi > LASZIP_GPSTIME_MULTI_MINUS) {
            this.enc_gps_time.encodeSymbol(context.m_gpstime_multi, LASZIP_GPSTIME_MULTI - multi);
            context.ic_gpstime.compress(multi * context.last_gpstime_diff[context.last],
              curr_gpstime_diff, 5);
          } else {
            this.enc_gps_time.encodeSymbol(context.m_gpstime_multi,
              LASZIP_GPSTIME_MULTI - LASZIP_GPSTIME_MULTI_MINUS);
            context.ic_gpstime.compress(
              LASZIP_GPSTIME_MULTI_MINUS * context.last_gpstime_diff[context.last],
              curr_gpstime_diff, 6);
            context.multi_extreme_counter[context.last]++;
            if (context.multi_extreme_counter[context.last] > 3) {
              context.last_gpstime_diff[context.last] = curr_gpstime_diff;
              context.multi_extreme_counter[context.last] = 0;
            }
          }
        } else {
          this.enc_gps_time.encodeSymbol(context.m_gpstime_multi, 0);
          context.ic_gpstime.compress(0, curr_gpstime_diff, 7);
          context.multi_extreme_counter[context.last]++;
          if (context.multi_extreme_counter[context.last] > 3) {
            context.last_gpstime_diff[context.last] = curr_gpstime_diff;
            context.multi_extreme_counter[context.last] = 0;
          }
        }
      } else {
        for (int i = 1; i < 4; i++) {
          final long other_gpstime_diff_64 = gpsTime - context.last_gpstime[context.last + i & 3];
          final int other_gpstime_diff = (int)other_gpstime_diff_64;
          if (other_gpstime_diff_64 == other_gpstime_diff) {
            this.enc_gps_time.encodeSymbol(context.m_gpstime_multi,
              LASZIP_GPSTIME_MULTI_CODE_FULL + i);
            context.last = context.last + i & 3;
            writeGpsTime(gpsTime);
            return;
          }
        }
        this.enc_gps_time.encodeSymbol(context.m_gpstime_multi, LASZIP_GPSTIME_MULTI_CODE_FULL);
        context.ic_gpstime.compress((int)(context.last_gpstime[context.last] >>> 32),
          (int)(gpsTime >>> 32), 8);
        this.enc_gps_time.writeInt((int)gpsTime);
        context.next = context.next + 1 & 3;
        context.last = context.next;
        context.last_gpstime_diff[context.last] = 0;
        context.multi_extreme_counter[context.last] = 0;
      }
      context.last_gpstime[context.last] = gpsTime;
    }
  }

  public int writeInit(final LasPoint point) {
    this.enc_channel_returns_XY.init();
    this.enc_Z.init();
    this.enc_classification.init();
    this.enc_flags.init();
    this.enc_intensity.init();
    this.enc_scan_angle.init();
    this.enc_user_data.init();
    this.enc_point_source.init();
    this.enc_gps_time.init();

    this.changed_classification = false;
    this.changed_flags = false;
    this.changed_intensity = false;
    this.changed_scan_angle = false;
    this.changed_user_data = false;
    this.changed_point_source = false;
    this.changed_gps_time = false;

    /* mark the four scanner channel contexts as unused */

    for (final LasZipContextPoint14 context : this.contexts) {
      context.unused = true;
    }

    this.current_context = point.getScannerChannel();

    writeInitContext(this.current_context, point);
    return this.current_context;
  }

  private void writeInitContext(final int contextIndex, final LasPoint point) {
    initContext(contextIndex, point, this.enc_channel_returns_XY, this.enc_Z, this.enc_intensity,
      this.enc_scan_angle, this.enc_point_source, this.enc_gps_time);

  }

}
