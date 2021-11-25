package org.jeometry.coordinatesystem.model;

public interface NormalizedParameterNames {

  ParameterName AZIMUTH = new MultiParameterName(ParameterNames.AZIMUTH,
    ParameterNames.AZIMUTH_OF_INITIAL_LINE);

  ParameterName CENTRAL_MERIDIAN = new MultiParameterName(ParameterNames.CENTRAL_MERIDIAN,
    ParameterNames.LONGITUDE_OF_CENTER, ParameterNames.LONGITUDE_OF_ORIGIN,
    ParameterNames.LONGITUDE_OF_FALSE_ORIGIN, ParameterNames.LONGITUDE_OF_NATURAL_ORIGIN);

  ParameterName FALSE_EASTING = new MultiParameterName(ParameterNames.FALSE_EASTING,
    ParameterNames.EASTING_AT_FALSE_ORIGIN, ParameterNames.EASTING_AT_PROJECTION_CENTRE);

  ParameterName FALSE_NORTHING = new MultiParameterName(ParameterNames.FALSE_NORTHING,
    ParameterNames.NORTHING_AT_FALSE_ORIGIN, ParameterNames.NORTHING_AT_PROJECTION_CENTRE);

  ParameterName LATITUDE_OF_CENTRE = new MultiParameterName(ParameterNames.LATITUDE_OF_CENTER,
    ParameterNames.LATITUDE_OF_PROJECTION_CENTRE);

  ParameterName LATITUDE_OF_ORIGIN = new MultiParameterName(ParameterNames.LATITUDE_OF_ORIGIN,
    ParameterNames.LATITUDE_OF_NATURAL_ORIGIN, ParameterNames.LATITUDE_OF_CENTER,
    ParameterNames.LATITUDE_OF_FALSE_ORIGIN);

  ParameterName LONGITUDE_OF_CENTRE = new MultiParameterName(ParameterNames.LONGITUDE_OF_CENTER,
    ParameterNames.LONGITUDE_OF_PROJECTION_CENTRE, ParameterNames.LONGITUDE_OF_ORIGIN);

  ParameterName RECTIFIED_GRID_ANGLE = new MultiParameterName(ParameterNames.RECTIFIED_GRID_ANGLE,
    ParameterNames.ANGLE_FROM_RECTIFIED_TO_SKEW_GRID);

  ParameterName SCALE_FACTOR = new MultiParameterName(ParameterNames.SCALE_FACTOR,
    ParameterNames.SCALE_FACTOR_AT_NATURAL_ORIGIN, ParameterNames.SCALE_FACTOR_ON_INITIAL_LINE,
    ParameterNames.SCALE_FACTOR_ON_PSEUDO_STANDARD_PARALLEL);

  ParameterName STANDARD_PARALLEL_1 = new MultiParameterName(ParameterNames.STANDARD_PARALLEL_1,
    ParameterNames.LATITUDE_OF_1ST_STANDARD_PARALLEL);

  ParameterName STANDARD_PARALLEL_2 = new MultiParameterName(ParameterNames.STANDARD_PARALLEL_2,
    ParameterNames.LATITUDE_OF_2ND_STANDARD_PARALLEL);

  static void init() {
  }
}
