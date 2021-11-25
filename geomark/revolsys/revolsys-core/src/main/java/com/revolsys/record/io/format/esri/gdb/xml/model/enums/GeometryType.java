package com.revolsys.record.io.format.esri.gdb.xml.model.enums;

import org.jeometry.common.data.type.DataType;

import com.revolsys.geometry.model.GeometryDataTypes;

public enum GeometryType {
  esriGeometryAny(GeometryDataTypes.GEOMETRY), //
  esriGeometryBag(GeometryDataTypes.GEOMETRY_COLLECTION), //
  esriGeometryBezier3Curve(null), //
  esriGeometryCircularArc(null), //
  esriGeometryEllipticArc(null), //
  esriGeometryEnvelope(GeometryDataTypes.BOUNDING_BOX), //
  esriGeometryLine(GeometryDataTypes.LINE_STRING), //
  esriGeometryMultiPatch(null), //
  esriGeometryMultipoint(GeometryDataTypes.MULTI_POINT), //
  esriGeometryNull(null), //
  esriGeometryPath(GeometryDataTypes.LINE_STRING), //
  esriGeometryPoint(GeometryDataTypes.POINT), //
  esriGeometryPolygon(GeometryDataTypes.MULTI_POLYGON), //
  esriGeometryPolyline(GeometryDataTypes.MULTI_LINE_STRING), //
  esriGeometryRay(null), //
  esriGeometryRing(GeometryDataTypes.LINEAR_RING), //
  esriGeometrySphere(null), //
  esriGeometryTriangleFan(null), //
  esriGeometryTriangles(null), //
  esriGeometryTriangleStrip(null);

  private DataType dataType;

  private GeometryType(final DataType dataType) {
    this.dataType = dataType;
  }

  public DataType getDataType() {
    return this.dataType;
  }
}
