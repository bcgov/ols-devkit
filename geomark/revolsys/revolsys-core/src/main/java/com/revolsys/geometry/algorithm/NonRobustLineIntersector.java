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

import org.jeometry.common.number.Doubles;

/**
 * A non-robust version of {@link LineIntersector}.
 *
 * @version 1.7
 */
public class NonRobustLineIntersector extends LineIntersector {
  /**
   * @return true if both numbers are positive or if both numbers are negative.
   * Returns false if both numbers are zero.
   */
  public static boolean isSameSignAndNonZero(final double a, final double b) {
    if (a == 0 || b == 0) {
      return false;
    }
    return a < 0 && b < 0 || a > 0 && b > 0;
  }

  public NonRobustLineIntersector() {
  }

  /*
   * p1-p2 and p3-p4 are assumed to be collinear (although not necessarily
   * intersecting). Returns: DONT_INTERSECT : the two segments do not intersect
   * COLLINEAR : the segments intersect, in the line segment pa-pb. pa-pb is in
   * the same direction as p1-p2 DO_INTERSECT : the inputLines intersect in a
   * single point only, pa
   */
  private int computeCollinearIntersection(final double line1x1, final double line1y1,
    final double line1x2, final double line1y2, final double line2x1, final double line2y1,
    final double line2x2, final double line2y2) {
    final double r1 = 0;
    final double r2 = 1;
    final double r3 = rParameter(line1x1, line1y1, line1x2, line1y2, line2x1, line2y1);
    final double r4 = rParameter(line1x1, line1y1, line1x2, line1y2, line2x2, line2y2);
    double q3x;
    double q3y;
    double q4x;
    double q4y;
    double t3;
    double t4;
    // make sure p3-p4 is in same direction as p1-p2
    if (r3 < r4) {
      q3x = line2x1;
      q3y = line2y1;
      q4x = line2x2;
      q4y = line2y2;
      t3 = r3;
      t4 = r4;
    } else {
      q3x = line2x2;
      q3y = line2y2;
      q4x = line2x1;
      q4y = line2y1;
      t3 = r4;
      t4 = r3;
    }
    if (t3 > r2 || t4 < r1) {
      // check for no intersection
      return NO_INTERSECTION;
    } else if (line1x1 == q4x && line1y1 == q4y) {
      // check for single point intersection
      this.pointAX = line1x1;
      this.pointAY = line1y1;
      return POINT_INTERSECTION;
    } else if (line1x2 == q3x && line1y2 == q3y) {
      this.pointAX = line1x2;
      this.pointAY = line1y2;
      return POINT_INTERSECTION;
    } else {

      // intersection MUST be a segment - compute endpoints
      if (t3 > r1) {
        this.pointAX = line2x1;
        this.pointAY = line2y1;
      } else {
        this.pointAX = line1x1;
        this.pointAY = line1y1;
      }
      if (t4 < r2) {
        this.pointBX = line2x2;
        this.pointBY = line2y2;
      } else {
        this.pointBX = line1x2;
        this.pointBY = line1y2;
      }
      return COLLINEAR_INTERSECTION;
    }
  }

