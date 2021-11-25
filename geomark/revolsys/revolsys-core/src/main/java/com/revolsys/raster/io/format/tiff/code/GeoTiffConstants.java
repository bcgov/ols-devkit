package com.revolsys.raster.io.format.tiff.code;

public interface GeoTiffConstants {

  /** Projection Coordinate System */
  int ModelTypeProjected = 1;

  /** Geographic latitude-longitude System */
  int ModelTypeGeographic = 2;

  /** Geocentric (X,Y,Z) Coordinate System */
  int ModelTypeGeocentric = 3;

  int RasterPixelIsArea = 1;

  int RasterPixelIsPoint = 2;

}
