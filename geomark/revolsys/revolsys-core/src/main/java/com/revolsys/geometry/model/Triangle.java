package com.revolsys.geometry.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.jeometry.common.function.Consumer4Double;
import org.jeometry.common.function.Function4Double;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

import com.revolsys.geometry.algorithm.NotRepresentableException;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.model.impl.Circle;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.impl.TriangleLinearRing;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDoubleGF;
import com.revolsys.geometry.util.Points;
import com.revolsys.geometry.util.Triangles;

public interface Triangle extends Polygon {
  static void addIntersection(final GeometryFactory geometryFactory, final Set<Point> coordinates,
    final Point line1Start, final Point line1End, final Point line2Start, final Point line2End) {
    final LineString intersections = LineSegmentUtil.getIntersection(geometryFactory, line1Start,
      line1End, line2Start, line2End);
    for (int i = 0; i < intersections.getVertexCount(); i++) {
      final Point point = intersections.getPoint(i);
      coordinates.add(point);
    }
  }

  static boolean containsPoint(final double x1, final double y1, final double x2, final double y2,
    final double x3, final double y3, final double x, final double y) {
    final double y2y3 = y2 - y3;
    final double xx3 = x - x3;
    final double x3x2 = x3 - x2;
    final double yy3 = y - y3;
    final double x1x3 = x1 - x3;
    final double y1y3 = y1 - y3;
    final double a = (y2y3 * xx3 + x3x2 * yy3) / (y2y3 * x1x3 + x3x2 * y1y3);
    if (0 <= a && a <= 1) {
      final double y3y1 = y3 - y1;
      final double b = (y3y1 * xx3 + x1x3 * yy3) / (y2y3 * x1x3 + x3x2 * y1y3);
      if (0 <= b && b <= 1) {
        final double c = 1 - a - b;
        if (-0.001 < c && c < 1.001) {
          return true;
        }
      }
    }
    return false;
  }

  static boolean containsPoint(final int x1, final int y1, final int x2, final int y2, final int x3,
    final int y3, final int x, final int y) {
    final long y2y3 = y2 - y3;
    final long xx3 = x - x3;
    final long x3x2 = x3 - x2;
    final long yy3 = y - y3;
    final long x1x3 = x1 - x3;
    final long y1y3 = y1 - y3;
    final long a = (y2y3 * xx3 + x3x2 * yy3) / (y2y3 * x1x3 + x3x2 * y1y3);
    if (0 <= a && a <= 1) {
      final long y3y1 = y3 - y1;
      final long b = (y3y1 * xx3 + x1x3 * yy3) / (y2y3 * x1x3 + x3x2 * y1y3);
      if (0 <= b && b <= 1) {
        final long c = 1 - a - b;
        if (0 <= c && c <= 1) {
          return true;
        }
      }
    }
    return false;
  }

  static double[] getCircumcentreCoordinates(final double x1, final double y1, final double x2,
    final double y2, final double x3, final double y3) {
    final double x1MinusX3 = x1 - x3;
    final double x1PlusX3 = x1 + x3;
    final double x2MinusX3 = x2 - x3;
    final double x2PlusX3 = x2 + x3;
    final double y1MinusY3 = y1 - y3;
    final double y1PlusY3 = y1 + y3;
    final double y2MinusY3 = y2 - y3;
    final double y2PlusY3 = y2 + y3;
    final double d = (x1MinusX3 * y2MinusY3 - x2MinusX3 * y1MinusY3) * 2;
    final double x1MinusX3TimesX1PlusX3 = x1MinusX3 * x1PlusX3;
    final double y1MinusY3TimesY1PlusY3 = y1MinusY3 * y1PlusY3;
    final double x2MinusX3TimesX2PlusX3 = x2MinusX3 * x2PlusX3;
    final double y2MinusY3Times2PlusY3 = y2MinusY3 * y2PlusY3;
    final double x1MinusX3TimesX1PlusX3Plus1MinusY3TimesY1PlusY3 = x1MinusX3TimesX1PlusX3
      + y1MinusY3TimesY1PlusY3;
    final double x2MinusX3TimesX2PlusX3PlusY2MinusY3Times2PlusY3 = x2MinusX3TimesX2PlusX3
      + y2MinusY3Times2PlusY3;
    final double centreX = (x1MinusX3TimesX1PlusX3Plus1MinusY3TimesY1PlusY3 * y2MinusY3
      - x2MinusX3TimesX2PlusX3PlusY2MinusY3Times2PlusY3 * y1MinusY3) / d;
    final double centreY = (x2MinusX3TimesX2PlusX3PlusY2MinusY3Times2PlusY3 * x1MinusX3
      - x1MinusX3TimesX1PlusX3Plus1MinusY3TimesY1PlusY3 * x2MinusX3) / d;
    if (Double.isFinite(centreX) && Double.isFinite(centreY)) {
      return new double[] {
        centreX, centreY
      };
    } else {
      throw new NotRepresentableException("Cannot get circumcentre for TRIANGLE(" + x1 + "," + y1
        + " " + x2 + "," + y2 + " " + x3 + "," + y3 + ")");
    }
  }

