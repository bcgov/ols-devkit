package com.revolsys.raster.io.format.tiff.code;

import java.util.HashMap;
import java.util.Map;

import org.jeometry.coordinatesystem.model.CoordinateOperationMethod;
import org.jeometry.coordinatesystem.model.ProjectedCoordinateSystem;

import com.revolsys.collection.map.IntHashMap;

public enum GeoTiffCoordinateTransformationCode {
  undefined(0), //
  CT_TransverseMercator(1, CoordinateOperationMethod.TRANSVERSE_MERCATOR), //
  CT_TransvMercator_Modified_Alaska(2), //
  CT_ObliqueMercator(3), //
  CT_ObliqueMercator_Laborde(4), //
  CT_ObliqueMercator_Rosenmund(5), //
  CT_ObliqueMercator_Spherical(6), //
  CT_Mercator(7, CoordinateOperationMethod.MERCATOR), //
  CT_LambertConfConic_2SP(8, CoordinateOperationMethod.LAMBERT_CONIC_CONFORMAL_2SP), //
  CT_LambertConfConic_Helmert(9, CoordinateOperationMethod.LAMBERT_CONIC_CONFORMAL_1SP), //
  CT_LambertAzimEqualArea(10), //
  CT_AlbersEqualArea(11, CoordinateOperationMethod.ALBERS_EQUAL_AREA), //
  CT_AzimuthalEquidistant(12), //
  CT_EquidistantConic(13), //
  CT_Stereographic(14), //
  CT_PolarStereographic(15), //
  CT_ObliqueStereographic(16), //
  CT_Equirectangular(17), //
  CT_CassiniSoldner(18), //
  CT_Gnomonic(19), //
  CT_MillerCylindrical(20), //
  CT_Orthographic(21), //
  CT_Polyconic(22), //
  CT_Robinson(23), //
  CT_Sinusoidal(24), //
  CT_VanDerGrinten(25), //
  CT_NewZealandMapGrid(26), //
  CT_TransvMercator_SouthOriented(27);

  private static IntHashMap<GeoTiffCoordinateTransformationCode> valueByCode = new IntHashMap<>();

  private static Map<String, GeoTiffCoordinateTransformationCode> valueByName = new HashMap<>();

  static {
    for (final GeoTiffCoordinateTransformationCode value : GeoTiffCoordinateTransformationCode
      .values()) {
      final int code = value.getCode();
      valueByCode.put(code, value);
      final String projectionName = value.getProjectionName();
      if (projectionName != null) {
        valueByName.put(projectionName, value);
      }
    }
  }

  public static GeoTiffCoordinateTransformationCode getById(final int code) {
    return valueByCode.getOrDefault(code, undefined);
  }

  public static int getCode(final ProjectedCoordinateSystem projectedCoordinateSystem) {
    final CoordinateOperationMethod coordinateOperationMethod = projectedCoordinateSystem
      .getCoordinateOperationMethod();
    final String methodName = coordinateOperationMethod.getName();
    return valueByName.getOrDefault(methodName, undefined).getCode();
  }

  public static CoordinateOperationMethod getCoordinateOperationMethod(final int code) {
    return getById(code).getCoordinateOperationMethod();
  }

  private int code;

  private CoordinateOperationMethod coordinateOperationMethod;

  private String projectionName;

  private GeoTiffCoordinateTransformationCode(final int code) {
    this.code = code;
  }

  private GeoTiffCoordinateTransformationCode(final int code, final String projectionName) {
    this.code = code;
    this.projectionName = projectionName;
    this.coordinateOperationMethod = new CoordinateOperationMethod(projectionName);
  }

  public int getCode() {
    return this.code;
  }

  public CoordinateOperationMethod getCoordinateOperationMethod() {
    return this.coordinateOperationMethod;
  }

  public String getProjectionName() {
    return this.projectionName;
  }
}
