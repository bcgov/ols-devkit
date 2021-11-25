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
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.util.RectangleUtil;

/**
 * A robust version of {@link LineIntersector}.
 *
 * @version 1.7
 * @see RobustDeterminant
 */
public class RobustLineIntersector extends LineIntersector {

  public static boolean lineBboxCovers(double minX, double minY, double maxX, double maxY,
    final double x, final double y) {
    if (minX > maxX) {
      final double t = minX;
      minX = maxX;
      maxX = t;
    }
    if (minY > maxY) {
      final double t = minY;
      minY = maxY;
      maxY = t;
    }
    return x >= minX && x <= maxX && y >= minY && y <= maxY;
  }

  public RobustLineIntersector() {
  }

  public RobustLineIntersector(final double scale) {
    super(scale);
  }

  private int computeCollinearIntersection(final double line1x1, final double line1y1,
    final double line1x2, final double line1y2, final double line2x1, final double line2y1,
    final double line2x2, final double line2y2) {
    final boolean p1q1p2 = RectangleUtil.intersectsPoint(line1x1, line1y1, line1x2, line1y2,
      line2x1, line2y1);
    final boolean p1q2p2 = RectangleUtil.intersectsPoint(line1x1, line1y1, line1x2, line1y2,
      line2x2, line2y2);
    final boolean q1p1q2 = RectangleUtil.intersectsPoint(line2x1, line2y1, line2x2, line2y2,
      line1x1, line1y1);
    final boolean q1p2q2 = RectangleUtil.intersectsPoint(line2x1, line2y1, line2x2, line2y2,
      line1x2, line1y2);

    if (p1q1p2 && p1q2p2) {
      this.intersectionX1 = line2x1;
      this.intersectionY1 = line2y1;
      this.intersectionX2 = line2x2;
      this.intersectionY2 = line2y2;
      return COLLINEAR_INTERSECTION;
    } else if (q1p1q2 && q1p2q2) {
      this.intersectionX1 = line1x1;
      this.intersectionY1 = line1y1;
      this.intersectionX2 = line1x2;
      this.intersectionY2 = line1y2;
      return COLLINEAR_INTERSECTION;
    } else if (p1q1p2 && q1p1q2) {
      this.intersectionX1 = line2x1;
      this.intersectionY1 = line2y1;
      if (line2x1 == line1x1 && line2y1 == line1y1 && !p1q2p2 && !q1p2q2) {
        return POINT_INTERSECTION;
      } else {
        this.intersectionX2 = line1x1;
        this.intersectionY2 = line1y1;
        return COLLINEAR_INTERSECTION;
      }
    } else if (p1q1p2 && q1p2q2) {
      this.intersectionX1 = line2x1;
      this.intersectionY1 = line2y1;
      if (line2x1 == line1x2 && line2y1 == line1y2 && !p1q2p2 && !q1p1q2) {
        return POINT_INTERSECTION;
      } else {
        this.intersectionX2 = line1x2;
        this.intersectionY2 = line1y2;
        return COLLINEAR_INTERSECTION;
      }
    } else if (p1q2p2 && q1p1q2) {
      this.intersectionX1 = line2x2;
      this.intersectionY1 = line2y2;
      if (line2x2 == line1x1 && line2y2 == line1y1 && !p1q1p2 && !q1p2q2) {
        return POINT_INTERSECTION;
      } else {
        this.intersectionX2 = line1x1;
        this.intersectionY2 = line1y1;
        return COLLINEAR_INTERSECTION;
      }
    } else if (p1q2p2 && q1p2q2) {
      this.intersectionX1 = line2x2;
      this.intersectionY1 = line2y2;
      if (line2x2 == line1x2 && line2y2 == line1y2 && !p1q1p2 && !q1p1q2) {
        return POINT_INTERSECTION;
      } else {
        this.intersectionX2 = line1x2;
        this.intersectionY2 = line1y2;
        return COLLINEAR_INTERSECTION;
      }
    } else {
      return NO_INTERSECTION;
    }
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
    this.isProper = false;

    // first try a fast test to see if the envelopes of the lines intersect
    if (!RectangleUtil.intersectsMinMax(line1x1, line1y1, line1x2, line1y2, line2x1, line2y1,
      line2x2, line2y2)) {
      this.intersectionCount = NO_INTERSECTION;
      return false;
    }

    // for each endpoint, compute which side of the other segment it lies
    // if both endpoints lie on the same side of the other segment,
    // the segments do not intersect
    final int Pq1 = CGAlgorithmsDD.orientationIndex(line1x1, line1y1, line1x2, line1y2, line2x1,
      line2y1);
    final int Pq2 = CGAlgorithmsDD.orientationIndex(line1x1, line1y1, line1x2, line1y2, line2x2,
      line2y2);

    if (Pq1 > 0 && Pq2 > 0 || Pq1 < 0 && Pq2 < 0) {
      this.intersectionCount = NO_INTERSECTION;
      return false;
    }

    final int Qp1 = CGAlgorithmsDD.orientationIndex(line2x1, line2y1, line2x2, line2y2, line1x1,
      line1y1);
    final int Qp2 = CGAlgorithmsDD.orientationIndex(line2x1, line2y1, line2x2, line2y2, line1x2,
      line1y2);

    if (Qp1 > 0 && Qp2 > 0 || Qp1 < 0 && Qp2 < 0) {
      this.intersectionCount = NO_INTERSECTION;
      return false;
    }

    final boolean collinear = Pq1 == 0 && Pq2 == 0 && Qp1 == 0 && Qp2 == 0;
    if (collinear) {
      this.intersectionCount = computeCollinearIntersection(line1x1, line1y1, line1x2, line1y2,
        line2x1, line2y1, line2x2, line2y2);
      return this.intersectionCount != NO_INTERSECTION;
    }

    /**
     * At this point we know that there is a single intersection point
     * (since the lines are not collinear).
     */

    /**
     *  Check if the intersection is an endpoint. If it is, copy the endpoint as
     *  the intersection point. Copying the point rather than computing it
     *  ensures the point has the exact value, which is important for
     *  robustness. It is sufficient to simply check for an endpoint which is on
     *  the other line, since at this point we know that the inputLines must
     *  intersect.
     */
    if (Pq1 == 0 || Pq2 == 0 || Qp1 == 0 || Qp2 == 0) {
      this.isProper = false;

      /**
       * Check for two equal endpoints.
       * This is done explicitly rather than by the orientation tests
       * below in order to improve robustness.
       *
       * [An example where the orientation tests fail to be consistent is
       * the following (where the true intersection is at the shared endpoint
       * POINT (19.850257749638203 46.29709338043669)
       *
       * LINESTRING ( 19.850257749638203 46.29709338043669, 20.31970698357233 46.76654261437082 )
       * and
       * LINESTRING ( -48.51001596420236 -22.063180333403878, 19.850257749638203 46.29709338043669 )
       *
       * which used to produce the INCORRECT result: (20.31970698357233, 46.76654261437082, NaN)
       *
       */
      if (line1x1 == line2x1 && line1y1 == line2y1 || line1x1 == line2x2 && line1y1 == line2y2) {
        this.intersectionX1 = line1x1;
        this.intersectionY1 = line1y1;
      } else if (line1x2 == line2x1 && line1y2 == line2y1
        || line1x2 == line2x2 && line1y2 == line2y2) {
        this.intersectionX1 = line1x2;
        this.intersectionY1 = line1y2;
      } else if (Pq1 == 0) {
        /**
         * Now check to see if any endpoint lies on the interior of the other segment.
         */
        this.intersectionX1 = line2x1;
        this.intersectionY1 = line2y1;
      } else if (Pq2 == 0) {
        this.intersectionX1 = line2x2;
        this.intersectionY1 = line2y2;
      } else if (Qp1 == 0) {
        this.intersectionX1 = line1x1;
        this.intersectionY1 = line1y1;
      } else if (Qp2 == 0) {
        this.intersectionX1 = line1x2;
        this.intersectionY1 = line1y2;
      }
    } else {
      this.isProper = true;
      final Point intersectionPoint = intersection(line1x1, line1y1, line1x2, line1y2, line2x1,
        line2y1, line2x2, line2y2);
      this.intersectionX1 = intersectionPoint.getX();
      this.intersectionY1 = intersectionPoint.getY();
    }
    this.intersectionCount = POINT_INTERSECTION;
    return true;
  }

