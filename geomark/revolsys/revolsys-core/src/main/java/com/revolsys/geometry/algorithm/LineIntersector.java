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

import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.util.Assert;

/**
 * A <code>LineIntersector</code> is an algorithm that can both test whether
 * two line segments intersect and compute the intersection point(s)
 * if they do.
 * <p>
 * There are three possible outcomes when determining whether two line segments intersect:
 * <ul>
 * <li>{@link #NO_INTERSECTION} - the segments do not intersect
 * <li>{@link #POINT_INTERSECTION - the segments intersect in a single point
 * <li>{@link #COLLINEAR_INTERSECTION - the segments are collinear and they intersect in a line segment
 * </ul>
 * For segments which intersect in a single point, the point may be either an endpoint
 * or in the interior of each segment.
 * If the point lies in the interior of both segments,
 * this is termed a <i>proper intersection</i>.
 * The method {@link #isProper()} test for this situation.
 * <p>
 * The intersection point(s) may be computed in a precise or non-precise manner.
 * Computing an intersection point precisely involves rounding it
 * via a supplied scale.
 * <p>
 * LineIntersectors do not perform an initial envelope intersection test
 * to determine if the segments are disjoint.
 * This is because this class is likely to be used in a context where
 * envelope overlap is already known to occur (or be likely).
 *
 * @version 1.7
 */
public abstract class LineIntersector {
  public final static int COLLINEAR = 2;

  /**
   * Indicates that line segments intersect in a line segment
   */
  public final static int COLLINEAR_INTERSECTION = 2;

  public final static int DO_INTERSECT = 1;

  /**
   * These are deprecated, due to ambiguous naming
   */
  public final static int DONT_INTERSECT = 0;

  /**
   * Indicates that line segments do not intersect
   */
  public final static int NO_INTERSECTION = 0;

  /**
   * Indicates that line segments intersect in a single point
   */
  public final static int POINT_INTERSECTION = 1;

  /**
   * This function is non-robust, since it may compute the square of large numbers.
   * Currently not sure how to improve this.
   */
  public static double nonRobustComputeEdgeDistance(final Point p, final Point p1, final Point p2) {
    final double dx = p.getX() - p1.getX();
    final double dy = p.getY() - p1.getY();
    final double dist = Math.sqrt(dx * dx + dy * dy); // dummy value
    Assert.isTrue(!(dist == 0.0 && !p.equals(p1)), "Invalid distance calculation");
    return dist;
  }

  protected int intersectionCount;

  protected double intersectionX1 = Double.NaN;

  protected double intersectionX2 = Double.NaN;

  protected double intersectionY1 = Double.NaN;

  protected double intersectionY2 = Double.NaN;

  protected boolean isProper;

  protected double line1x1;

  protected double line1x2;

  protected double line1y1;

  protected double line1y2;

  protected double line2x1;

  protected double line2x2;

  protected double line2y1;

  protected double line2y2;

  protected double pointAX = Double.NaN;

  protected double pointAY = Double.NaN;

  protected double pointBX = Double.NaN;

  protected double pointBY = Double.NaN;

  protected final double scale;

  public LineIntersector() {
    this.intersectionCount = 0;
    this.scale = 0;
  }

  public LineIntersector(final double scale) {
    this.scale = scale;
  }

  public abstract boolean computeIntersectionLine(final double line1x1, final double line1y1,
    final double line1x2, final double line1y2, final double line2x1, final double line2y1,
    final double line2x2, final double line2y2);

  /**
   * Compute the intersection of a point p and the line p1-p2.
   * This function computes the boolean value of the hasIntersection test.
   * The actual value of the intersection (if there is one)
   * is equal to the value of <code>p</code>.
   */
  public abstract boolean computeIntersectionPoint(double x1, double y1, double x2, double y2,
    double x, double y);