  static Point getCircumcentrePoint(final GeometryFactory geometryFactory, final double x1,
    final double y1, final double x2, final double y2, final double x3, final double y3) {
    final double[] circumcentreCoordinates = getCircumcentreCoordinates(x1, y1, x2, y2, x3, y3);
    return geometryFactory.point(circumcentreCoordinates);
  }

  static double getCircumcircleRadius(final double centreX, final double centreY, final double x3,
    final double y3) {
    return Points.distance(centreX, centreY, x3, y3);
    // final double xDistanceSquared = (x3 - centreX) * (x3 - centreX);
    // final double yDistanceSquared = (y3 - centreY) * (y3 - centreY);
    // final double radiusSquared = xDistanceSquared + yDistanceSquared;
    // return Math.sqrt(radiusSquared);
  }

  static double getElevation(//
    final double x1, final double y1, double z1, //
    final double x2, final double y2, double z2, //
    final double x3, final double y3, double z3, //
    final double x, final double y) {
    final boolean finite1 = Double.isFinite(z1);
    final boolean finite2 = Double.isFinite(z2);
    final boolean finite3 = Double.isFinite(z3);
    if (finite1) {
      if (finite2) {
        if (finite3) {
        } else {
          z3 = (z1 + z2) / 2;
        }
      } else {
        if (finite3) {
          z2 = (z1 + z3) / 2;
        } else {
          return z1;
        }
      }
    } else {
      if (finite2) {
        if (finite3) {
          z1 = (z2 + z3) / 2;
        } else {
          return z2;
        }
      } else {
        if (finite3) {
          return z3;
        } else {
          return Double.NaN;
        }
      }
    }
    if (x == x1 && y == y1) {
      return z1;
    } else if (x == x2 && y == y2) {
      return z2;
    } else if (x == x3 && y == y3) {
      return z3;
    } else if (z1 == z2 && z2 == z3) {
      return z1;
    }

    // https://en.wikipedia.org/wiki/Barycentric_coordinate_system
    // http://www.alecjacobson.com/weblog/?p=1596

    final double xx3 = x - x3;
    final double x1x3 = x1 - x3;
    final double x3x2 = x3 - x2;
    final double yy3 = y - y3;
    final double y1y3 = y1 - y3;
    final double y2y3 = y2 - y3;
    final double y3y1 = y3 - y1;
    final double det = y2y3 * x1x3 + x3x2 * y1y3;

    final double lambda1 = (y2y3 * xx3 + x3x2 * yy3) / det;
    final double lambda2 = (y3y1 * xx3 + x1x3 * yy3) / det;
    final double lambda3 = 1.0 - lambda1 - lambda2;
    final double z = lambda1 * z1 + lambda2 * z2 + lambda3 * z3;
    return z;
  }

  static int getElevation(//
    final int x1, final int y1, int z1, //
    final int x2, final int y2, int z2, //
    final int x3, final int y3, int z3, //
    final int x, final int y) {
    final boolean finite1 = z1 != Integer.MIN_VALUE;
    final boolean finite2 = z2 != Integer.MIN_VALUE;
    final boolean finite3 = z3 != Integer.MIN_VALUE;
    if (finite1) {
      if (finite2) {
        if (finite3) {
        } else {
          z3 = (z1 + z2) / 2;
        }
      } else {
        if (finite3) {
          z2 = (z1 + z3) / 2;
        } else {
          return z1;
        }
      }
    } else {
      if (finite2) {
        if (finite3) {
          z1 = (z2 + z3) / 2;
        } else {
          return z2;
        }
      } else {
        if (finite3) {
          return z3;
        } else {
          return Integer.MIN_VALUE;
        }
      }
    }
    if (x == x1 && y == y1) {
      return z1;
    } else if (x == x2 && y == y2) {
      return z2;
    } else if (x == x3 && y == y3) {
      return z3;
    } else if (z1 == z2 && z2 == z3) {
      return z1;
    }

    // https://en.wikipedia.org/wiki/Barycentric_coordinate_system
    // http://www.alecjacobson.com/weblog/?p=1596

    final double invDET = 1. / ((y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3));

    final double l1 = ((y2 - y3) * (x - x3) + (x3 - x2) * (y - y3)) * invDET;
    final double l2 = ((y3 - y1) * (x - x3) + (x1 - x3) * (y - y3)) * invDET;
    final double l3 = 1. - l1 - l2;
    final double z = l1 * z1 + l2 * z2 + l3 * z3;
    return (int)z;
  }

