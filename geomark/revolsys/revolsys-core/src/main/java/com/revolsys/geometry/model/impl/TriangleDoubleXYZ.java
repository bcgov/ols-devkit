package com.revolsys.geometry.model.impl;

import org.jeometry.common.function.BiConsumerDouble;
import org.jeometry.common.function.Consumer3Double;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Triangle;

public class TriangleDoubleXYZ extends AbstractTriangle {
  private static final long serialVersionUID = 1l;

  private final double x1;

  private final double y1;

  private final double z1;

  private final double x2;

  private final double y2;

  private final double z2;

  private final double x3;

  private final double y3;

  private final double z3;

  public TriangleDoubleXYZ(final double... triangleCoordinates) {
    this.x1 = triangleCoordinates[0];
    this.y1 = triangleCoordinates[1];
    this.z1 = triangleCoordinates[2];
    this.x2 = triangleCoordinates[3];
    this.y2 = triangleCoordinates[4];
    this.z2 = triangleCoordinates[5];
    this.x3 = triangleCoordinates[6];
    this.y3 = triangleCoordinates[7];
    this.z3 = triangleCoordinates[8];
  }

  public TriangleDoubleXYZ(final double x1, final double y1, final double z1, final double x2,
    final double y2, final double z2, final double x3, final double y3, final double z3) {
    this.x1 = x1;
    this.y1 = y1;
    this.z1 = z1;
    this.x2 = x2;
    this.y2 = y2;
    this.z2 = z2;
    this.x3 = x3;
    this.y3 = y3;
    this.z3 = z3;
  }

  @Override
  public TriangleDoubleXYZ clone() {
    return (TriangleDoubleXYZ)super.clone();
  }

  @Override
  public boolean contains(final double x, final double y) {
    return Triangle.containsPoint(this.x1, this.y1, this.x2, this.y2, this.x3, this.y3, x, y);
  }

  @Override
  public boolean containsPoint(final double x, final double y) {
    return Triangle.containsPoint(this.x1, this.y1, this.x2, this.y2, this.x3, this.y3, x, y);
  }

  @Override
  public void forEachVertex(final BiConsumerDouble action) {
    if (!isEmpty()) {
      action.accept(this.x1, this.y1);
      action.accept(this.x2, this.y2);
      action.accept(this.x3, this.y3);
    }
  }

  @Override
  public void forEachVertex(final Consumer3Double action) {
    if (!isEmpty()) {
      action.accept(this.x1, this.y1, this.z1);
      action.accept(this.x2, this.y2, this.z2);
      action.accept(this.x3, this.y3, this.z3);
    }
  }

  @Override
  public int getAxisCount() {
    return 3;
  }

  @Override
  public double getCoordinate(final int vertexIndex, final int axisIndex) {
    switch (axisIndex) {
      case 0:
        switch (vertexIndex) {
          case 0:
          case 3:
            return this.x1;
          case 1:
            return this.x2;
          case 2:
            return this.x3;

          default:
            return Double.NaN;
        }
      case 1:
        switch (vertexIndex) {
          case 0:
          case 3:
            return this.y1;
          case 1:
            return this.y2;
          case 2:
            return this.y3;

          default:
            return Double.NaN;
        }
      case 2:
        switch (vertexIndex) {
          case 0:
          case 3:
            return this.z1;
          case 1:
            return this.z2;
          case 2:
            return this.z3;
          default:
            return Double.NaN;
        }
      default:
        return Double.NaN;
    }
  }

  @Override
  public double[] getCoordinates() {
    return new double[] {
      this.x1, this.y1, this.z1, this.x2, this.y2, this.z2, this.x3, this.y3, this.z3
    };
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return GeometryFactory.DEFAULT_3D;
  }

  @Override
  public double getX(final int vertexIndex) {
    switch (vertexIndex) {
      case 0:
      case 3:
        return this.x1;
      case 1:
        return this.x2;
      case 2:
        return this.x3;

      default:
        return Double.NaN;
    }
  }

  @Override
  public double getY(final int vertexIndex) {
    switch (vertexIndex) {
      case 0:
      case 3:
        return this.y1;
      case 1:
        return this.y2;
      case 2:
        return this.y3;

      default:
        return Double.NaN;
    }
  }

  @Override
  public double getZ(final int vertexIndex) {
    switch (vertexIndex) {
      case 0:
      case 3:
        return this.z1;
      case 1:
        return this.z2;
      case 2:
        return this.z3;
      default:
        return Double.NaN;
    }
  }

}