  private int computeIntersect(final double line1x1, final double line1y1, final double line1x2,
    final double line1y2, final double line2x1, final double line2y1, final double line2x2,
    final double line2y2) {
    this.isProper = false;

    /*
     * Compute a1, b1, c1, where line joining points 1 and 2 is
     * "a1 x  +  b1 y  +  c1  =  0".
     */
    final double a1 = line1y2 - line1y1;
    final double b1 = line1x1 - line1x2;
    final double c1 = line1x2 * line1y1 - line1x1 * line1y2;

    /*
     * Compute r3 and r4.
     */
    final double r3 = a1 * line2x1 + b1 * line2y1 + c1;
    final double r4 = a1 * line2x2 + b1 * line2y2 + c1;

    /*
     * Check signs of r3 and r4. If both point 3 and point 4 lie on same side of
     * line 1, the line segments do not intersect.
     */
    if (r3 != 0 && r4 != 0 && isSameSignAndNonZero(r3, r4)) {
      return NO_INTERSECTION;
    }

    /*
     * Coefficients of line eqns.
     */
    final double a2 = line2y2 - line2y1;
    final double b2 = line2x1 - line2x2;
    final double c2 = line2x2 * line2y1 - line2x1 * line2y2;

    /*
     * Compute r1 and r2
     */
    final double r1 = a2 * line1x1 + b2 * line1y1 + c2;
    final double r2 = a2 * line1x2 + b2 * line1y2 + c2;

    /*
     * Check signs of r1 and r2. If both point 1 and point 2 lie on same side of
     * second line segment, the line segments do not intersect.
     */
    if (r1 != 0 && r2 != 0 && isSameSignAndNonZero(r1, r2)) {
      return NO_INTERSECTION;
    }

    /**
     *  Line segments intersect: compute intersection point.
     */
    final double denom = a1 * b2 - a2 * b1;
    if (denom == 0) {
      return computeCollinearIntersection(line1x1, line1y1, line1x2, line1y2, line2x1, line2y1,
        line2x2, line2y2);
    }
    final double numX = b1 * c2 - b2 * c1;
    /*
     * TESTING ONLY double valX = (( num < 0 ? num - offset : num + offset ) /
     * denom); double valXInt = (int) (( num < 0 ? num - offset : num + offset )
     * / denom); if (valXInt != pa.x) // TESTING ONLY System.out.println(val +
     * " - int: " + valInt + ", floor: " + pa.x);
     */
    final double numY = a2 * c1 - a1 * c2;
    this.pointAX = numX / denom;
    this.pointAY = numY / denom;

    // check if this is a proper intersection BEFORE truncating values,
    // to avoid spurious equality comparisons with endpoints
    this.isProper = true;
    if (this.pointAX == line1x1 && this.pointAY == line1y1
      || this.pointAX == line1x2 && this.pointAY == line1y2
      || this.pointAX == line2x1 && this.pointAY == line2y1
      || this.pointAX == line2x2 && this.pointAY == line2y2) {
      this.isProper = false;
    }

    this.pointAX = Doubles.makePrecise(this.scale, this.pointAX);
    this.pointAY = Doubles.makePrecise(this.scale, this.pointAY);
    return POINT_INTERSECTION;
  }

  @Override
  public boolean computeIntersectionLine(final double line1x1, final double line1y1,
    final double line1x2, final double line1y2, final double line2x1, final double line2y1,
    final double line2x2, final double line2y2) {
    this.line1x1 = line1x1;
    this.line1y1 = line1y1;
    this.line1x2 = line1x2;
    this.line1y2 = line1y2;
    this.line2x1 = line2x1;
    this.line2y1 = line2y1;
    this.line2x2 = line2x2;
    this.line2y2 = line2y2;
    this.intersectionCount = computeIntersect(line1x1, line1y1, line1x2, line1y2, line2x1, line2y1,
      line2x2, line2y2);
    return hasIntersection();
  }

  @Override
  public boolean computeIntersectionPoint(final double x1, final double y1, final double x2,
    final double y2, final double x, final double y) {
    this.isProper = false;

    /*
     * Compute a1, b1, c1, where line joining points 1 and 2 is
     * "a1 x  +  b1 y  +  c1  =  0".
     */
    final double a1 = y2 - y1;
    final double b1 = x1 - x2;
    final double c1 = x2 * y1 - x1 * y2;

    /*
     * Compute r3 and r4.
     */
    final double r = a1 * x + b1 * y + c1;

    // if r != 0 the point does not lie on the line
    if (r != 0) {
      this.intersectionCount = NO_INTERSECTION;
      return false;
    }

    // Point lies on line - check to see whether it lies in line segment.

    final double dist = rParameter(x, y, x1, y1, x2, y2);
    if (dist < 0.0 || dist > 1.0) {
      this.intersectionCount = NO_INTERSECTION;
      return false;
    }

    this.isProper = true;
    if (x == x1 && y == y1 || x == x2 && y == y2) {
      this.isProper = false;
    }
    this.intersectionCount = POINT_INTERSECTION;
    return true;
  }

  /**
   *  RParameter computes the parameter for the point p
   *  in the parameterized equation
   *  of the line from p1 to p2.
   *  This is equal to the 'distance' of p along p1-p2
   */
  private double rParameter(final double x, final double y, final double x1, final double y1,
    final double x2, final double y2) {
    final double dx = Math.abs(x2 - x1);
    final double dy = Math.abs(y2 - y1);
    // compute maximum delta, for numerical stability
    // also handle case of p1-p2 being vertical or horizontal
    double r;
    if (dx > dy) {
      r = (x - x1) / (x2 - x1);
    } else {
      r = (y - y1) / (y2 - y1);
    }
    return r;
  }

}
