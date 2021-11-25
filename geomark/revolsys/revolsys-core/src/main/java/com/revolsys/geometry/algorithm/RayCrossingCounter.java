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

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.segment.LineSegment;

/**
 * Counts the number of segments crossed by a horizontal ray extending to the right
 * from a given point, in an incremental fashion.
 * This can be used to determine whether a point lies in a {@link Polygonal} geometry.
 * The class determines the situation where the point lies exactly on a segment.
 * When being used for Point-In-Polygon determination, this case allows short-circuiting
 * the evaluation.
 * <p>
 * This class handles polygonal geometries with any number of shells and holes.
 * The orientation of the shell and hole rings is unimportant.
 * In order to compute a correct location for a given polygonal geometry,
 * it is essential that <b>all</b> segments are counted which
 * <ul>
 * <li>touch the ray
 * <li>lie in in any ring which may contain the point
 * </ul>
 * The only exception is when the point-on-segment situation is detected, in which
 * case no further processing is required.
 * The implication of the above rule is that segments
 * which can be a priori determined to <i>not</i> touch the ray
 * (i.e. by a test of their bounding box or Y-extent)
 * do not need to be counted.  This allows for optimization by indexing.
 *
 * @author Martin Davis
 *
 */
public class RayCrossingCounter implements Consumer<LineSegment> {

  public static Location locatePointInRing(final LineString ring, final double x, final double y) {
    final BoundingBox boundingBox = ring.getBoundingBox();
    if (boundingBox.bboxCovers(x, y)) {

      final RayCrossingCounter counter = new RayCrossingCounter(x, y);
      ring.findSegment((x1, y1, x2, y2) -> {
        counter.countSegment(x2, y2, x1, y1);
        if (counter.isOnSegment()) {
          return counter.getLocation();
        }
        return null;
      });
      return counter.getLocation();
    } else {
      return Location.EXTERIOR;
    }
  }

  public static Location locatePointInRing(final Point p, final Iterable<Point> ring) {
    final RayCrossingCounter counter = new RayCrossingCounter(p);
    final Iterator<Point> iterator = ring.iterator();
    if (iterator.hasNext()) {
      final Point previousPoint = iterator.next();
      while (iterator.hasNext()) {
        final Point currentPoint = iterator.next();
        counter.countSegment(currentPoint, previousPoint);
        if (counter.isOnSegment()) {
          return counter.getLocation();
        }
      }
    }
    return counter.getLocation();
  }

  /**
   * Determines the {@link Location} of a point in a ring.
   *
   * @param p
   *            the point to test
   * @param ring
   *            a coordinate sequence forming a ring
   * @return the location of the point in the ring
   */
  public static Location locatePointInRing(Point point, final LineString ring) {
    if (point == null) {
      return Location.EXTERIOR;
    } else {
      point = point.convertGeometry(ring.getGeometryFactory());
      final BoundingBox boundingBox = ring.getBoundingBox();
      if (point.intersectsBbox(boundingBox)) {

        final RayCrossingCounter counter = new RayCrossingCounter(point);

        double x1 = ring.getX(0);
        double y1 = ring.getY(0);
        final int vertexCount = ring.getVertexCount();
        for (int i = 1; i < vertexCount; i++) {
          final double x2 = ring.getX(i);
          final double y2 = ring.getY(i);
          counter.countSegment(x2, y2, x1, y1);
          if (counter.isOnSegment()) {
            return counter.getLocation();
          }
          x1 = x2;
          y1 = y2;
        }
        return counter.getLocation();
      } else {
        return Location.EXTERIOR;
      }
    }
  }

  /**
   * Determines the {@link Location} of a point in a ring.
   * This method is an exemplar of how to use this class.
   *
   * @param p the point to test
   * @param ring an array of Point forming a ring
   * @return the location of the point in the ring
   */
  public static Location locatePointInRing(final Point p, final Point[] ring) {
    final RayCrossingCounter counter = new RayCrossingCounter(p);

    for (int i = 1; i < ring.length; i++) {
      final Point p1 = ring[i];
      final Point p2 = ring[i - 1];
      counter.countSegment(p1, p2);
      if (counter.isOnSegment()) {
        return counter.getLocation();
      }
    }
    return counter.getLocation();
  }

  private int crossingCount = 0;

  // true if the test point lies on an input segment
  private boolean pointOnSegment = false;

  private double x;

  private double y;

  public RayCrossingCounter() {
  }

  public RayCrossingCounter(final double x, final double y) {
    this.x = x;
    this.y = y;
  }

  public RayCrossingCounter(final Point point) {
    this(point.getX(), point.getY());
  }

  @Override
  public void accept(final LineSegment segment) {
    final double x1 = segment.getX(0);
    final double y1 = segment.getY(0);
    final double x2 = segment.getX(1);
    final double y2 = segment.getY(1);

    countSegment(x1, y1, x2, y2);
  }

