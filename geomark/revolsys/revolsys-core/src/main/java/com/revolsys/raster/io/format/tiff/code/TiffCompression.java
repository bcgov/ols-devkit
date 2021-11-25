package com.revolsys.raster.io.format.tiff.code;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * https://www.awaresystems.be/imaging/tiff/tifftags/compression.html
 */
public enum TiffCompression {
  NONE(1), //
  CIIT_RLE(2), //
  CIIT_GROUP_3_FAX(3), //
  CIIT_GROUP_4_FAX(4), //
  LZW(5), //
  JPEG_OLD(6), //
  JPEG(7), //
  ADOBE_DEFLATE(8), //
  NEXT(32766), //
  CCITTRLEW(32771), //
  PACKBITS(32773), //
  THUNDERSCAN(32809), //
  IT8CTPAD(32895), //
  IT8LW(32896), //
  IT8MP(32897), //
  IT8BL(32898), //
  PIXARFILM(32908), //
  PIXARLOG(32909), //
  DEFLATE(32946), //
  DCS(32947), //
  JBIG(34661), //
  SGILOG(34676), //
  SGILOG24(34677), //
  JP2000(34712) //
  ;

  private static final Map<Integer, TiffCompression> ENUM_BY_ID = new HashMap<>();

  static {
    for (final TiffCompression value : values()) {
      ENUM_BY_ID.put(value.id, value);
    }
  }

  public static TiffCompression getById(final int id) {
    return ENUM_BY_ID.get(id);
  }

  private int id;

  private TiffCompression(final int id) {
    this.id = id;
  }

  public int getId() {
    return this.id;
  }

}
