package com.revolsys.geometry.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.revolsys.geometry.algorithm.RobustLineIntersector;
import com.revolsys.geometry.algorithm.linematch.LineMatchGraph;
import com.revolsys.geometry.model.End;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.CoordinatesUtil;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;

public final class LineStringUtil {
  public static final String COORDINATE_DISTANCE = "coordinateDistance";

  public static final String COORDINATE_INDEX = "coordinateIndex";

  public static final String SEGMENT_DISTANCE = "segmentDistance";

  public static final String SEGMENT_INDEX = "segmentIndex";

  public static LineString addElevation(final LineString original, final LineString update) {
    final int axisCount = update.getAxisCount();
    if (axisCount > 2) {
      final double[] coordinates = update.getCoordinates();

      final Point c0 = update.getPoint(0);
      if (Double.isNaN(update.getZ(0))) {
        final double z = CoordinatesUtil.getElevation(original, c0);
        coordinates[2] = z;
      }
      final Point cN = update.getPoint(update.getVertexCount() - 1);
      if (Double.isNaN(cN.getZ())) {
        final double z = CoordinatesUtil.getElevation(original, c0);
        coordinates[update.getVertexCount() * axisCount + 2] = z;
      }
      return update.getGeometryFactory().lineString(axisCount, coordinates);
    } else {
      return update;
    }
  }

  public static void addLineString(final GeometryFactory geometryFactory, final LineString points,
    final Point startPoint, final int startIndex, final int endIndex, final Point endPoint,
    final List<LineString> lines) {
    final int length = endIndex - startIndex + 1;
    final LineString newPoints = points.subLine(startPoint, startIndex, length, endPoint);
    if (newPoints.getVertexCount() > 1) {
      final LineString newLine = geometryFactory.lineString(newPoints);
      if (newLine.getLength() > 0) {
        lines.add(newLine);
      }
    }
  }

  /**
   * Compare the coordinates of the two lines up to the given dimension to see
   * if they have the same ordinate values in either the forward or reverse
   * direction.
   *
   * @param line1 The first line.
   * @param line2 The second line.
   * @param dimension The dimension.
   * @return True if the coordinates match.
   */
  public static boolean equalsIgnoreDirection(final LineString line1, final LineString line2,
    final int dimension) {
    if (line1 == line2) {
      return true;
    } else {
      if (line1.equals(dimension, line2)) {
        return true;
      } else {
        return line1.reverse().equals(dimension, line2);
      }
    }
  }

  /**
   * Compare the 2D coordinates of the two lines to see if they have the same
   * ordinate values in either the forward or reverse direction.
   *
   * @param line1 The first line.
   * @param line2 The second line.
   * @return True if the coordinates match.
   */
  public static boolean equalsIgnoreDirection2d(final LineString line1, final LineString line2) {
    return equalsIgnoreDirection(line1, line2, 2);
  }

  public static Point getClosestEndsCoordinates(final LineString line, final Point coordinates) {
    final Point fromCoordinates = line.getPoint(0);
    final Point toCoordinates = line.getToPoint();
    if (fromCoordinates.distancePoint(coordinates) <= toCoordinates.distancePoint(coordinates)) {
      return fromCoordinates;
    } else {
      return toCoordinates;
    }
  }

