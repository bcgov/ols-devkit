package com.revolsys.elevation.cloud.las.zip;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import com.revolsys.collection.map.IntHashMap;
import com.revolsys.elevation.cloud.las.zip.v1.LasZipItemCodecGpsTime11V1;
import com.revolsys.elevation.cloud.las.zip.v1.LasZipItemCodecPoint10V1;
import com.revolsys.elevation.cloud.las.zip.v1.LasZipItemCodecRgb12V1;
import com.revolsys.elevation.cloud.las.zip.v2.LasZipItemCodecGpsTime11V2;
import com.revolsys.elevation.cloud.las.zip.v2.LasZipItemCodecPoint10V2;
import com.revolsys.elevation.cloud.las.zip.v2.LasZipItemCodecRgb12V2;
import com.revolsys.elevation.cloud.las.zip.v3.LasZipItemCodecByte14V3;
import com.revolsys.elevation.cloud.las.zip.v3.LasZipItemCodecPoint14V3;
import com.revolsys.elevation.cloud.las.zip.v3.LasZipItemCodecRgb14V3;
import com.revolsys.elevation.cloud.las.zip.v3.LasZipItemCodecRgbNir14V3;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingCodec;

public enum LasZipItemType {
  BYTE(0, -1), // Only used for non-standard record sizes. Not supported
  SHORT(1, -1), // Not used
  INT(2, -1), // Not used
  LONG(3, -1), // Not used
  FLOAT(4, -1), // Not used
  DOUBLE(5, -1), // Not used
  POINT10(6, 20), //
  GPSTIME11(7, 8), //
  RGB12(8, 6), //
  WAVEPACKET13(9, 29), //
  POINT14(10, 30), //
  RGB14(11, 6), //
  RGBNIR14(12, 8), //
  WAVEPACKET14(13, 29), //
  BYTE14(14, -1) //
  ;

  private static final Map<Integer, LasZipItemType> TYPES = new HashMap<>();

  static {
    for (final LasZipItemType type : values()) {
      TYPES.put(type.id, type);
    }

    POINT10.addCodec(1, LasZipItemCodecPoint10V1::new);
    POINT10.addCodec(2, LasZipItemCodecPoint10V2::new);

    GPSTIME11.addCodec(1, LasZipItemCodecGpsTime11V1::new);
    GPSTIME11.addCodec(2, LasZipItemCodecGpsTime11V2::new);

    RGB12.addCodec(1, LasZipItemCodecRgb12V1::new);
    RGB12.addCodec(2, LasZipItemCodecRgb12V2::new);

    for (final int version : Arrays.asList(3, 4)) {
      POINT14.addCodec(version, (codec, size) -> new LasZipItemCodecPoint14V3(codec, version));
      RGB14.addCodec(version, (codec, size) -> new LasZipItemCodecRgb14V3(codec, version));
      RGBNIR14.addCodec(version, (codec, size) -> new LasZipItemCodecRgbNir14V3(codec, version));
      BYTE14.addCodec(version, (codec, size) -> new LasZipItemCodecByte14V3(codec, version, size));
    }
  }

  public static LasZipItemType fromId(final int i) {
    return TYPES.get(i);
  }

  private int id;

  private int size;

  private final IntHashMap<BiFunction<ArithmeticCodingCodec, Integer, LasZipItemCodec>> codecByVersion = new IntHashMap<>();

  private LasZipItemType(final int id, final int size) {
    this.id = id;
    this.size = size;
  }

  private void addCodec(final int version,
    final BiFunction<ArithmeticCodingCodec, Integer, LasZipItemCodec> codecConstructor) {
    this.codecByVersion.put(version, codecConstructor);
  }

  public int getId() {
    return this.id;
  }

  public int getSize() {
    return this.size;
  }

  public LasZipItemCodec newCodec(final ArithmeticCodingCodec codec, final int version,
    final int size) {
    final BiFunction<ArithmeticCodingCodec, Integer, LasZipItemCodec> codecConstructor = this.codecByVersion
      .get(version);
    if (codecConstructor == null) {
      throw new IllegalArgumentException(
        "LasZip item type " + name() + " version " + version + " not currently supported");
    } else {
      return codecConstructor.apply(codec, size);
    }
  }
}
