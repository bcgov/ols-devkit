package org.jeometry.coordinatesystem.model;

import java.util.HashMap;
import java.util.Map;

import org.jeometry.coordinatesystem.model.unit.UnitsOfMeasure;

public class ParameterNames {

  protected static final Map<String, ParameterName> _PARAMETER_BY_NAME = new HashMap<>();

  protected static final Map<Integer, ParameterName> _PARAMETER_BY_ID = new HashMap<>();

  public static final ParameterName ANGLE_FROM_RECTIFIED_TO_SKEW_GRID = new SingleParameterName(
    8814, "angle_from_rectified_to_skew_grid");

  public static final ParameterName AZIMUTH = new SingleParameterName("azimuth");

  public static final ParameterName AZIMUTH_OF_INITIAL_LINE = new SingleParameterName(8813,
    "azimuth_of_initial_line");

  public static final ParameterName CENTRAL_MERIDIAN = new SingleParameterName("central_meridian",
    UnitsOfMeasure.DEGREE);

  public static final ParameterName COLATITUDE_OF_CONE_AXIS = new SingleParameterName(1036,
    "colatitude_of_cone_axis");

  public static final ParameterName EASTING_AT_FALSE_ORIGIN = new SingleParameterName(8826,
    "easting_at_false_origin", UnitsOfMeasure.METRE);

  public static final ParameterName EASTING_AT_PROJECTION_CENTRE = new SingleParameterName(8816,
    "easting_at_projection_centre", UnitsOfMeasure.METRE);

  public static final ParameterName FALSE_EASTING = new SingleParameterName(8806, "false_easting",
    UnitsOfMeasure.METRE);

  public static final ParameterName FALSE_NORTHING = new SingleParameterName(8807, "false_northing",
    UnitsOfMeasure.METRE);

  public static final ParameterName INITIAL_LONGITUDE = new SingleParameterName(8830,
    "initial_longitude", UnitsOfMeasure.DEGREE);

  public static final SingleParameterName LATITUDE_OF_1ST_STANDARD_PARALLEL = new SingleParameterName(
    8823, "latitude_of_1st_standard_parallel", UnitsOfMeasure.DEGREE);

  public static ParameterName LATITUDE_OF_2ND_STANDARD_PARALLEL = new SingleParameterName(8824,
    "latitude_of_2nd_standard_parallel", UnitsOfMeasure.DEGREE);

  public static final ParameterName LATITUDE_OF_CENTER = new SingleParameterName(
    "latitude_of_center", UnitsOfMeasure.DEGREE);

  public static final ParameterName LATITUDE_OF_FALSE_ORIGIN = new SingleParameterName(8821,
    "latitude_of_false_origin", UnitsOfMeasure.DEGREE);

  public static final ParameterName LATITUDE_OF_NATURAL_ORIGIN = new SingleParameterName(
    "latitude_of_natural_origin", UnitsOfMeasure.DEGREE);

  public static final ParameterName LATITUDE_OF_ORIGIN = new SingleParameterName(
    "latitude_of_origin", UnitsOfMeasure.DEGREE);

  public static final ParameterName LATITUDE_OF_PROJECTION_CENTRE = new SingleParameterName(8811,
    "latitude_of_projection_centre", UnitsOfMeasure.DEGREE);

  public static final ParameterName LATITUDE_OF_PSEUDO_STANDARD_PARALLEL = new SingleParameterName(
    8818, "latitude_of_pseudo_standard_parallel", UnitsOfMeasure.DEGREE);

  public static ParameterName LATITUDE_OF_STANDARD_PARALLEL = new SingleParameterName(8832,
    "latitude_of_standard_parallel", UnitsOfMeasure.DEGREE);

  public static final ParameterName LONGITUDE_OF_CENTER = new SingleParameterName(
    "longitude_of_center", UnitsOfMeasure.DEGREE);

  public static final ParameterName LONGITUDE_OF_FALSE_ORIGIN = new SingleParameterName(8822,
    "longitude_of_false_origin", UnitsOfMeasure.DEGREE);

  public static ParameterName LONGITUDE_OF_NATURAL_ORIGIN = new SingleParameterName(8802,
    "longitude_of_natural_origin", UnitsOfMeasure.DEGREE);

  public static ParameterName LONGITUDE_OF_ORIGIN = new SingleParameterName(8833,
    "longitude_of_origin", UnitsOfMeasure.DEGREE);

  public static ParameterName LONGITUDE_OF_PROJECTION_CENTRE = new SingleParameterName(8812,
    "longitude_of_projection_centre", UnitsOfMeasure.DEGREE);

  public static final ParameterName NORTHING_AT_FALSE_ORIGIN = new SingleParameterName(8827,
    "northing_at_false_origin", UnitsOfMeasure.METRE);

  public static final ParameterName NORTHING_AT_PROJECTION_CENTRE = new SingleParameterName(8817,
    "northing_at_projection_centre", UnitsOfMeasure.METRE);

  public static final ParameterName PSEUDO_STANDARD_PARALLEL_1 = new SingleParameterName(
    "pseudo_standard_parallel_1", UnitsOfMeasure.DEGREE);

  public static final ParameterName RECTIFIED_GRID_ANGLE = new SingleParameterName(
    "rectified_grid_angle");

  public static final ParameterName SCALE_FACTOR = new SingleParameterName("scale_factor");

  public static final ParameterName SCALE_FACTOR_AT_NATURAL_ORIGIN = new SingleParameterName(8805,
    "scale_factor_at_natural_origin");

  public static final ParameterName SCALE_FACTOR_ON_INITIAL_LINE = new SingleParameterName(8815,
    "scale_factor_on_initial_line");

  public static final ParameterName SCALE_FACTOR_ON_PSEUDO_STANDARD_PARALLEL = new SingleParameterName(
    8819, "scale_factor_on_pseudo_standard_parallel");

  public static final ParameterName SPHERICAL_LATITUDE_OF_ORIGIN = new SingleParameterName(8828,
    "spherical_latitude_of_origin", UnitsOfMeasure.DEGREE);

  public static final ParameterName SPHERICAL_LONGITUDE_OF_ORIGIN = new SingleParameterName(8829,
    "spherical_longitude_of_origin", UnitsOfMeasure.DEGREE);

  public static final ParameterName STANDARD_PARALLEL_1 = new SingleParameterName(
    "standard_parallel_1", UnitsOfMeasure.DEGREE);

  public static final ParameterName STANDARD_PARALLEL_2 = new SingleParameterName(
    "standard_parallel_2", UnitsOfMeasure.DEGREE);

  static {
    NormalizedParameterNames.init();
  }

  public static ParameterName getParameterName(final int id, String name) {
    ParameterName parameterName = _PARAMETER_BY_ID.get(id);
    if (parameterName == null) {
      name = normalizeName(name);
      parameterName = _PARAMETER_BY_NAME.get(name);
      if (parameterName == null) {
        parameterName = new SingleParameterName(id, name);
      }
    }
    return parameterName;
  }

  public static final ParameterName getParameterName(String name) {
    name = normalizeName(name);
    ParameterName parameterName = _PARAMETER_BY_NAME.get(name);
    if (parameterName == null) {
      parameterName = new SingleParameterName(name);
    }
    return parameterName;
  }

  public static String normalizeName(final String name) {
    return name.trim().toLowerCase().replace(' ', '_').replaceAll("[^a-z0-9_]", "");
  }

  private ParameterNames() {
  }
}