  /**
   * Get the coordinate where two lines cross, or null if they don't cross.
   *
   * @param line1 The first line.
   * @param line2 The second line
   * @return The coordinate or null if they don't cross
   */
  public static Point getCrossingIntersection(final LineString line1, final LineString line2) {
    final RobustLineIntersector intersector = new RobustLineIntersector();

    final LineString coordinates1 = line1;
    final LineString coordinates2 = line2;
    final int numCoordinates1 = coordinates1.getVertexCount();
    final int numCoordinates2 = coordinates2.getVertexCount();
    final Point firstCoord1 = coordinates1.getPoint(0);
    final Point firstCoord2 = coordinates2.getPoint(0);
    final Point lastCoord1 = coordinates1.getPoint(numCoordinates1 - 1);
    final Point lastCoord2 = coordinates2.getPoint(numCoordinates2 - 2);

    Point previousCoord1 = firstCoord1;
    for (int i1 = 1; i1 < numCoordinates1; i1++) {
      final Point currentCoord1 = coordinates1.getPoint(i1);
      Point previousCoord2 = firstCoord2;

      for (int i2 = 1; i2 < numCoordinates2; i2++) {
        final Point currentCoord2 = coordinates2.getPoint(i2);

        intersector.computeIntersectionPoints(previousCoord1, currentCoord1, previousCoord2,
          currentCoord2);
        final int numIntersections = intersector.getIntersectionCount();
        if (intersector.hasIntersection()) {
          if (intersector.isProper()) {
            final Point intersection = intersector.getIntersection(0);
            return intersection;
          } else if (numIntersections == 1) {
            final Point intersection = intersector.getIntersection(0);
            if (i1 == 1 || i2 == 1 || i1 == numCoordinates1 - 1 || i2 == numCoordinates2 - 1) {
              if (!((intersection.equals(2, firstCoord1) || intersection.equals(2, lastCoord1))
                && (intersection.equals(2, firstCoord2) || intersection.equals(2, lastCoord2)))) {
                return intersection;
              }
            } else {
              return intersection;
            }
          } else if (intersector.isInteriorIntersection()) {
            for (int i = 0; i < numIntersections; i++) {
              final Point intersection = intersector.getIntersection(i);
              if (!Arrays.asList(currentCoord1, previousCoord1, currentCoord2, previousCoord2)
                .contains(intersection)) {
                return intersection;
              }
            }
          }

        }

        previousCoord2 = currentCoord2;
      }
      previousCoord1 = currentCoord1;
    }
    return null;

  }

  public static double getElevation(final Point point, final Point point1, final Point point2) {
    final double z1 = point1.getZ();
    final double z2 = point2.getZ();
    final double fraction;
    if (point1.equals(2, point2)) {
      fraction = 0.5;
    } else {
      fraction = point.distancePoint(point1) / point1.distancePoint(point2);
    }
    final double z = z1 + (z2 - z1) * fraction;
    return z;
  }

  @Deprecated
  public static Point getEndPoint(final LineString line, final boolean fromPoint) {
    if (fromPoint) {
      return line.getPoint(0);
    } else {
      return line.getToPoint();
    }
  }

  public static boolean hasEqualExact2d(final List<LineString> lines, final LineString newLine) {
    for (final LineString line : lines) {
      if (line.equals(2, newLine)) {
        return true;
      }
    }
    return false;
  }

  public static boolean hasLoop(final Collection<LineString> lines) {
    for (final LineString line : lines) {
      if (line.isClosed()) {
        return true;
      }
    }
    return false;

  }