  @Override
  public boolean computeIntersectionPoint(final double x1, final double y1, final double x2,
    final double y2, final double x, final double y) {
    this.isProper = false;
    // do between check first, since it is faster than the orientation test
    if (RectangleUtil.intersectsPoint(x1, y1, x2, y2, x, y)) {
      if (CGAlgorithmsDD.orientationIndex(x1, y1, x2, y2, x, y) == 0
        && CGAlgorithmsDD.orientationIndex(x2, y2, x1, y1, x, y) == 0) {
        this.isProper = true;
        if (x == x1 && y == y1 || x == x2 && y == y2) {
          this.isProper = false;
        }
        this.intersectionCount = POINT_INTERSECTION;
        return true;
      }
    }
    this.intersectionCount = NO_INTERSECTION;
    return false;
  }

  /**
   * This method computes the actual value of the intersection point.
   * To obtain the maximum precision from the intersection calculation,
   * the coordinates are normalized by subtracting the minimum
   * ordinate values (in absolute value).  This has the effect of
   * removing common significant digits from the calculation to
   * maintain more bits of precision.
   */
  private Point intersection(final double line1x1, final double line1y1, final double line1x2,
    final double line1y2, final double line2x1, final double line2y1, final double line2x2,
    final double line2y2) {
    double minX1;
    double maxX1;
    if (line1x1 <= line1x2) {
      minX1 = line1x1;
      maxX1 = line1x2;
    } else {
      minX1 = line1x2;
      maxX1 = line1x1;
    }
    double minY1;
    double maxY1;
    if (line1y1 <= line1y2) {
      minY1 = line1y1;
      maxY1 = line1y2;
    } else {
      minY1 = line1y2;
      maxY1 = line1y1;
    }

    double minX2;
    double maxX2;
    if (line2x1 <= line2x2) {
      minX2 = line2x1;
      maxX2 = line2x2;
    } else {
      minX2 = line2x2;
      maxX2 = line2x1;
    }
    double minY2;
    double maxY2;
    if (line2y1 <= line2y2) {
      minY2 = line2y1;
      maxY2 = line2y2;
    } else {
      minY2 = line2y2;
      maxY2 = line2y1;
    }

    final double intMinX = minX1 > minX2 ? minX1 : minX2;
    final double intMaxX = maxX1 < maxX2 ? maxX1 : maxX2;
    final double intMinY = minY1 > minY2 ? minY1 : minY2;
    final double intMaxY = maxY1 < maxY2 ? maxY1 : maxY2;

    final double normX = (intMinX + intMaxX) / 2.0;
    final double normY = (intMinY + intMaxY) / 2.0;
    final double line1x11 = line1x1 - normX;
    final double line1y11 = line1y1 - normY;
    final double line1x21 = line1x2 - normX;
    final double line1y21 = line1y2 - normY;
    final double line2x11 = line2x1 - normX;
    final double line2y11 = line2y1 - normY;
    final double line2x21 = line2x2 - normX;
    final double line2y21 = line2y2 - normY;
    Point intersectionPoint;
    try {
      intersectionPoint = HCoordinate.intersection(line1x11, line1y11, line1x21, line1y21, line2x11,
        line2y11, line2x21, line2y21);
    } catch (final NotRepresentableException e) {
      // compute an approximate result
      intersectionPoint = CentralEndpointIntersector.getIntersection(line1x11, line1y11, line1x21,
        line1y21, line2x11, line2y11, line2x21, line2y21);
    }

    final double x1 = intersectionPoint.getX() + normX;
    final double y1 = intersectionPoint.getY() + normY;
    final Point point = newPoint(x1, y1);

    final double x = point.getX();
    final double y = point.getY();
    /**
     * Due to rounding it can happen that the computed intersection is
     * outside the envelopes of the input segments.  Clearly this
     * is inconsistent.
     * This code checks this condition and forces a more reasonable answer
     *
     * MD - May 4 2005 - This is still a problem.  Here is a failure case:
     *
     * LINESTRING (2089426.5233462777 1180182.3877339689, 2085646.6891757075 1195618.7333999649)
     * LINESTRING (1889281.8148903656 1997547.0560044837, 2259977.3672235999 483675.17050843034)
     * int point = (2097408.2633752143,1144595.8008114607)
     *
     * MD - Dec 14 2006 - This does not seem to be a failure case any longer
     */
    final boolean inSegmentEnvelopes = x >= minX1 && x <= maxX1 && y >= minY1 && y <= maxY1
      && x >= minX2 && x <= maxX2 && y >= minY2 && y <= maxY2;
    if (!inSegmentEnvelopes) {
      return nearestEndpoint(line1x1, line1y1, line1x2, line1y2, line2x1, line2y1, line2x2,
        line2y2);
    }
    return point;
  }

