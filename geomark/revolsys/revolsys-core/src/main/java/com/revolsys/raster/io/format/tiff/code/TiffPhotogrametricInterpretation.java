package com.revolsys.raster.io.format.tiff.code;

import java.util.HashMap;
import java.util.Map;

public enum TiffPhotogrametricInterpretation {
  MIN_IS_WHITE(0), //
  MIN_IS_BLACK(1), //
  RGB(2), //
  PALETTE(3), //
  MASK(4), //
  CMYK(5), //
  YCBCR(6), //
  CIELAB(8), //
  ICCLAB(9), //
  ITULAB(10), //
  LOGL(32844), //
  LOGLUV(32845) //
  ;

  private static final Map<Integer, TiffPhotogrametricInterpretation> ENUM_BY_ID = new HashMap<>();

  static {
    for (final TiffPhotogrametricInterpretation value : values()) {
      ENUM_BY_ID.put(value.id, value);
    }
  }

  public static TiffPhotogrametricInterpretation getById(final int id) {
    return ENUM_BY_ID.get(id);
  }

  private int id;

  private TiffPhotogrametricInterpretation(final int id) {
    this.id = id;
  }

  public int getId() {
    return this.id;
  }

}
