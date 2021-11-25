package com.revolsys.geometry.model.impl;

import java.util.function.Consumer;

import org.jeometry.common.function.BiConsumerDouble;
import org.jeometry.common.function.BiFunctionDouble;
import org.jeometry.common.function.Consumer3Double;
import org.jeometry.common.function.Consumer4Double;
import org.jeometry.common.function.Function4Double;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.DelegatingLineString;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;

public class AbstractDelegatingLineString extends AbstractLineString
  implements DelegatingLineString {

  private static final long serialVersionUID = 1L;

  protected final LineString line;

  public AbstractDelegatingLineString(final LineString line) {
    this.line = line;
  }

  @Override
  public boolean equalsVertex2d(final int vertexIndex, final double x, final double y) {
    return this.line.equalsVertex2d(vertexIndex, x, y);
  }

  @Override
  public boolean equalsVertex2d(final int vertexIndex1, final int vertexIndex2) {
    return this.line.equalsVertex2d(vertexIndex1, vertexIndex2);
  }

  @Override
  public <R> R findSegment(final Function4Double<R> action) {
    return this.line.findSegment(action);
  }

  @Override
  public <R> R findVertex(final BiFunctionDouble<R> action) {
    return this.line.findVertex(action);
  }

  @Override
  public void forEachSegment(final Consumer4Double action) {
    this.line.forEachSegment(action);
  }

  @Override
  public void forEachVertex(final BiConsumerDouble action) {
    this.line.forEachVertex(action);
  }

  @Override
  public void forEachVertex(final Consumer3Double action) {
    this.line.forEachVertex(action);
  }

  @Override
  public void forEachVertex(final CoordinatesOperation coordinatesOperation,
    final CoordinatesOperationPoint point, final Consumer<CoordinatesOperationPoint> action) {
    this.line.forEachVertex(coordinatesOperation, point, action);
  }

  @Override
  public void forEachVertex(final CoordinatesOperationPoint coordinates,
    final Consumer<CoordinatesOperationPoint> action) {
    this.line.forEachVertex(coordinates, action);
  }

  @Override
  public int getAxisCount() {
    return this.line.getAxisCount();
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.line.getBoundingBox();
  }

  @Override
  public double getCoordinate(final int vertexIndex, final int axisIndex) {
    return this.line.getCoordinate(vertexIndex, axisIndex);
  }

  @Override
  public double[] getCoordinates() {
    return this.line.getCoordinates();
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.line.getGeometryFactory();
  }

  @Override
  public double getLength() {
    return this.line.getLength();
  }

  @Override
  public LineString getLineString() {
    return this.line;
  }

  @Override
  public double getM(final int vertexIndex) {
    return this.line.getM(vertexIndex);
  }

  @Override
  public Point getPoint() {
    return this.line.getPoint();
  }

  @Override
  public Point getPoint(final int i) {
    return this.line.getPoint(i);
  }

  @Override
  public int getVertexCount() {
    return this.line.getVertexCount();
  }

  @Override
  public double getX(final int vertexIndex) {
    return this.line.getX(vertexIndex);
  }

  @Override
  public double getY(final int vertexIndex) {
    return this.line.getY(vertexIndex);
  }

  @Override
  public double getZ(final int vertexIndex) {
    return this.line.getZ(vertexIndex);
  }

  @Override
  public boolean isClosed() {
    return this.line.isClosed();
  }

  @Override
  public String toWkt() {
    return this.line.toWkt();
  }
}
