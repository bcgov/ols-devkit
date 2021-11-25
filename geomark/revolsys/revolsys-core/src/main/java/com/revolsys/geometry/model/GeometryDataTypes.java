package com.revolsys.geometry.model;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.FunctionDataType;

import com.revolsys.geometry.model.editor.GeometryCollectionImplEditor;
import com.revolsys.geometry.model.editor.LineStringEditor;
import com.revolsys.geometry.model.editor.LinearRingEditor;
import com.revolsys.geometry.model.editor.MultiLineStringEditor;
import com.revolsys.geometry.model.editor.MultiPointEditor;
import com.revolsys.geometry.model.editor.MultiPolygonEditor;
import com.revolsys.geometry.model.editor.PointEditor;
import com.revolsys.geometry.model.editor.PolygonEditor;
import com.revolsys.record.RecordDataType;

public class GeometryDataTypes {

  public static final DataType BOUNDING_BOX = new FunctionDataType("boolean", BoundingBox.class,
    BoundingBox::bboxGet);

  public static final GeometryDataType<Geometry, GeometryCollectionImplEditor> GEOMETRY = new GeometryDataType<>(
    Geometry.class, value -> Geometry.newGeometry(value),
    value -> new GeometryCollectionImplEditor(value));

  public static final GeometryDataType<GeometryCollection, GeometryCollectionImplEditor> GEOMETRY_COLLECTION = new GeometryDataType<>(
    GeometryCollection.class, value -> GeometryCollection.newGeometryCollection(value),
    value -> new GeometryCollectionImplEditor(value));

  public static final DataType GEOMETRY_FACTORY = new FunctionDataType("GeometryFactory",
    GeometryFactory.class, value -> GeometryFactory.newGeometryFactory(value));

  public static final GeometryDataType<LineString, LineStringEditor> LINE_STRING = new GeometryDataType<>(
    LineString.class, value -> LineString.newLineString(value),
    value -> new LineStringEditor(value));

  public static final GeometryDataType<LinearRing, LinearRingEditor> LINEAR_RING = new GeometryDataType<>(
    LinearRing.class, value -> LinearRing.newLinearRing(value),
    value -> new LinearRingEditor(value));

  public static final GeometryDataType<MultiLineString, MultiLineStringEditor> MULTI_LINE_STRING = new GeometryDataType<>(
    MultiLineString.class, value -> Lineal.newLineal(value),
    value -> new MultiLineStringEditor(value));

  public static final GeometryDataType<MultiPoint, MultiPointEditor> MULTI_POINT = new GeometryDataType<>(
    MultiPoint.class, value -> Punctual.newPunctual(value), value -> new MultiPointEditor(value));

  public static final GeometryDataType<MultiPolygon, MultiPolygonEditor> MULTI_POLYGON = new GeometryDataType<>(
    MultiPolygon.class, value -> Polygonal.newPolygonal(value),
    value -> new MultiPolygonEditor(value));

  public static final GeometryDataType<Point, PointEditor> POINT = new GeometryDataType<>(
    Point.class, value -> Point.newPoint(value), value -> new PointEditor(value));

  public static final GeometryDataType<Polygon, PolygonEditor> POLYGON = new GeometryDataType<>(
    Polygon.class, value -> Polygon.newPolygon(value), value -> new PolygonEditor(value));

  public static final DataType RECORD = new RecordDataType();

  private GeometryDataTypes() {
  }

}
