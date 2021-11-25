package com.revolsys.elevation.tin;

import com.revolsys.geometry.algorithm.CGAlgorithms;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Triangle;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.model.impl.Circle;
import com.revolsys.geometry.model.impl.TriangleDoubleXYZ;
import com.revolsys.geometry.util.Points;

public class TriangleWithCircumcircle extends TriangleDoubleXYZ {
  private static final long serialVersionUID = 1L;

  public static Triangle newClockwiseTriangle(final double x1, final double y1, final double x2,
    final double y2, final double x3, final double y3) {
    return newClockwiseTriangle(x1, y1, Double.NaN, x2, y2, Double.NaN, x3, y3, Double.NaN);
  }

  public static TriangleWithCircumcircle newClockwiseTriangle(final double x1, final double y1,
    final double z1, final double x2, final double y2, final double z2, final double x3,
    final double y3, final double z3) {
    if (CoordinatesListUtil.orientationIndex(x1, y1, x2, y2, x3, y3) == CGAlgorithms.CLOCKWISE) {
      return new TriangleWithCircumcircle(//
        x1, y1, z1, //
        x2, y2, z2, //
        x3, y3, z3);
    } else {
      return new TriangleWithCircumcircle(//
        x1, y1, z1, //
        x3, y3, z3, //
        x2, y2, z2);
    }
  }

  public static TriangleWithCircumcircle newClockwiseTriangle(final Point p1, final Point p2,
    final Point p3) {
    final double x1 = p1.getX();
    final double y1 = p1.getY();
    final double z1 = p1.getZ();

    final double x2 = p2.getX();
    final double y2 = p2.getY();
    final double z2 = p2.getZ();

    final double x3 = p3.getX();
    final double y3 = p3.getY();
    final double z3 = p3.getZ();

    return newClockwiseTriangle(x1, y1, z1, x2, y2, z2, x3, y3, z3);
  }

  public static TriangleWithCircumcircle newTriangle(final Point p1, final Point p2,
    final Point p3) {

    final double x1 = p1.getX();
    final double y1 = p1.getY();
    final double z1 = p1.getZ();

    final double x2 = p2.getX();
    final double y2 = p2.getY();
    final double z2 = p2.getZ();

    final double x3 = p3.getX();
    final double y3 = p3.getY();
    final double z3 = p3.getZ();
    return new TriangleWithCircumcircle(//
      x1, y1, z1, //
      x2, y2, z2, //
      x3, y3, z3);
  }

  private double centreX = Double.NaN;

  private double centreY = Double.NaN;

  private double radius = Double.NaN;

  public TriangleWithCircumcircle(final double x1, final double y1, final double z1,
    final double x2, final double y2, final double z2, final double x3, final double y3,
    final double z3) {
    super(x1, y1, z1, x2, y2, z2, x3, y3, z3);
    try {
      final double[] centre = Triangle.getCircumcentreCoordinates(x1, y1, x2, y2, x3, y3);
      this.centreX = centre[X];
      this.centreY = centre[Y];
      this.radius = Triangle.getCircumcircleRadius(this.centreX, this.centreY, x3, y3);
    } catch (final Throwable e) {
    }
  }

  @Override
  public boolean circumcircleContains(final double x, final double y) {
    final double distanceFromCentre = Points.distance(this.centreX, this.centreY, x, y);
    return distanceFromCentre < this.radius + 0.0001;
  }

  @Override
  public TriangleWithCircumcircle clone() {
    return (TriangleWithCircumcircle)super.clone();
  }

  @Override
  public Point getCircumcentre() {
    return getGeometryFactory().point(this.centreX, this.centreY);
  }

  @Override
  public Circle getCircumcircle() {
    final Point circumcentre = getCircumcentre();
    return new Circle(circumcentre, this.radius);
  }

  @Override
  public double getCircumcircleRadius() {
    return this.radius;
  }

}
