package com.revolsys.raster.io.format.tiff.code;

import java.util.HashMap;
import java.util.Map;

public enum GeoTiffKeys implements GeoTiffKey {
  Unknown(-1), //
  GTModelTypeGeoKey(1024), //
  GTRasterTypeGeoKey(1025), //
  GTCitationGeoKey(1026), //
  GeographicTypeGeoKey(2048), //
  GeogCitationGeoKey(2049), //
  GeogGeodeticDatumGeoKey(2050), //
  GeogPrimeMeridianGeoKey(2051), //
  GeogLinearUnitsGeoKey(2052), //
  GeogLinearUnitSizeGeoKey(2053), //
  GeogAngularUnitsGeoKey(2054), //
  GeogAngularUnitSizeGeoKey(2055), //
  GeogEllipsoidGeoKey(2056), //
  GeogSemiMajorAxisGeoKey(2057), //
  GeogSemiMinorAxisGeoKey(2058), //
  GeogInvFlatteningGeoKey(2059), //
  GeogAzimuthUnitsGeoKey(2060), //
  GeogPrimeMeridianLongGeoKey(2061), //
  GeogTOWGS84GeoKey(2062), //
  ProjectedCSTypeGeoKey(3072), //
  PCSCitationGeoKey(3073), //
  ProjectionGeoKey(3074), //
  ProjCoordTransGeoKey(3075), //
  ProjLinearUnitsGeoKey(3076), //
  ProjLinearUnitSizeGeoKey(3077), //
  VerticalCSTypeGeoKey(4096), //
  VerticalCitationGeoKey(4097), //
  VerticalDatumGeoKey(4098), //
  VerticalUnitsGeoKey(4099) //
  ;

  private static Map<Integer, GeoTiffKeys> enumById = new HashMap<>();

  static {
    for (final GeoTiffKeys geoKey : values()) {
      enumById.put(geoKey.id, geoKey);
    }
  }

  public static GeoTiffKey getById(final int id) {
    if (id >= 3078 && id <= 3096) {
      return GeoTiffKeyProjectionParameterName.getCode(id);
    } else {
      final GeoTiffKey key = enumById.get(id);
      if (key == null) {
        return new GeoTiffKeyCustom(id);
      } else {
        return key;
      }
    }
  }

  private int id;

  private GeoTiffKeys(final int id) {
    this.id = id;
  }

  @Override
  public int getId() {
    return this.id;
  }

  @Override
  public String toString() {
    return super.toString() + "<" + this.id + ">";
  }
}