  /**
   * Computes the intersection of the lines p1-p2 and p3-p4.
   * This function computes both the boolean value of the hasIntersection test
   * and the (approximate) value of the intersection point itself (if there is one).
   */
  public final boolean computeIntersectionPoints(final Point p1, final Point p2, final Point p3,
    final Point p4) {
    final double x1 = p1.getX();
    final double y1 = p1.getY();
    final double x2 = p2.getX();
    final double y2 = p2.getY();
    final double x3 = p3.getX();
    final double y3 = p3.getY();
    final double x4 = p4.getX();
    final double y4 = p4.getY();
    return computeIntersectionLine(x1, y1, x2, y2, x3, y3, x4, y4);
  }

  public boolean equalsIntersection(final int intersectionIndex, final double x, final double y) {
    if (intersectionIndex == 0) {
      return x == this.intersectionX1 && y == this.intersectionY1;
    } else if (intersectionIndex == 1) {
      return x == this.intersectionX2 && y == this.intersectionY2;
    } else {
      throw new ArrayIndexOutOfBoundsException(intersectionIndex);
    }
  }

  /**
   * Computes the "edge distance" of an intersection point along the specified input line segment.
   *
   * @param segmentIndex is 0 or 1
   * @param index is 0 or 1
   *
   * @return the edge distance of the intersection point
   */
  public double getEdgeDistance(final int segmentIndex, final int index) {
    double x;
    double y;
    if (index == 0) {
      x = this.intersectionX1;
      y = this.intersectionY1;
    } else if (index == 1) {
      x = this.intersectionX2;
      y = this.intersectionY2;
    } else {
      throw new ArrayIndexOutOfBoundsException(index);
    }
    final double x1;
    final double y1;
    final double x2;
    final double y2;
    if (segmentIndex == 0) {
      x1 = this.line1x1;
      y1 = this.line1y1;
      x2 = this.line1x2;
      y2 = this.line1y2;
    } else {
      x1 = this.line2x1;
      y1 = this.line2y1;
      x2 = this.line2x2;
      y2 = this.line2y2;
    }

    if (x == x1 && y == y1) {
      return 0.0;
    } else if (x == x2 && y == y2) {
      final double dx = Math.abs(x2 - x1);
      final double dy = Math.abs(y2 - y1);
      if (dx > dy) {
        return dx;
      } else {
        return dy;
      }
    } else {
      final double dx = Math.abs(x2 - x1);
      final double dy = Math.abs(y2 - y1);
      final double pdx = Math.abs(x - x1);
      final double pdy = Math.abs(y - y1);
      double dist;
      if (dx > dy) {
        dist = pdx;
      } else {
        dist = pdy;
      }
      // hack to ensure that non-endpoints always have a non-zero distance
      if (dist == 0.0) {
        dist = Math.max(pdx, pdy);
      }
      return dist;
    }
  }

  /**
   * Gets an endpoint of an input segment.
   *
   * @param segmentIndex the index of the input segment (0 or 1)
   * @param ptIndex the index of the endpoint (0 or 1)
   * @return the specified endpoint
   */
  public Point getEndpoint(final int segmentIndex, final int ptIndex) {
    double x;
    double y;
    if (segmentIndex == 0) {
      if (ptIndex == 0) {
        x = this.line1x1;
        y = this.line1y1;
      } else {
        x = this.line1x2;
        y = this.line1y2;
      }
    } else {
      if (ptIndex == 0) {
        x = this.line2x1;
        y = this.line2y1;
      } else {
        x = this.line2x2;
        y = this.line2y2;
      }
    }

    return new PointDoubleXY(x, y);
  }

  /*
   * public String toString() { String str = inputLines[0][0] + "-" +
   * inputLines[0][1] + " " + inputLines[1][0] + "-" + inputLines[1][1] + " : "
   * + getTopologySummary(); return str; }
   */

  /**
   * Returns the intIndex'th intersection point
   *
   * @param intIndex is 0 or 1
   *
   * @return the intIndex'th intersection point
   */
  public Point getIntersection(final int intersectionIndex) {
    if (intersectionIndex == 0) {
      return new PointDoubleXY(this.intersectionX1, this.intersectionY1);
    } else if (intersectionIndex == 1) {
      return new PointDoubleXY(this.intersectionX2, this.intersectionY2);
    } else {
      throw new ArrayIndexOutOfBoundsException(intersectionIndex);
    }
  }