  public void countSegment(final double x1, final double y1, final double x2, final double y2) {
    final double x = this.x;
    final double y = this.y;
    if (x1 < x && x2 < x) {
      // check if the segment is strictly to the left of the test point
    } else if (x == x2 && y == y2) {
      // check if the point is equal to the current ring vertex
      this.pointOnSegment = true;
    } else if (y1 == y && y2 == y) {
      /**
       * For horizontal segments, check if the point is on the segment. Otherwise,
       * horizontal segments are not counted.
       */
      double minX = x1;
      double maxX = x2;
      if (minX > maxX) {
        minX = x2;
        maxX = x1;
      }
      if (x >= minX && x <= maxX) {
        this.pointOnSegment = true;
      }
    } else if (y1 > y && y2 <= y || y2 > y && y1 <= y) {
      /**
       * Evaluate all non-horizontal segments which cross a horizontal ray to the
       * right of the test pt. To avoid double-counting shared vertices, we use
       * the convention that
       * <ul>
       * <li>an upward edge includes its starting endpoint, and excludes its final
       * endpoint
       * <li>a downward edge excludes its starting endpoint, and includes its
       * final endpoint
       * </ul>
       */
      // translate the segment so that the test point lies on the origin
      final double deltaX1 = x1 - x;
      final double deltaY1 = y1 - y;
      final double deltaX2 = x2 - x;
      final double deltaY2 = y2 - y;

      /**
       * The translated segment straddles the x-axis. Compute the sign of the
       * ordinate of intersection with the x-axis. (y2 != y1, so denominator
       * will never be 0.0)
       */
      double xIntSign = RobustDeterminant.signOfDet2x2(deltaX1, deltaY1, deltaX2, deltaY2);
      if (xIntSign == 0.0) {
        this.pointOnSegment = true;
      } else {
        if (deltaY2 < deltaY1) {
          xIntSign = -xIntSign;
        }

        // The segment crosses the ray if the sign is strictly positive.
        if (xIntSign > 0.0) {
          this.crossingCount++;
        }
      }
    }
  }

  public void countSegment(final LineSegment segment) {
    final double x1 = segment.getX(0);
    final double y1 = segment.getY(0);
    final double x2 = segment.getX(1);
    final double y2 = segment.getY(1);
    countSegment(x1, y1, x2, y2);
  }

  /**
   * For each segment, check if it crosses a horizontal ray running from the
   * test point in the positive x direction.
   *
   * @param p1 an endpoint of the segment
   * @param p2 another endpoint of the segment
   */
  public void countSegment(final Point p1, final Point p2) {

    final double x1 = p1.getX();
    final double y1 = p1.getY();
    final double x2 = p2.getX();
    final double y2 = p2.getY();

    countSegment(x1, y1, x2, y2);
  }

  /**
   * Gets the {@link Location} of the point relative to
   * the ring, polygon
   * or multipolygon from which the processed segments were provided.
   * <p>
   * This method only determines the correct location
   * if <b>all</b> relevant segments must have been processed.
   *
   * @return the Location of the point
   */
  public Location getLocation() {
    if (this.pointOnSegment) {
      return Location.BOUNDARY;
    } else if (this.crossingCount % 2 == 1) {
      // The point is in the interior of the ring if the number of X-crossings
      // is
      // odd.
      return Location.INTERIOR;
    } else {
      return Location.EXTERIOR;
    }
  }

  public double getX() {
    return this.x;
  }

  public double getY() {
    return this.y;
  }

  public boolean isDisjoint() {
    if (this.pointOnSegment) {
      return false;
    } else if (this.crossingCount % 2 == 1) {
      // The point is in the interior of the ring if the number of X-crossings
      // is
      // odd.
      return false;
    } else {
      return true;
    }
  }

  public boolean isIntersects() {
    if (this.pointOnSegment) {
      return true;
    } else if (this.crossingCount % 2 == 1) {
      // The point is in the interior of the ring if the number of X-crossings
      // is
      // odd.
      return true;
    } else {
      return false;
    }
  }

  public boolean isIntersects(final double x, final double y, final List<LineSegment> segments) {
    reset(x, y);
    for (final LineSegment segment : segments) {
      countSegment(segment);
    }
    return isIntersects();
  }

  /**
   * Reports whether the point lies exactly on one of the supplied segments.
   * This method may be called at any time as segments are processed.
   * If the result of this method is <tt>true</tt>,
   * no further segments need be supplied, since the result
   * will never change again.
   *
   * @return true if the point lies exactly on a segment
   */
  public boolean isOnSegment() {
    return this.pointOnSegment;
  }

  /**
   * Tests whether the point lies in or on
   * the ring, polygon
   * or multipolygon from which the processed segments were provided.
   * <p>
   * This method only determines the correct location
   * if <b>all</b> relevant segments must have been processed.
   *
   * @return true if the point lies in or on the supplied polygon
   */
  public boolean isPointInPolygon() {
    return getLocation() != Location.EXTERIOR;
  }

  public void reset() {
    this.crossingCount = 0;
    this.pointOnSegment = false;
  }

  public void reset(final double x, final double y) {
    this.crossingCount = 0;
    this.pointOnSegment = false;
    this.x = x;
    this.y = y;
  }

  public void resetX(final double x) {
    this.crossingCount = 0;
    this.pointOnSegment = false;
    this.x = x;
  }

  public void resetY(final double y) {
    this.crossingCount = 0;
    this.pointOnSegment = false;
    this.y = y;
  }

  public void setPointOnSegment(final boolean pointOnSegment) {
    this.pointOnSegment = pointOnSegment;
  }

  public void setXY(final double x, final double y) {
    this.x = x;
    this.y = y;
  }
}
