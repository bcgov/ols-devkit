package com.revolsys.geometry.model.coordinates;

import org.jeometry.common.math.Angle;
import org.jeometry.common.number.Doubles;

import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.util.Points;
import com.revolsys.util.Trig;

public interface CoordinatesUtil {

  static Point average(final Point c1, final Point c2) {
    final int axisCount = Math.min(c1.getAxisCount(), c2.getAxisCount());
    final double[] coordinates = new double[axisCount];
    for (int i = 0; i < axisCount; i++) {
      final double value1 = c1.getCoordinate(i);
      final double value2 = c2.getCoordinate(i);
      double value;
      if (Double.isNaN(value1) || Double.isNaN(value1)) {
        value = value2;
      } else if (Double.isNaN(value2) || Double.isNaN(value2)) {
        value = value1;
      } else {
        value = Doubles.avg(value1, value2);
      }
      coordinates[i] = value;
    }
    return new PointDouble(coordinates);
  }

  static int compare(final double x1, final double y1, final double x2, final double y2) {
    if (x1 < x2) {
      return -1;
    } else if (x1 > x2) {
      return 1;
    } else {
      if (y1 < y2) {
        return -1;
      } else if (y1 > y2) {
        return 1;
      } else {
        return 0;
      }
    }
  }

  static int compareArcDistance(final double x1, final double y1, final double x2,
    final double y2) {
    // min is the minimum of all coordinates. This is used to normalize the x,y
    // values to calculate the distance
    double min = x1;
    if (x2 < min) {
      min = x2;
    }
    if (y1 < min) {
      min = y1;
    }
    if (x2 < min) {
      min = x2;
    }

    final double distance1 = Points.distance(min, min, x1, y1);

    final double distance2 = Points.distance(min, min, x2, y2);
    final int distanceCompare = Double.compare(distance1, distance2);
    if (distanceCompare == 0) {
      final int xCompare = Double.compare(x1, x2);
      return xCompare;
    } else {
      return distanceCompare;
    }
  }

  static int compareArcDistance(final Point point1, final Point point2) {
    final double x1 = point1.getX();
    final double y1 = point1.getY();
    final double x2 = point2.getX();
    final double y2 = point2.getY();
    return compareArcDistance(x1, y1, x2, y2);
  }

  static int compareToOrigin(final Point point1, final Object other) {
    if (other instanceof Point) {
      final Point point2 = (Point)other;
      return compareToOrigin(point1, point2);
    } else {
      return -1;
    }
  }

  static int compareToOrigin(final Point point1, final Point point2) {
    final double x1 = point1.getX();
    final double y1 = point1.getY();
    final double x2 = point2.getX();
    final double y2 = point2.getY();
    final double distance1 = Points.distance(0, 0, x1, y1);

    final double distance2 = Points.distance(0, 0, x2, y2);
    final int distanceCompare = Double.compare(distance1, distance2);
    if (distanceCompare == 0) {
      final int yCompare = Double.compare(y1, y2);
      return yCompare;
    } else {
      return distanceCompare;
    }
  }