  static double getElevation(final List<Triangle> triangles, final double x, final double y) {
    for (final Triangle triangle : triangles) {
      if (triangle.contains(x, y)) {
        final double z = triangle.getElevation(x, y);
        if (Double.isFinite(z)) {
          return z;
        }
      }
    }
    return Double.NaN;
  }

  default boolean circumcircleContains(final double x, final double y) {
    final double x1 = getCoordinate(0, X);
    final double y1 = getCoordinate(0, Y);

    final double x2 = getCoordinate(1, X);
    final double y2 = getCoordinate(1, Y);

    final double x3 = getCoordinate(2, X);
    final double y3 = getCoordinate(2, Y);
    try {
      final double[] centre = getCircumcentreCoordinates(x1, y1, x2, y2, x3, y3);
      final double centreX = centre[X];
      final double centreY = centre[Y];

      final double circumcircleRadius = getCircumcircleRadius(centreX, centreY, x3, y3);
      final double distanceFromCentre = Points.distance(centreX, centreY, x, y);
      return distanceFromCentre < circumcircleRadius + 0.0001;

    } catch (final NotRepresentableException e) {
      return getBoundingBox().bboxCovers(x, y);
    }
  }

  @Override
  default boolean contains(final double x, final double y) {
    return containsPoint(x, y);
  }

  default boolean containsPoint(final double x, final double y) {
    final double x1 = getX(0);
    final double y1 = getY(0);

    final double x2 = getX(1);
    final double y2 = getY(1);

    final double x3 = getX(2);
    final double y3 = getY(2);

    return containsPoint(x1, y1, x2, y2, x3, y3, x, y);
  }

  /**
   * Returns true if the point lies inside or on the edge of the Triangle.
   *
   * @return True if the point lies inside or on the edge of the Triangle.
   */
  default boolean containsPoint(final Point point) {
    final double x = point.getX();
    final double y = point.getY();

    return containsPoint(x, y);
  }

  default boolean equals(final Triangle triangle) {
    final HashSet<Point> coords = new HashSet<>();
    coords.add(triangle.getP0());
    coords.add(triangle.getP1());
    coords.add(triangle.getP2());
    coords.add(getP0());
    coords.add(getP1());
    coords.add(getP2());
    return coords.size() == 3;
  }

  default boolean equalsVertex2d(final int vertexIndex, final double x, final double y) {
    final double x1 = getX(vertexIndex);
    if (x1 == x) {
      final double y1 = getY(vertexIndex);
      if (y1 == y) {
        return true;
      }
    }
    return false;
  }

  @Override
  default <R> R findSegment(final Function4Double<R> action) {
    if (!isEmpty()) {
      final double x1 = getX(0);
      final double y1 = getY(0);
      final double x2 = getX(1);
      final double y2 = getY(1);
      final double x3 = getX(2);
      final double y3 = getY(2);
      R result = action.accept(x1, y1, x2, y2);
      if (result == null) {
        result = action.accept(x2, y2, x3, y3);
        if (result == null) {
          result = action.accept(x3, y3, x1, y1);
        }
      }
    }
    return null;
  }

  @Override
  default void forEachSegment(final Consumer4Double action) {
    if (!isEmpty()) {
      final double x1 = getX(0);
      final double y1 = getY(0);
      final double x2 = getX(1);
      final double y2 = getY(1);
      final double x3 = getX(2);
      final double y3 = getY(2);
      action.accept(x1, y1, x2, y2);
      action.accept(x2, y2, x3, y3);
      action.accept(x3, y3, x1, y1);
    }
  }

