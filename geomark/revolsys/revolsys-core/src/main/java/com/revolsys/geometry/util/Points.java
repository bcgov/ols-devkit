package com.revolsys.geometry.util;

import org.jeometry.common.math.Angle;

import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.model.impl.PointDoubleXY;

public class Points {

  public static Point closestPoint(final Point point, final Point point1, final Point point2) {
    final double distance1 = point.distancePoint(point1);
    final double distance2 = point.distancePoint(point2);
    Point closestPoint;
    if (distance1 <= distance2) {
      closestPoint = point1;
    } else {
      closestPoint = point2;
    }
    return closestPoint;
  }

  /**
   * Calculate the distance between two coordinates.
   *
   * @param x1 The first x coordinate.
   * @param y1 The first y coordinate.
   * @param x2 The second x coordinate.
   * @param y2 The second y coordinate.
   * @return The distance.
   */
  public static double distance(final double x1, final double y1, final double x2,
    final double y2) {
    final double dx = x2 - x1;
    final double dy = y2 - y1;
    final double distanceSquared = dx * dx + dy * dy;
    final double distance = Math.sqrt(distanceSquared);
    return distance;
  }

  public static double distanceInt(final int x1, final int y1, final int x2, final int y2) {
    final long dx = x2 - x1;
    final int dy = y2 - y1;
    final long distanceSquared = dx * dx + dy * dy;
    final double distance = Math.sqrt(distanceSquared);
    return distance;
  }

  public static Point pointOffset(final Point point1, final Point point2, final double xOffset,
    double yOffset) {
    final double distance = point1.distancePoint(point2);

    final double projectionFactor = xOffset / distance;
    if (Double.isNaN(projectionFactor) || Double.isInfinite(projectionFactor)) {
      return new PointDoubleXY(point1.getX() + xOffset, point1.getY() + yOffset);
    } else {
      final Point point = LineSegmentUtil.pointAlong(point1, point2, projectionFactor);
      if (yOffset == 0) {
        return new PointDoubleXY(point);
      } else {
        double angle = point1.angle2d(point2);
        if (yOffset > 0) {
          angle += Angle.PI_OVER_2;
        } else {
          angle -= Angle.PI_OVER_2;
          yOffset = -yOffset;
        }
        final double x = point.getX() + Math.cos(angle) * yOffset;
        final double y = point.getY() + Math.sin(angle) * yOffset;
        return new PointDoubleXY(x, y);
      }
    }
  }

}
