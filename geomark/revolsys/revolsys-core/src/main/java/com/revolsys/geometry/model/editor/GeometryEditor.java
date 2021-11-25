package com.revolsys.geometry.model.editor;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataType;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.vertex.Vertex;

public interface GeometryEditor<GE extends GeometryEditor<?>> extends Geometry {

  static LinearRingEditor newLinearRing(final GeometryFactoryProxy factory) {
    final GeometryFactory geometryFactory = factory.getGeometryFactory();
    return new LinearRingEditor(geometryFactory);
  }

  static LineStringEditor newLineString(final GeometryFactoryProxy factory) {
    final GeometryFactory geometryFactory = factory.getGeometryFactory();
    return new LineStringEditor(geometryFactory);
  }

  static MultiLineStringEditor newMultiLineString(final GeometryFactoryProxy factory) {
    final GeometryFactory geometryFactory = factory.getGeometryFactory();
    return new MultiLineStringEditor(geometryFactory);
  }

  static MultiPointEditor newMultiPoint(final GeometryFactoryProxy factory) {
    final GeometryFactory geometryFactory = factory.getGeometryFactory();
    return new MultiPointEditor(geometryFactory);
  }

  static MultiPolygonEditor newMultiPolygon(final GeometryFactoryProxy factory) {
    final GeometryFactory geometryFactory = factory.getGeometryFactory();
    return new MultiPolygonEditor(geometryFactory);
  }

  static PointEditor newPoint(final GeometryFactoryProxy factory) {
    final GeometryFactory geometryFactory = factory.getGeometryFactory();
    return new PointEditor(geometryFactory);
  }

  static PolygonEditor newPolygon(final GeometryFactoryProxy factory) {
    final GeometryFactory geometryFactory = factory.getGeometryFactory();
    return new PolygonEditor(geometryFactory);
  }

  default GeometryEditor<?> appendVertex(final int[] geometryId,
    final GeometryDataType<?, ?> partDataType, final Point point) {
    return appendVertex(geometryId, point);
  }

  GeometryEditor<?> appendVertex(int[] geometryId, Point point);

  @Override
  default <V extends Geometry> V convertGeometry(final GeometryFactory geometryFactory) {
    final Geometry geometry = getCurrentGeometry();
    if (geometry == this) {
      return Geometry.super.convertGeometry(geometryFactory);
    } else {
      return geometry.convertGeometry(geometryFactory);
    }
  }

  GeometryEditor<?> deleteVertex(int[] vertexId);

  Iterable<GE> editors();

  default boolean equalsVertex(final int axisCount, final int vertexIndex, final Point point) {
    return false;
  }

  boolean equalsVertex(int axisCount, int[] geometryId, int vertexIndex, Point point);

  default boolean equalsVertex(final int axisCount, final int[] vertexId, final Point point) {
    if (vertexId != null && vertexId.length > 0) {
      final int vertexIndex = vertexId[vertexId.length - 1];
      final GeometryEditor<?> geometryEditor = getGeometryEditor(vertexId, 0, vertexId.length - 1);
      if (geometryEditor != null) {
        return geometryEditor.equalsVertex(axisCount, vertexIndex, point);
      }
    }
    return false;
  }

  default boolean equalsVertex(final int[] vertexId, final Point point) {
    final int axisCount = getAxisCount();
    return equalsVertex(axisCount, vertexId, point);
  }

  Geometry getCurrentGeometry();

  default int[] getFirstGeometryId() {
    return new int[0];
  }

  default GeometryEditor<?> getGeometryEditor(final int[] geometryId, final int idOffset) {
    final int idLength = geometryId.length;
    return getGeometryEditor(geometryId, idOffset, idLength);
  }

  default GeometryEditor<?> getGeometryEditor(final int[] geometryId, final int idOffset,
    final int idLength) {
    return null;
  }

  default GeometryDataType<?, ?> getPartDataType() {
    return null;
  }

  default int getVertexCount(final int[] geometryId) {
    final int idLength = geometryId.length;
    return getVertexCount(geometryId, idLength);
  }

  int getVertexCount(int[] geometryId, int idLength);

  GeometryEditor<?> insertVertex(int[] vertexId, Point newPoint);

  boolean isModified();

  default GeometryEditor<?> move(final double deltaX, final double deltaY) {
    for (final Vertex vertex : vertices()) {
      double x = vertex.getX();
      x += deltaX;
      vertex.setX(x);

      double y = vertex.getY();
      y += deltaY;
      vertex.setY(y);
    }
    return this;
  }

  Geometry newGeometry();

  /**
   * Construct a new geometry from the geometry editor. The result may be any geometry.
   * For example a {@link LineStringEditor} with vertexCount == 1 would return a Point.
   *
   * @return
   */
  <GA extends Geometry> GA newGeometryAny();

  void removeGeometry(int partIndex);

  void revertChanges();

  GeometryEditor<?> setAxisCount(int axisCount);

  GeometryEditor<?> setCoordinate(int[] vertexId, int axisIndex, double coordinate);

  GeometryEditor<?> setM(int[] vertexId, double m);

  GeometryEditor<?> setVertex(int[] vertexId, Point newPoint);

  GeometryEditor<?> setX(int[] vertexId, double x);

  GeometryEditor<?> setY(int[] vertexId, double y);

  GeometryEditor<?> setZ(int[] vertexId, double z);
}