  @Override
  default void forEachVertex(final CoordinatesOperation coordinatesOperation,
    final CoordinatesOperationPoint point, final Consumer<CoordinatesOperationPoint> action) {
    final int vertexCount = getVertexCount();
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      final double x = getX(vertexIndex);
      final double y = getY(vertexIndex);
      final double z = getZ(vertexIndex);
      point.setPoint(x, y, z);
      coordinatesOperation.perform(point);
      action.accept(point);
    }
  }

  @Override
  default void forEachVertex(final CoordinatesOperationPoint point,
    final Consumer<CoordinatesOperationPoint> action) {
    final int vertexCount = getVertexCount();
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      final double x = getX(vertexIndex);
      final double y = getY(vertexIndex);
      final double z = getZ(vertexIndex);
      point.setPoint(x, y, z);
      action.accept(point);
    }
  }

  /**
   * Computes the 2D area of this triangle. The area value is always
   * non-negative.
   *
   * @return the area of this triangle
   *
   * @see #signedArea()
   */
  @Override
  default double getArea() {
    final double x1 = getX(0);
    final double y1 = getY(0);
    final double x2 = getX(1);
    final double y2 = getY(1);
    final double x3 = getX(2);
    final double y3 = getY(2);
    return Triangles.area(x1, y1, x2, y2, x3, y3);
  }

  /**
   * Computes the 3D area of this triangle. The value computed is always
   * non-negative.
   *
   * @return the 3D area of this triangle
   */
  default double getArea3D() {
    final double x1 = getX(0);
    final double y1 = getY(0);
    final double z1 = getZ(0);
    final double x2 = getX(1);
    final double y2 = getY(1);
    final double z2 = getZ(2);
    final double x3 = getX(2);
    final double y3 = getY(2);
    final double z3 = getZ(2);
    return Triangles.area3D(x1, y1, z1, x2, y2, z2, x3, y3, z3);
  }

  @Override
  default int getAxisCount() {
    return 3;
  }

  default Point getCircumcentre() {
    final double x1 = getCoordinate(0, X);
    final double y1 = getCoordinate(0, Y);

    final double x2 = getCoordinate(1, X);
    final double y2 = getCoordinate(1, Y);

    final double x3 = getCoordinate(2, X);
    final double y3 = getCoordinate(2, Y);

    final GeometryFactory geometryFactory = getGeometryFactory();
    return getCircumcentrePoint(geometryFactory, x1, y1, x2, y2, x3, y3);
  }

  /**
   * Computes the circumcircle of a triangle. The circumcircle is the smallest
   * circle which encloses the triangle.
   *
   * @return The circumcircle of the triangle.
   */
  default Circle getCircumcircle() {
    final double x1 = getCoordinate(0, X);
    final double y1 = getCoordinate(0, Y);

    final double x2 = getCoordinate(1, X);
    final double y2 = getCoordinate(1, Y);

    final double x3 = getCoordinate(2, X);
    final double y3 = getCoordinate(2, Y);
    final GeometryFactory geometryFactory = getGeometryFactory();
    final Point centre = getCircumcentrePoint(geometryFactory, x1, y1, x2, y2, x3, y3);
    final double centreX = centre.getX();
    final double centreY = centre.getY();

    final double circumcircleRadius = getCircumcircleRadius(centreX, centreY, x3, y3);
    return new Circle(centre, circumcircleRadius);
  }

  default BoundingBox getCircumcircleBoundingBox() {
    final double x1 = getCoordinate(0, X);
    final double y1 = getCoordinate(0, Y);

    final double x2 = getCoordinate(1, X);
    final double y2 = getCoordinate(1, Y);

    final double x3 = getCoordinate(2, X);
    final double y3 = getCoordinate(2, Y);
    try {
      final double[] centre = getCircumcentreCoordinates(x1, y1, x2, y2, x3, y3);
      final double centreX = centre[X];
      final double centreY = centre[Y];

      final double radius = getCircumcircleRadius(centreX, centreY, x3, y3);
      if (Double.isFinite(radius)) {
        final double minX = centreX - radius;
        final double minY = centreY - radius;
        final double maxX = centreX + radius;
        final double maxY = centreY + radius;
        final GeometryFactory geometryFactory = getGeometryFactory();
        final double[] bounds = {
          minX, minY, maxX, maxY
        };
        return geometryFactory.newBoundingBox(2, bounds);
      } else {
        return getBoundingBox();
      }
    } catch (final NotRepresentableException e) {
      return getBoundingBox();
    }
  }

  default double getCircumcircleRadius() {
    final double x1 = getCoordinate(0, X);
    final double y1 = getCoordinate(0, Y);

    final double x2 = getCoordinate(1, X);
    final double y2 = getCoordinate(1, Y);

    final double x3 = getCoordinate(2, X);
    final double y3 = getCoordinate(2, Y);
    try {
      final double[] centre = getCircumcentreCoordinates(x1, y1, x2, y2, x3, y3);
      final double centreX = centre[X];
      final double centreY = centre[Y];

      final double circumcircleRadius = getCircumcircleRadius(centreX, centreY, x3, y3);
      return circumcircleRadius;
    } catch (final NotRepresentableException e) {
      return Double.NaN;
    }
  }

  double getCoordinate(int vertexIndex, int axisIndex);

  @Override
  default double getCoordinate(final int ringIndex, final int vertexIndex, final int axisIndex) {
    if (ringIndex == 0) {
      return getCoordinate(vertexIndex, axisIndex);
    } else {
      return Double.NaN;
    }
  }

  double[] getCoordinates();

  default double getElevation(final double x, final double y) {
    final double x1 = getX(0);
    final double y1 = getY(0);
    final double z1 = getZ(0);
    final double x2 = getX(1);
    final double y2 = getY(1);
    final double z2 = getZ(1);
    final double x3 = getX(2);
    final double y3 = getY(2);
    final double z3 = getZ(2);
    return getElevation(x1, y1, z1, x2, y2, z2, x3, y3, z3, x, y);
  }

  @Override
  default GeometryFactory getGeometryFactory() {
    final int axisCount = getAxisCount();
    return GeometryFactory.floating(0, axisCount);
  }

  default Point getInCentre() {
    final double x1 = getCoordinate(0, X);
    final double y1 = getCoordinate(0, Y);

    final double x2 = getCoordinate(1, X);
    final double y2 = getCoordinate(1, Y);

    final double x3 = getCoordinate(2, X);
    final double y3 = getCoordinate(2, Y);

    final double len0 = Points.distance(x2, y2, x3, y3);
    final double len1 = Points.distance(x1, y1, x3, y3);
    final double len2 = Points.distance(x1, y1, x2, y2);
    final double circum = len0 + len1 + len2;

    final double inCentreX = (len0 * x1 + len1 * x2 + len2 * x3) / circum;
    final double inCentreY = (len0 * y1 + len1 * y2 + len2 * y3) / circum;
    return new PointDoubleXY(inCentreX, inCentreY);
  }

  default double getM(final int vertexIndex) {
    return getCoordinate(vertexIndex, M);
  }

  default Point getP0() {
    return getPoint(0);
  }

  default Point getP1() {
    return getPoint(1);
  }

  default Point getP2() {
    return getPoint(2);
  }

  default Point getPoint(int vertexIndex) {
    while (vertexIndex < 0) {
      vertexIndex += getVertexCount();
    }
    if (vertexIndex > getVertexCount()) {
      return null;
    } else {
      final int axisCount = getAxisCount();
      final double[] coordinates = new double[axisCount];
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        coordinates[axisIndex] = getCoordinate(vertexIndex, axisIndex);
      }
      final GeometryFactory geometryFactory = getGeometryFactory();
      return geometryFactory.point(coordinates);
    }
  }

  @Override
  default LinearRing getRing(final int ringIndex) {
    if (ringIndex == 0) {
      return new TriangleLinearRing(this);
    } else {
      return null;
    }
  }

  @Override
  default int getRingCount() {
    return 1;
  }

  @Override
  default List<LinearRing> getRings() {
    return Collections.singletonList(new TriangleLinearRing(this));
  }

  @Override
  default int getVertexCount() {
    return 4;
  }

  default double getX(final int vertexIndex) {
    return getCoordinate(vertexIndex, X);
  }

  default double getY(final int vertexIndex) {
    return getCoordinate(vertexIndex, Y);
  }

  default double getZ(final int vertexIndex) {
    return getCoordinate(vertexIndex, Z);
  }

  default LineSegment intersection(final GeometryFactory geometryFactory, final LineSegment line) {
    final Point lc0 = line.getPoint(0);
    final Point lc1 = line.getPoint(1);
    final boolean lc0Contains = containsPoint(lc0);
    final boolean lc1Contains = containsPoint(lc1);
    if (lc0Contains && lc1Contains) {
      return line;
    } else {
      final Set<Point> coordinates = new HashSet<>();
      addIntersection(geometryFactory, coordinates, lc0, lc1, getP0(), getP1());
      addIntersection(geometryFactory, coordinates, lc0, lc1, getP1(), getP2());
      addIntersection(geometryFactory, coordinates, lc0, lc1, getP2(), getP0());

      final Iterator<Point> coordIterator = coordinates.iterator();
      if (coordIterator.hasNext()) {
        final Point c1 = coordIterator.next();
        if (coordIterator.hasNext()) {
          final Point c2 = coordIterator.next();
          if (coordIterator.hasNext()) {
            // TODO Too many intersect
          }
          return new LineSegmentDoubleGF(c1, c2);
        } else {
          return new LineSegmentDoubleGF(c1, c1);
        }
      } else {
        return null;
      }
    }
  }

  default boolean intersectsCircumCircle(final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    return circumcircleContains(x, y);
  }

  @Override
  default boolean isEmpty() {
    return false;
  }

}
