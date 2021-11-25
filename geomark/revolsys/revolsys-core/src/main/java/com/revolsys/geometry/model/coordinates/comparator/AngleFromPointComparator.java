package com.revolsys.geometry.model.coordinates.comparator;

import java.util.Comparator;

import org.jeometry.common.math.Angle;

import com.revolsys.geometry.model.Point;

public class AngleFromPointComparator implements Comparator<Point> {

  private final double x;

  private final double y;

  public AngleFromPointComparator(final double x, final double y) {
    super();
    this.x = x;
    this.y = y;
  }

  public int compare(final double x1, final double y1, final double x2, final double y2) {
    final double angleC1 = Angle.angle2d(this.x, this.y, x1, y1);
    final double angleC2 = Angle.angle2d(this.x, this.y, x2, y2);
    if (angleC1 < angleC2) {
      return 1;
    } else if (angleC1 > angleC2) {
      return -1;
    } else {
      return 0;
    }
  }

  @Override
  public int compare(final Point c1, final Point c2) {
    final double x1 = c1.getX();
    final double y1 = c1.getY();
    final double x2 = c2.getX();
    final double y2 = c2.getY();
    return compare(x1, y1, x2, y2);
  }
}
