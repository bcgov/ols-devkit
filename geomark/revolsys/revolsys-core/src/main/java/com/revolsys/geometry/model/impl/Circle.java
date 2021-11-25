package com.revolsys.geometry.model.impl;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;

public class Circle extends PointDoubleXY {

  private static final long serialVersionUID = 1L;

  private final BoundingBox boundingBox;

  private final double radius;

  private final double tolerance = 0.0001;

  public Circle(final Point centre, final double radius) {
    super(centre);
    this.radius = radius;
    final double x = getX();
    final double y = getY();
    this.boundingBox = new BoundingBoxDoubleXY(x - radius, y - radius, x + radius, y + radius);
  }

  public boolean contains(final Point point) {
    final double distanceFromCentre = distancePoint(point);
    return distanceFromCentre < this.radius + this.tolerance;
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public double getRadius() {
    return this.radius;
  }

  @Override
  public Geometry toGeometry() {
    return buffer(this.radius);
  }

  @Override
  public String toString() {
    return "CIRCLE(" + getX() + " " + getY() + " " + this.radius + ")";
  }
}
