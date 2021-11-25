package com.revolsys.geometry.model.impl;

import org.jeometry.common.function.BiConsumerDouble;

import com.revolsys.geometry.algorithm.CGAlgorithms;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Triangle;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;

public class TriangleDouble extends AbstractTriangle {
  private static final long serialVersionUID = 7579865828939708871L;

  public static Triangle newClockwiseTriangle(final double x1, final double y1, final double x2,
    final double y2, final double x3, final double y3) {
    if (CoordinatesListUtil.orientationIndex(x1, y1, x2, y2, x3, y3) == CGAlgorithms.CLOCKWISE) {
      return new TriangleDouble(x1, y1, x2, y2, x3, y3);
    } else {
      return new TriangleDouble(x1, y1, x3, y3, x2, y2);
    }
  }

  public static Triangle newTriangle(final Point... points) {
    if (points.length != 3) {
      throw new IllegalArgumentException(
        "A traingle must have exeactly 3 points not " + points.length);
    }
    final double[] coordinates = new double[9];
    for (int i = 0; i < 3; i++) {
      final Point point = points[i];
      coordinates[i * 3] = point.getX();
      coordinates[i * 3 + 1] = point.getY();
      coordinates[i * 3 + 2] = point.getZ();
    }
    return new TriangleDouble(coordinates);
  }

  private double[] coordinates;

  public TriangleDouble(final double... coordinates) {
    if (coordinates == null || coordinates.length == 0) {
      throw new IllegalArgumentException("coordinates must be specified");
    } else if (coordinates.length % 3 != 0) {
      throw new IllegalArgumentException("coordinates must be a multiple of 3");
    } else {
      this.coordinates = coordinates;
    }
  }

  @Override
  public TriangleDouble clone() {
    final TriangleDouble clone = (TriangleDouble)super.clone();
    clone.coordinates = this.coordinates.clone();
    return clone;
  }

  @Override
  public void forEachVertex(final BiConsumerDouble action) {
    if (!isEmpty()) {
      final int axisCount = getAxisCount();
      final double x1 = this.coordinates[0];
      final double y1 = this.coordinates[1];
      action.accept(x1, y1);
      final double x2 = this.coordinates[axisCount];
      final double y2 = this.coordinates[axisCount + 1];
      action.accept(x2, y2);
      final double x3 = this.coordinates[axisCount * 2];
      final double y3 = this.coordinates[axisCount * 2 + 1];
      action.accept(x3, y3);
    }
  }

  @Override
  public int getAxisCount() {
    return this.coordinates.length / 3;
  }

  @Override
  public double getCoordinate(int vertexIndex, final int axisIndex) {
    final int axisCount = getAxisCount();
    if (axisIndex < axisCount) {
      while (vertexIndex < 0) {
        vertexIndex += 4;
      }
      if (vertexIndex >= 3) {
        vertexIndex = vertexIndex % 4;
        if (vertexIndex == 3) {
          vertexIndex = 0;
        }
      }
      return this.coordinates[vertexIndex * axisCount + axisIndex];
    } else {
      return Double.NaN;
    }
  }

  @Override
  public double[] getCoordinates() {
    final int axisCount = getAxisCount();
    final double[] coordinates = new double[this.coordinates.length + axisCount];
    System.arraycopy(this.coordinates, 0, coordinates, 0, this.coordinates.length);
    System.arraycopy(this.coordinates, 0, coordinates, 3 * axisCount, axisCount);
    return coordinates;
  }

}
