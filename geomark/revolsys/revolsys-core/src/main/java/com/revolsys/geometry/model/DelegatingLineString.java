package com.revolsys.geometry.model;

import java.util.function.Consumer;

import org.jeometry.common.function.BiConsumerDouble;
import org.jeometry.common.function.BiFunctionDouble;
import org.jeometry.common.function.Consumer3Double;
import org.jeometry.common.function.Consumer4Double;
import org.jeometry.common.function.Function4Double;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

public interface DelegatingLineString extends LineString {
  @Override
  default double distancePoint(final double x, final double y, final double terminateDistance) {
    final LineString line = getLineString();
    return line.distancePoint(x, y, terminateDistance);
  }

  @Override
  default boolean equalsVertex2d(final int vertexIndex, final double x, final double y) {
    final LineString line = getLineString();
    return line.equalsVertex2d(vertexIndex, x, y);
  }

  @Override
  default boolean equalsVertex2d(final int vertexIndex1, final int vertexIndex2) {
    final LineString line = getLineString();
    return line.equalsVertex2d(vertexIndex1, vertexIndex2);
  }

  @Override
  default <R> R findSegment(final Function4Double<R> action) {
    final LineString line = getLineString();
    return line.findSegment(action);
  }

  @Override
  default <R> R findVertex(final BiFunctionDouble<R> action) {
    final LineString line = getLineString();
    return line.findVertex(action);
  }

  @Override
  default void forEachSegment(final Consumer4Double action) {
    final LineString line = getLineString();
    line.forEachSegment(action);
  }

  @Override
  default void forEachVertex(final BiConsumerDouble action) {
    final LineString line = getLineString();
    line.forEachVertex(action);
  }

  @Override
  default void forEachVertex(final Consumer3Double action) {
    final LineString line = getLineString();
    line.forEachVertex(action);
  }

  @Override
  default void forEachVertex(final CoordinatesOperation coordinatesOperation,
    final CoordinatesOperationPoint point, final Consumer<CoordinatesOperationPoint> action) {
    final LineString line = getLineString();
    line.forEachVertex(coordinatesOperation, point, action);
  }

  @Override
  default void forEachVertex(final CoordinatesOperationPoint coordinates,
    final Consumer<CoordinatesOperationPoint> action) {
    final LineString line = getLineString();
    line.forEachVertex(coordinates, action);
  }

  @Override
  default int getAxisCount() {
    final LineString line = getLineString();
    return line.getAxisCount();
  }

  @Override
  default BoundingBox getBoundingBox() {
    final LineString line = getLineString();
    return line.getBoundingBox();
  }

  @Override
  default double getCoordinate(final int vertexIndex, final int axisIndex) {
    final LineString line = getLineString();
    return line.getCoordinate(vertexIndex, axisIndex);
  }

  @Override
  default double[] getCoordinates() {
    final LineString line = getLineString();
    return line.getCoordinates();
  }

  @Override
  default GeometryFactory getGeometryFactory() {
    final LineString line = getLineString();
    return line.getGeometryFactory();
  }

  @Override
  default double getLength() {
    final LineString line = getLineString();
    if (line == null) {
      return 0;
    } else {
      return line.getLength();
    }
  }

  LineString getLineString();

  @Override
  default double getM(final int vertexIndex) {
    final LineString line = getLineString();
    return line.getM(vertexIndex);
  }

  @Override
  default Point getPoint() {
    final LineString line = getLineString();
    return line.getPoint();
  }

  @Override
  default Point getPoint(final int i) {
    final LineString line = getLineString();
    return line.getPoint(i);
  }

  @Override
  default int getVertexCount() {
    final LineString line = getLineString();
    return line.getVertexCount();
  }

  @Override
  default double getX(final int vertexIndex) {
    final LineString line = getLineString();
    return line.getX(vertexIndex);
  }

  @Override
  default double getY(final int vertexIndex) {
    final LineString line = getLineString();
    return line.getY(vertexIndex);
  }

  @Override
  default double getZ(final int vertexIndex) {
    final LineString line = getLineString();
    return line.getZ(vertexIndex);
  }

  @Override
  default String toWkt() {
    final LineString line = getLineString();
    return line.toWkt();
  }
}
