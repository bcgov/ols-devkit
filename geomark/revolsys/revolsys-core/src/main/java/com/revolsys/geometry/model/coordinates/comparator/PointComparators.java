package com.revolsys.geometry.model.coordinates.comparator;

import java.util.Comparator;
import java.util.List;

import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.util.Points;

public interface PointComparators {

  static <P extends Point> Comparator<P> leftLowest() {
    return (point1, point2) -> {
      final double x1 = point1.getX();
      final double x2 = point2.getX();
      if (x1 < x2) {
        return -1;
      } else if (x1 > x2) {
        return 1;
      } else {
        final double y1 = point1.getY();
        final double y2 = point2.getY();
        if (y1 < y2) {
          return -1;
        } else if (y1 > y2) {
          return 1;
        } else {
          return 0;
        }
      }
    };
  }

  static <P extends Point> Comparator<P> lowestLeft() {
    return (point1, point2) -> {
      final double y1 = point1.getY();
      final double y2 = point2.getY();

      final int yCompare = Double.compare(y1, y2);
      if (yCompare == 0) {
        final double x1 = point1.getX();
        final double x2 = point2.getX();
        return Double.compare(x1, x2);
      } else {
        return yCompare;
      }
    };
  }

  static <P extends Point> Comparator<P> lowestLeft(final int resolution) {
    return (point1, point2) -> {
      final double y1 = point1.getY();
      final double y2 = point2.getY();

      final long y1Long = (long)Math.floor(y1 / resolution);
      final long y2Long = (long)Math.floor(y2 / resolution);
      if (y1Long < y2Long) {
        return -1;
      } else if (y1Long > y2Long) {
        return 1;
      } else {
        final double x1 = point1.getX();
        final double x2 = point2.getX();
        final long x1Long = (long)Math.floor(x1 / resolution);
        final long x2Long = (long)Math.floor(x2 / resolution);
        if (x1Long < x2Long) {
          return -1;
        } else if (x1Long > x2Long) {
          return 1;
        } else {
          final int yCompare = Double.compare(y1, y2);
          if (yCompare == 0) {
            return Double.compare(x1, x2);
          } else {
            return yCompare;
          }
        }
      }
    };
  }

  public static <P extends Point> void lowestLeftSort(final int resolution, final List<P> points) {
    final Comparator<P> comparator = lowestLeft(resolution);
    points.sort(comparator);
  }

  public static <P extends Point> void lowestLeftSort(final List<P> points) {
    final Comparator<P> comparator = lowestLeft();
    points.sort(comparator);
  }

  public static <P extends Point> Comparator<P> originDistance(final double originX,
    final double originY) {
    return (c1, c2) -> {
      final double x1 = c1.getX();
      final double y1 = c1.getY();
      final double x2 = c2.getX();
      final double y2 = c2.getY();
      final double distance1 = Points.distance(originX, originY, x1, y1);
      final double distance2 = Points.distance(originX, originY, x2, y2);
      int compare = Double.compare(distance1, distance2);
      if (compare == 0) {
        compare = Double.compare(y1, y2);
        if (compare == 0) {
          compare = Double.compare(x1, x2);
        }
      }
      return compare;
    };
  }

  public static <P extends Point> void originDistanceSort(final double originX,
    final double originY, final List<P> points) {
    final Comparator<P> comparator = originDistance(originX, originY);
    points.sort(comparator);

  }
}
