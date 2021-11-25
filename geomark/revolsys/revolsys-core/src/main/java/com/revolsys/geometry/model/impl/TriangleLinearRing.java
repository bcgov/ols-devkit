package com.revolsys.geometry.model.impl;

import org.jeometry.common.function.BiConsumerDouble;
import org.jeometry.common.function.BiFunctionDouble;
import org.jeometry.common.function.Consumer3Double;
import org.jeometry.common.function.Function4Double;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Triangle;

public class TriangleLinearRing extends AbstractLineString implements LinearRing {
  private static final long serialVersionUID = 1L;

  private final Triangle triangle;

  public TriangleLinearRing(final Triangle triangle) {
    this.triangle = triangle;
  }

  @Override
  public TriangleLinearRing clone() {
    return (TriangleLinearRing)super.clone();
  }

  @Override
  public <R> R findSegment(final Function4Double<R> action) {
    if (!isEmpty()) {
      final Triangle triangle = this.triangle;
      final double x1 = triangle.getX(0);
      final double y1 = triangle.getY(0);
      final double x2 = triangle.getX(1);
      final double y2 = triangle.getY(1);
      final double x3 = triangle.getX(2);
      final double y3 = triangle.getY(2);
      R result = action.accept(x1, y1, x2, y2);
      if (result == null) {
        result = action.accept(x2, y2, x3, y3);
        if (result == null) {
          result = action.accept(x3, y3, x1, y1);
        }
      }
    }
    return null;
  }

  @Override
  public <R> R findVertex(final BiFunctionDouble<R> action) {
    if (!isEmpty()) {
      for (int i = 0; i < 3; i++) {
        final double x = getX(i);
        final double y = getY(i);
        final R result = action.accept(x, y);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  @Override
  public void forEachVertex(final BiConsumerDouble action) {
    if (!isEmpty()) {
      final Triangle triangle = this.triangle;
      for (int i = 0; i < 3; i++) {
        final double x = triangle.getX(i);
        final double y = triangle.getY(i);
        action.accept(x, y);
      }
    }
  }

  @Override
  public void forEachVertex(final Consumer3Double action) {
    if (!isEmpty()) {
      final Triangle triangle = this.triangle;
      for (int i = 0; i < 3; i++) {
        final double x = triangle.getX(i);
        final double y = triangle.getY(i);
        final double z = triangle.getZ(i);
        action.accept(x, y, z);
      }
    }
  }

  @Override
  public int getAxisCount() {
    return this.triangle.getAxisCount();
  }

  @Override
  public double getCoordinate(int vertexIndex, final int axisIndex) {
    if (vertexIndex == 3) {
      vertexIndex = 0;
    }
    return this.triangle.getCoordinate(vertexIndex, axisIndex);
  }

  @Override
  public double[] getCoordinates() {
    return this.triangle.getCoordinates();
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.triangle.getGeometryFactory();
  }

  @Override
  public int getVertexCount() {
    return 4;
  }

  @Override
  public LinearRing newGeometry(final GeometryFactory geometryFactory) {
    return LinearRing.super.newGeometry(geometryFactory);
  }
}
