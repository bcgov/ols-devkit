/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.geometry.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.PointList;
import com.revolsys.geometry.model.editor.LineStringEditor;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.vertex.Vertex;

/**
 * Computes the convex hull of a {@link Geometry}.
 * The convex hull is the smallest convex Geometry that contains all the
 * points in the input Geometry.
 * <p>
 * Uses the Graham Scan algorithm.
 *
 *@version 1.7
 */
public class ConvexHull {
  /**
   *@param  vertices  the vertices of a linear ring, which may or may not be
   *      flattened (i.e. vertices collinear)
   *@return           the coordinates with unnecessary (collinear) vertices
   *      removed
   */
  private static LineStringEditor cleanRing(final GeometryFactory geometryFactory,
    final List<Point> points) {
    final int count = points.size();
    final LineStringEditor cleanedRing = new LineStringEditor(geometryFactory, count);
    Point previousDistinctPoint = null;

    for (int i = 0; i <= count - 2; i++) {
      final Point currentPoint = points.get(i);
      final Point nextPoint = points.get(i + 1);
      if (currentPoint.equals(nextPoint)) {
      } else if (previousDistinctPoint != null
        && isBetween(previousDistinctPoint, currentPoint, nextPoint)) {
      } else {
        cleanedRing.appendVertex(currentPoint);
        previousDistinctPoint = currentPoint;
      }
    }
    cleanedRing.appendVertex(points.get(count - 1));
    return cleanedRing;
  }

  private static PointList computeOctPts(final Collection<Point> points) {
    final Iterator<Point> iterator = points.iterator();
    final Point firstPoint = iterator.next();
    Point octetPoint1 = firstPoint;
    Point octetPoint2 = firstPoint;
    Point octetPoint3 = firstPoint;
    Point octetPoint4 = firstPoint;
    Point octetPoint5 = firstPoint;
    Point octetPoint6 = firstPoint;
    Point octetPoint7 = firstPoint;
    Point octetPoint8 = firstPoint;
    while (iterator.hasNext()) {
      final Point currentPoint = iterator.next();
      final double currentX = currentPoint.getX();
      final double currentY = currentPoint.getY();
      if (currentX < octetPoint1.getX()) {
        octetPoint1 = currentPoint;
      }
      if (currentX - currentY < octetPoint2.getX() - octetPoint2.getY()) {
        octetPoint2 = currentPoint;
      }
      if (currentY > octetPoint3.getY()) {
        octetPoint3 = currentPoint;
      }
      if (currentX + currentY > octetPoint4.getX() + octetPoint4.getY()) {
        octetPoint4 = currentPoint;
      }
      if (currentX > octetPoint5.getX()) {
        octetPoint5 = currentPoint;
      }
      if (currentX - currentY > octetPoint6.getX() - octetPoint6.getY()) {
        octetPoint6 = currentPoint;
      }
      if (currentY < octetPoint7.getY()) {
        octetPoint7 = currentPoint;
      }
      if (currentX + currentY < octetPoint8.getX() + octetPoint8.getY()) {
        octetPoint8 = currentPoint;
      }
    }
    final PointList pointList = new PointList();
    pointList.add(octetPoint1, false);
    pointList.add(octetPoint2, false);
    pointList.add(octetPoint3, false);
    pointList.add(octetPoint4, false);
    pointList.add(octetPoint5, false);
    pointList.add(octetPoint6, false);
    pointList.add(octetPoint7, false);
    pointList.add(octetPoint8, false);
    return pointList;
  }

  private static List<Point> computeOctRing(final Collection<Point> points) {
    final PointList pointList = computeOctPts(points);

    // points must all lie in a line
    if (pointList.size() < 3) {
      return null;
    }
    pointList.closeRing();
    return pointList;
  }

  public static Geometry convexHull(final Geometry geometry) {
    final Vertex vertices = geometry.vertices();
    final GeometryFactory geometryFactory = geometry.getGeometryFactory();
    return convexHull(geometryFactory, vertices);
  }

  public static Geometry convexHull(final GeometryFactory geometryFactory,
    final Iterable<? extends Point> points) {
    return convexHull(geometryFactory, points, 50);
  }

  public static Geometry convexHull(final GeometryFactory geometryFactory,
    final Iterable<? extends Point> inputPoints, final int maxPoints) {
    Collection<Point> points = ConvexHull.getUniquePoints(inputPoints);

    final int vertexCount = points.size();
    if (vertexCount == 0) {
      return geometryFactory.geometryCollection();
    } else if (vertexCount == 1) {
      return geometryFactory.point(points.iterator().next());
    } else if (vertexCount == 2) {
      return geometryFactory.lineString(points);
    } else {
      // use heuristic to reduce points, if large
      if (vertexCount > maxPoints) {
        points = reduce(points);
      }

      points = preSort(points);

      final Stack<Point> hullPoints = grahamScan(points);

      final LineStringEditor cleanedRing = cleanRing(geometryFactory, hullPoints);
      if (cleanedRing.getVertexCount() == 3) {
        return geometryFactory.lineString(cleanedRing.getVertex(0), cleanedRing.getVertex(1));
      } else {
        return cleanedRing.newPolygon();
      }
    }
  }

  private static Set<Point> getUniquePoints(final Iterable<? extends Point> points) {
    final Set<Point> set = new HashSet<>();
    for (final Point point : points) {
      final Point point2d = new PointDoubleXY(point);
      set.add(point2d);
    }
    return set;
  }