  public static boolean intersects(final LineString line1, final LineString line2) {
    if (line1.getBoundingBox().bboxIntersects(line2.getBoundingBox())) {
      final LineMatchGraph<LineString> graph = new LineMatchGraph<>(line2);
      for (final LineString line : line1.segments()) {
        if (graph.add(line)) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean isEndsWithinDistance(final LineString line1, final LineString line2,
    final double maxDistance) {
    final Point fromPoint = line1.getPoint(0);
    if (isEndsWithinDistance(line2, fromPoint, maxDistance)) {
      return true;
    } else {
      final Point toPoint = line1.getToPoint();
      if (isEndsWithinDistance(line2, toPoint, maxDistance)) {
        return true;
      } else {
        return false;
      }
    }
  }

  public static boolean isEndsWithinDistance(final LineString line, final Point point,
    final double maxDistance) {
    final Point fromPoint = line.getPoint(0);
    if (fromPoint.distancePoint(point) < maxDistance) {
      return true;
    } else {
      final Point toPoint = line.getToPoint();
      if (toPoint.distancePoint(point) < maxDistance) {
        return true;
      } else {
        return false;
      }
    }
  }

  public static boolean isEndsWithinDistanceOfEnds(final LineString line1, final LineString line2,
    final double maxDistance) {
    final Point fromPoint = line1.getPoint(0);
    if (isWithinDistanceOfEnds(fromPoint, line2, maxDistance)) {
      final Point toPoint = line1.getToPoint();
      return isWithinDistanceOfEnds(toPoint, line2, maxDistance);
    } else {
      return false;
    }
  }

  public static boolean isPointOnLine(final LineString line, final double x, final double y,
    final double maxDistance) {
    final int vertexCount = line.getVertexCount();
    if (vertexCount > 0) {
      double x1 = line.getX(0);
      double y1 = line.getY(0);
      if (x == x1 && y == y1) {
        return true;
      }
      for (int vertexIndex = 1; vertexIndex < vertexCount; vertexIndex++) {
        final double x2 = line.getX(vertexIndex);
        final double y2 = line.getY(vertexIndex);
        if (x == x2 && y == y2) {
          return true;
        } else {
          if (LineSegmentUtil.isPointOnLine(x1, y1, x2, y2, x, y, maxDistance)) {
            return true;
          }
        }
        x1 = x2;
        y1 = y2;
      }
    }

    return false;
  }

  /**
   * Check to see if the point is on any of the segments of the line.
   *
   * @param line The line.
   * @param point The point.
   * @return True if the point is on the line, false otherwise.
   * @see LineSegmentUtil#isPointOnLine(GeometryFactory, Coordinates,
   *      Coordinates, Point)
   */
  public static boolean isPointOnLine(final LineString line, final Point point) {
    final GeometryFactory factory = line.getGeometryFactory();
    final double x = point.getX();
    final double y = point.getY();
    final int vertexCount = line.getVertexCount();
    if (vertexCount > 0) {
      double x1 = line.getX(0);
      double y1 = line.getY(0);
      if (x == x1 && y == y1) {
        return true;
      }
      for (int vertexIndex = 1; vertexIndex < vertexCount; vertexIndex++) {
        final double x2 = line.getX(vertexIndex);
        final double y2 = line.getY(vertexIndex);
        if (x == x2 && y == y2) {
          return true;
        } else {
          if (LineSegmentUtil.isPointOnLine(factory, x1, y1, x2, y2, x, y)) {
            return true;
          }
        }
        x1 = x2;
        y1 = y2;
      }
    }

    return false;

  }

  /**
   * Check to see if the point is on any of the segments of the line.
   *
   * @param line The line.
   * @param point The point.
   * @param maxDistance The maximum distance the point can be from the line.
   * @return True if the point is on the line, false otherwise.
   * @see LineSegmentUtil#isPointOnLine(Coordinates, Coordinates, Coordinates,
   *      double)
   */
  public static boolean isPointOnLine(final LineString line, final Point point,
    final double maxDistance) {
    final double x = point.getX();
    final double y = point.getY();
    return isPointOnLine(line, x, y, maxDistance);
  }

  public static boolean isWithinDistance(final Point point, final LineString line, final int index,
    final double maxDistance) {
    final Point point2 = line.getVertex(index);
    return point.distancePoint(point2) < maxDistance;
  }

  public static boolean isWithinDistanceOfEnds(final Point point, final LineString line,
    final double maxDistance) {
    if (isWithinDistance(point, line, 0, maxDistance)) {
      return true;
    } else {
      return isWithinDistance(point, line, line.getVertexCount() - 1, maxDistance);
    }
  }

  public static Point pointOffset(final LineString line, final End lineEnd, final double xOffset,
    double yOffset) {
    if (line.getLength() == 0) {
      return line.getFromPoint();
    } else {
      Point point1;
      Point point2;
      if (End.isFrom(lineEnd)) {
        point1 = line.getPoint(0);
        int i = 1;
        do {
          point2 = line.getPoint(i);
          i++;
        } while (point1.equals(point2));
      } else {
        point1 = line.getToPoint();
        int i = -2;
        do {
          point2 = line.getPoint(i);
          i--;
        } while (point1.equals(point2));
        yOffset = -yOffset;
      }
      return Points.pointOffset(point1, point2, xOffset, yOffset);
    }
  }

}