  static boolean contains(final Iterable<? extends Point> points, final Point point) {
    for (final Point point1 : points) {
      if (point1.equals(point)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Computes the 3-dimensional Euclidean distance to another location.
   *
   * @param c a coordinate
   * @return the 3-dimensional Euclidean distance between the locations
   */
  static double distance3d(final Point point1, final Point point2) {
    final double dx = point1.getX() - point2.getX();
    final double dy = point1.getY() - point2.getY();
    final double dz = point1.getZ() - point2.getZ();
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  static boolean equals(final double x1, final double y1, final double x2, final double y2) {
    return x1 == x2 && y1 == y2;
  }

  static int getAxisCount(final Point... points) {
    int axisCount = 2;
    for (final Point point : points) {
      axisCount = Math.max(axisCount, point.getAxisCount());
    }
    return axisCount;
  }

  static double getElevation(final double x1, final double y1, final double z1, final double x2,
    final double y2, final double z2, final double x, final double y) {
    final double fraction = Points.distance(x, y, x1, y1) / Points.distance(x1, y1, x2, y2);
    final double z = z1 + (z2 - z1) * fraction;
    return z;
  }

  static double getElevation(final LineString line, final Point coordinate) {
    final LineString coordinates = line;
    Point previousCoordinate = coordinates.getPoint(0);
    for (int i = 1; i < coordinates.getVertexCount(); i++) {
      final Point currentCoordinate = coordinates.getPoint(i);

      if (LineSegmentUtil.distanceLinePoint(previousCoordinate, currentCoordinate,
        coordinate) < 1) {
        return LineSegmentUtil.getElevation(previousCoordinate, currentCoordinate, coordinate);
      }
      previousCoordinate = currentCoordinate;
    }
    return Double.NaN;
  }

  static double getElevation(final Point newLocation, final Point originalLocation) {
    if (originalLocation.getAxisCount() > 2) {
      final double z = originalLocation.getZ();
      if (Double.isNaN(z)) {
        return newLocation.getZ();
      } else {
        return z;
      }
    } else {
      return newLocation.getZ();
    }
  }

  static double getElevation(final Point point, final Point point1, final Point point2) {

    final double x1 = point1.getX();
    final double y1 = point1.getY();
    final double z1 = point1.getZ();
    final double x2 = point2.getX();
    final double y2 = point2.getY();
    final double z2 = point2.getZ();
    final double x = point.getX();
    final double y = point.getY();

    return getElevation(x1, y1, z1, x2, y2, z2, x, y);
  }

  static boolean isAcute(final Point point1, final Point point2, final Point point3) {
    final double x1 = point1.getX();
    final double y1 = point1.getY();
    final double x2 = point2.getX();
    final double y2 = point2.getY();
    final double x3 = point3.getX();
    final double y3 = point3.getY();

    return Angle.isAcute(x1, y1, x2, y2, x3, y3);
  }

  /**
   * Methods for computing and working with octants of the Cartesian plane
   * Octants are numbered as follows:
   *
   * <pre>
   *  \2|1/
   * 3 \|/ 0
   * ---+--
   * 4 /|\ 7
   * /5|6\
   *
   * <pre>
   * If line segments lie along a coordinate axis, the octant is the lower of the two
   * possible values.
   *
   * Returns the octant of a directed line segment (specified as x and y
   * displacements, which cannot both be 0).
   */
  static int octant(final double dx, final double dy) {
    if (dx == 0.0 && dy == 0.0) {
      throw new IllegalArgumentException(
        "Cannot compute the octant for point ( " + dx + ", " + dy + " )");
    }

    final double adx = Math.abs(dx);
    final double ady = Math.abs(dy);

    if (dx >= 0) {
      if (dy >= 0) {
        if (adx >= ady) {
          return 0;
        } else {
          return 1;
        }
      } else { // dy < 0
        if (adx >= ady) {
          return 7;
        } else {
          return 6;
        }
      }
    } else { // dx < 0
      if (dy >= 0) {
        if (adx >= ady) {
          return 3;
        } else {
          return 2;
        }
      } else { // dy < 0
        if (adx >= ady) {
          return 4;
        } else {
          return 5;
        }
      }
    }
  }

  /**
   * Returns the octant of a directed line segment from p0 to p1.
   */
  static int octant(final Point p0, final Point p1) {
    final double dx = p1.getX() - p0.getX();
    final double dy = p1.getY() - p0.getY();
    if (dx == 0.0 && dy == 0.0) {
      throw new IllegalArgumentException(
        "Cannot compute the octant for two identical points " + p0);
    }
    return octant(dx, dy);
  }

  static Point offset(final Point coordinate, final double angle, final double distance) {
    final double newX = coordinate.getX() + distance * Math.cos(angle);
    final double newY = coordinate.getY() + distance * Math.sin(angle);
    final Point newCoordinate = new PointDoubleXY(newX, newY);
    return newCoordinate;

  }

  static int orientationIndex(final Point p1, final Point p2, final Point q) {
    // travelling along p1->p2, turn counter clockwise to get to q return 1,
    // travelling along p1->p2, turn clockwise to get to q return -1,
    // p1, p2 and q are colinear return 0.
    final double x1 = p1.getX();
    final double y1 = p1.getY();
    final double x2 = p2.getX();
    final double y2 = p2.getY();
    final double x = q.getX();
    final double y = q.getY();
    return CoordinatesListUtil.orientationIndex(x1, y1, x2, y2, x, y);
  }

  /**
   * Return the first point of points1 not in points2
   * @param points1
   * @param points2
   * @return
   */
  static Point pointNotInList(final LineString line1, final LineString line2) {
    final int vertexCount = line1.getVertexCount();
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      final double x = line1.getX(vertexIndex);
      final double y = line1.getY(vertexIndex);
      if (!line2.hasVertex(x, y)) {
        return new PointDoubleXY(x, y);
      }
    }
    return null;
  }

  static Point setElevation(final Point newLocation, final Point originalLocation) {
    if (originalLocation.getAxisCount() > 2) {
      final double z = originalLocation.getZ();
      if (Double.isNaN(z)) {
        return newLocation;
      } else {
        final double[] points = originalLocation.getCoordinates();
        points[2] = z;
        final Point newCoordinates = new PointDouble(points);
        return newCoordinates;
      }
    } else {
      return newLocation;
    }
  }

  static Point translate(final Point point, final double angle, final double length) {
    final double x = point.getX();
    final double y = point.getY();

    final double newX = Trig.adjacent(x, angle, length);
    final double newY = Trig.opposite(y, angle, length);

    final Point newPoint = new PointDoubleXY(newX, newY);
    return newPoint;
  }
}