  /**
   * Uses the Graham Scan algorithm to compute the convex hull vertices.
   *
   * @param points a list of points, with at least 3 entries
   * @return a Stack containing the ordered points of the convex hull ring
   */
  private static Stack<Point> grahamScan(final Collection<Point> points) {
    final Stack<Point> pointStack = new Stack<>();
    final Iterator<Point> pointIterator = points.iterator();
    final Point firstPoint = pointIterator.next();
    pointStack.push(firstPoint);
    pointStack.push(pointIterator.next());
    pointStack.push(pointIterator.next());
    while (pointIterator.hasNext()) {
      Point p = pointStack.pop();
      final Point currentPoint = pointIterator.next();
      while (!pointStack.empty()
        && CGAlgorithmsDD.orientationIndex(pointStack.peek(), p, currentPoint) > 0) {
        p = pointStack.pop();
      }
      pointStack.push(p);
      pointStack.push(currentPoint);
    }
    pointStack.push(firstPoint);
    return pointStack;
  }

  /**
   *@return    whether the three coordinates are collinear and c2 lies between
   *      c1 and c3 inclusive
   */
  private static boolean isBetween(final Point c1, final Point c2, final Point c3) {
    if (CGAlgorithmsDD.orientationIndex(c1, c2, c3) != 0) {
      return false;
    } else {
      final double x1 = c1.getX();
      final double y1 = c1.getY();
      final double x2 = c2.getX();
      final double y2 = c2.getY();
      final double x3 = c3.getX();
      final double y3 = c3.getY();

      if (x1 != x3) {
        if (x1 <= x2 && x2 <= x3) {
          return true;
        }
        if (x3 <= x2 && x2 <= x1) {
          return true;
        }
      }
      if (y1 != y3) {
        if (y1 <= y2 && y2 <= y3) {
          return true;
        }
        if (y3 <= y2 && y2 <= y1) {
          return true;
        }
      }
      return false;
    }
  }

  /**
   * Tests whether a point lies inside or on a ring. The ring may be oriented in
   * either direction. A point lying exactly on the ring boundary is considered
   * to be inside the ring.
   * <p>
   * This method does <i>not</i> first check the point against the envelope of
   * the ring.
   *
   * @param p
   *          point to check for ring inclusion
   * @param ring
   *          an array of coordinates representing the ring (which must have
   *          first point identical to last point)
   * @return true if p is inside ring
   *
   * @see locatePointInRing
   */
  private static boolean isPointInRing(final Point p, final List<Point> ring) {
    final Location location = RayCrossingCounter.locatePointInRing(p, ring);
    return location != Location.EXTERIOR;
  }

  private static List<Point> padArray3(final Collection<Point> points) {
    final List<Point> pad = Lists.toArray(points);
    while (pad.size() < 3) {
      pad.add(pad.get(0));
    }
    return pad;
  }

  private static List<Point> preSort(final Collection<Point> points) {
    double originX = Double.MAX_VALUE;
    double originY = Double.MAX_VALUE;
    // find the lowest point in the set. If two or more points have
    // the same minimum y coordinate choose the one with the minimum x.
    for (final Point point : points) {
      final double y = point.getY();
      final double x = point.getX();
      if (y < originY) {
        originX = x;
        originY = y;
      }
      if (y == originY) {
        if (x < originX) {
          originX = x;
        }
      }
    }

    // sort the points radially around the focal point.
    final RadialComparator comparator = new RadialComparator(originX, originY);
    final List<Point> newPoints = new ArrayList<>(points);
    newPoints.sort(comparator);
    return newPoints;
  }

  /**
   * Uses a heuristic to reduce the number of points scanned
   * to compute the hull.
   * The heuristic is to find a polygon guaranteed to
   * be in (or on) the hull, and eliminate all points inside it.
   * A quadrilateral defined by the extremal points
   * in the four orthogonal directions
   * can be used, but even more inclusive is
   * to use an octilateral defined by the points in the 8 cardinal directions.
   * <p>
   * Note that even if the method used to determine the polygon vertices
   * is not 100% robust, this does not affect the robustness of the convex hull.
   * <p>
   * To satisfy the requirements of the Graham Scan algorithm,
   * the returned array has at least 3 entries.
   *
   * @param pts the points to reduce
   * @return the reduced list of points (at least 3)
   */
  private static Collection<Point> reduce(final Collection<Point> points) {
    final List<Point> polyPts = computeOctRing(points);

    // unable to compute interior polygon for some reason
    if (polyPts == null) {
      return points;
    } else {

      // LinearRing ring = geomFactory.createLinearRing(polyPts);
      // System.out.println(ring);

      // add points defining polygon
      final Set<Point> reducedSet = new TreeSet<>();
      for (final Point polyPt : polyPts) {
        reducedSet.add(polyPt);
      }
      /**
       * Add all unique points not in the interior poly.
       * CGAlgorithms.isPointInRing is not defined for points actually on the ring,
       * but this doesn't matter since the points of the interior polygon
       * are forced to be in the reduced set.
       */
      for (final Point point : points) {
        if (!isPointInRing(point, polyPts)) {
          reducedSet.add(point);
        }
      }

      // ensure that computed array has at least 3 points (not necessarily
      // unique)
      if (reducedSet.size() < 3) {
        return padArray3(reducedSet);
      } else {
        return Lists.toArray(reducedSet);
      }
    }
  }
}
