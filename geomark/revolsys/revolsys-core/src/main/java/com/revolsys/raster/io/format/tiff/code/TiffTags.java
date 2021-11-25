package com.revolsys.raster.io.format.tiff.code;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.io.channels.DataReader;

public class TiffTags {

  private static final Map<Integer, TiffTag> TAG_BY_ID = new HashMap<>();

  static {
    for (final TiffTag tag : TiffBaselineTag.values()) {
      addTag(tag);
    }
    for (final TiffTag tag : TiffExtensionTag.values()) {
      addTag(tag);
    }
    for (final TiffTag tag : TiffPrivateTag.values()) {
      addTag(tag);
    }
  }

  static void addTag(final TiffTag tag) {
    TAG_BY_ID.put(tag.getId(), tag);
  }

  public static TiffTag getTag(final int tag) {
    final TiffTag tiffTag = TAG_BY_ID.get(tag);
    if (tiffTag == null) {
      return new TiffCustomTag(tag);
    }
    return tiffTag;
  }

  public static TiffTag readTag(final DataReader in) {
    final int tag = in.getUnsignedShort();
    return getTag(tag);
  }
}
