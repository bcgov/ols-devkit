package com.revolsys.elevation.tin;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;

public interface TinBuilder extends TriangulatedIrregularNetwork {

  @Override
  void forEachTriangle(TriangleConsumer action);

  @Override
  BoundingBox getBoundingBox();

  @Override
  GeometryFactory getGeometryFactory();

  Point insertVertex(double x, double y, double z);

  default void insertVertex(final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    final double z = point.getZ();
    insertVertex(x, y, z);
  }

  default void insertVertices(final Iterable<? extends Point> points) {
    for (final Point point : points) {
      insertVertex(point);
    }
  }

  default void insertVertices(final LineString line) {
    final int vertexCount = line.getVertexCount();
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      final double x = line.getX(vertexIndex);
      final double y = line.getY(vertexIndex);
      final double z = line.getZ(vertexIndex);
      insertVertex(x, y, z);
    }
  }

  TriangulatedIrregularNetwork newTriangulatedIrregularNetwork();
}