  /**
   * Returns the number of intersection points found.  This will be either 0, 1 or 2.
   *
   * @return the number of intersection points found (0, 1, or 2)
   */
  public int getIntersectionCount() {
    return this.intersectionCount;
  }

  private String getTopologySummary() {
    final StringBuilder catBuf = new StringBuilder();
    if (isEndPoint()) {
      catBuf.append(" endpoint");
    }
    if (this.isProper) {
      catBuf.append(" proper");
    }
    if (isCollinear()) {
      catBuf.append(" collinear");
    }
    return catBuf.toString();
  }

  /**
   * Tests whether the input geometries intersect.
   *
   * @return true if the input geometries intersect
   */
  public boolean hasIntersection() {
    return this.intersectionCount != NO_INTERSECTION;
  }

  protected boolean isCollinear() {
    return this.intersectionCount == COLLINEAR_INTERSECTION;
  }

  protected boolean isEndPoint() {
    return hasIntersection() && !this.isProper;
  }

  /**
   * Tests whether either intersection point is an interior point of one of the input segments.
   *
   * @return <code>true</code> if either intersection point is in the interior of one of the input segments
   */
  public boolean isInteriorIntersection() {
    if (isInteriorIntersection(0)) {
      return true;
    } else if (isInteriorIntersection(1)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Tests whether either intersection point is an interior point of the specified input segment.
   *
   * @return <code>true</code> if either intersection point is in the interior of the input segment
   */
  public boolean isInteriorIntersection(final int inputLineIndex) {
    if (this.intersectionCount > 0) {
      final double x1;
      final double y1;
      final double x2;
      final double y2;
      if (inputLineIndex == 0) {
        x1 = this.line1x1;
        y1 = this.line1y1;
        x2 = this.line1x2;
        y2 = this.line1y2;
      } else {
        x1 = this.line2x1;
        y1 = this.line2y1;
        x2 = this.line2x2;
        y2 = this.line2y2;
      }
      for (int i = 0; i < this.intersectionCount; i++) {
        if (!(equalsIntersection(i, x1, y1) || equalsIntersection(i, x2, y2))) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isIntersection(final double x, final double y) {
    if (this.intersectionCount == NO_INTERSECTION) {
      return false;
    } else {
      if (this.intersectionX1 == x && this.intersectionY1 == y) {
        return true;
      }
      if (this.intersectionCount == COLLINEAR_INTERSECTION) {
        if (this.intersectionX2 == x && this.intersectionY2 == y) {
          return true;
        }
      }
      return false;
    }
  }

  /**
   * Test whether a point is a intersection point of two line segments.
   * Note that if the intersection is a line segment, this method only tests for
   * equality with the endpoints of the intersection segment.
   * It does <b>not</b> return true if
   * the input point is internal to the intersection segment.
   *
   * @return true if the input point is one of the intersection points.
   */
  public boolean isIntersection(final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    return isIntersection(x, y);
  }

  /**
   * Tests whether an intersection is proper.
   * <br>
   * The intersection between two line segments is considered proper if
   * they intersect in a single point in the interior of both segments
   * (e.g. the intersection is a single point and is not equal to any of the
   * endpoints).
   * <p>
   * The intersection between a point and a line segment is considered proper
   * if the point lies in the interior of the segment (e.g. is not equal to
   * either of the endpoints).
   *
   * @return true if the intersection is proper
   */
  public boolean isProper() {
    return hasIntersection() && this.isProper;
  }

  @Override
  public String toString() {
    return "LINESTRING(" + this.line1x1 + ' ' + this.line1y1 + ',' + this.line1x2 + ' '
      + this.line1y2 + ") - LINESTRING(" + this.line2x1 + ' ' + this.line2y1 + ',' + this.line2x2
      + ' ' + this.line2y2 + ")" + getTopologySummary();
  }
}
