package com.revolsys.raster.io.format.tiff.code;

import java.util.Map;

import com.revolsys.collection.map.Maps;

public interface GeoTiffKey {
  int getId();

  default int getInteger(final Map<GeoTiffKey, Object> map, final int defaultValue) {
    return Maps.getInteger(map, this, defaultValue);
  }
}