  /**
   * Finds the endpoint of the segments P and Q which
   * is closest to the other segment.
   * This is a reasonable surrogate for the true
   * intersection points in ill-conditioned cases
   * (e.g. where two segments are nearly coincident,
   * or where the endpoint of one segment lies almost on the other segment).
   * <p>
   * This replaces the older CentralEndpoint heuristic,
   * which chose the wrong endpoint in some cases
   * where the segments had very distinct slopes
   * and one endpoint lay almost on the other segment.
   *
   * @param p1 an endpoint of segment P
   * @param p2 an endpoint of segment P
   * @param q1 an endpoint of segment Q
   * @param q2 an endpoint of segment Q
   * @return the nearest endpoint to the other segment
   */
  private Point nearestEndpoint(final double line1x1, final double line1y1, final double line1x2,
    final double line1y2, final double line2x1, final double line2y1, final double line2x2,
    final double line2y2) {
    double x = line1x1;
    double y = line1y1;
    double minDist = LineSegmentUtil.distanceLinePoint(line2x1, line2y1, line2x2, line2y2, line1x1,
      line1y1);

    double dist = LineSegmentUtil.distanceLinePoint(line2x1, line2y1, line2x2, line2y2, line1x2,
      line1y2);
    if (dist < minDist) {
      minDist = dist;
      x = line1x2;
      y = line1y2;
    }
    dist = LineSegmentUtil.distanceLinePoint(line1x1, line1y1, line1x2, line1y2, line2x1, line2y1);
    if (dist < minDist) {
      minDist = dist;
      x = line2x1;
      y = line2y1;
    }
    dist = LineSegmentUtil.distanceLinePoint(line1x1, line1y1, line1x2, line1y2, line2x2, line2y2);
    if (dist < minDist) {
      minDist = dist;
      x = line2x2;
      y = line2y2;
    }
    return newPoint(x, y);
  }

  private Point newPoint(double x, double y) {
    final double scale = this.scale;
    if (scale > 0) {
      x = Math.round(x * scale) / scale;
      y = Math.round(y * scale) / scale;
    }
    return new PointDoubleXY(x, y);
  }
}
