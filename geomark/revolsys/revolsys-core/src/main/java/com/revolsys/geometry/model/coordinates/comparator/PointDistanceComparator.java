package com.revolsys.geometry.model.coordinates.comparator;

import java.util.Comparator;

import com.revolsys.geometry.model.Point;

public class PointDistanceComparator implements Comparator<Point> {
  private double x;

  private double y;

  public PointDistanceComparator() {
    this(0, 0);
  }

  public PointDistanceComparator(final double x, final double y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public int compare(final Point point1, final Point point2) {
    int compare;
    final double distance1 = point1.distancePoint(this.x, this.y);
    final double distance2 = point2.distancePoint(this.x, this.y);
    if (distance1 == distance2) {
      compare = point1.compareTo(point2);
    } else if (distance1 < distance2) {
      compare = -1;
    } else {
      compare = 1;
    }

    return compare;
  }

  public void setPoint(final double x, final double y) {
    this.x = x;
    this.y = y;
  }

  public void setPoint(final Point point) {
    this.x = point.getX();
    this.y = point.getY();
  }

  @Override
  public String toString() {
    return "distance(" + this.x + "," + this.y + ")";
  }
}
