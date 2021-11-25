package com.revolsys.raster.io.format.tiff.code;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jeometry.coordinatesystem.model.ParameterName;
import org.jeometry.coordinatesystem.model.ParameterNames;
import org.jeometry.coordinatesystem.model.ParameterValue;

import com.revolsys.collection.map.IntHashMap;
import com.revolsys.raster.io.format.tiff.TiffImageFactory;

public enum GeoTiffKeyProjectionParameterName implements GeoTiffKey {
  ProjStdParallel1GeoKey(3078, ParameterNames.STANDARD_PARALLEL_1), //
  ProjStdParallel2GeoKey(3079, ParameterNames.STANDARD_PARALLEL_2), //
  ProjNatOriginLongGeoKey(3080, ParameterNames.LONGITUDE_OF_NATURAL_ORIGIN), //
  ProjNatOriginLatGeoKey(3081, ParameterNames.LATITUDE_OF_NATURAL_ORIGIN), //
  ProjFalseEastingGeoKey(3082, ParameterNames.FALSE_EASTING), //
  ProjFalseNorthingGeoKey(3083, ParameterNames.FALSE_NORTHING), //
  ProjFalseOriginLongGeoKey(3084, ParameterNames.LONGITUDE_OF_FALSE_ORIGIN), //
  ProjFalseOriginLatGeoKey(3085, ParameterNames.LATITUDE_OF_FALSE_ORIGIN), //
  ProjFalseOriginEastingGeoKey(3086, ParameterNames.EASTING_AT_FALSE_ORIGIN), //
  ProjFalseOriginNorthingGeoKey(3087, ParameterNames.NORTHING_AT_FALSE_ORIGIN), //
  ProjCenterLongGeoKey(3088, ParameterNames.LONGITUDE_OF_CENTER), //
  ProjCenterLatGeoKey(3089, ParameterNames.LATITUDE_OF_CENTER), //
  ProjCenterEastingGeoKey(3090, ParameterNames.EASTING_AT_PROJECTION_CENTRE), //
  ProjCenterNorthingGeoKey(3091, ParameterNames.NORTHING_AT_PROJECTION_CENTRE), //
  ProjScaleAtNatOriginGeoKey(3092, ParameterNames.SCALE_FACTOR_AT_NATURAL_ORIGIN), //
  ProjScaleAtCenterGeoKey(3093, ParameterNames.SCALE_FACTOR), //
  ProjAzimuthAngleGeoKey(3094, ParameterNames.AZIMUTH); //
  // ProjStraightVertPoleLongGeoKey(3095, ParameterNames.);

  private static IntHashMap<GeoTiffKeyProjectionParameterName> valueByCode = new IntHashMap<>();

  private static Map<ParameterName, GeoTiffKeyProjectionParameterName> valueByParameterName = new HashMap<>();

  static {
    for (final GeoTiffKeyProjectionParameterName value : GeoTiffKeyProjectionParameterName
      .values()) {
      final int code = value.getId();
      valueByCode.put(code, value);
      final ParameterName parameterName = value.getParameterName();
      valueByParameterName.put(parameterName, value);
    }
  }

  public static GeoTiffKeyProjectionParameterName getById(final ParameterName parameterName) {
    return valueByParameterName.get(parameterName);
  }

  public static GeoTiffKeyProjectionParameterName getCode(final int code) {
    return valueByCode.get(code);
  }

  public static Map<ParameterName, ParameterValue> getProjectionParameters(
    final Map<GeoTiffKey, Object> geoKeys) {
    final Map<ParameterName, ParameterValue> parameters = new LinkedHashMap<>();
    for (final GeoTiffKeyProjectionParameterName tiffParam : values()) {
      final ParameterName parameterName = tiffParam.getParameterName();
      TiffImageFactory.addDoubleParameter(parameters, parameterName, geoKeys, tiffParam);
    }
    return parameters;
  }

  private int id;

  private ParameterName parameterName;

  private GeoTiffKeyProjectionParameterName(final int code, final ParameterName parameterName) {
    this.id = code;
    this.parameterName = parameterName;
  }

  @Override
  public int getId() {
    return this.id;
  }

  public ParameterName getParameterName() {
    return this.parameterName;
  }
}
