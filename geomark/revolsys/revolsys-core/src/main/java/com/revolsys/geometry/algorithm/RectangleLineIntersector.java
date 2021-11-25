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

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;

/**
 * Computes whether a rectangle intersects line segments.
 * <p>
 * Rectangles contain a large amount of inherent symmetry
 * (or to put it another way, although they contain four
 * coordinates they only actually contain 4 ordinates
 * worth of information).
 * The algorithm used takes advantage of the symmetry of
 * the geometric situation
 * to optimize performance by minimizing the number
 * of line intersection tests.
 *
 * @author Martin Davis
 *
 */
public class RectangleLineIntersector {
  private final Point diagDown0;

  private final Point diagDown1;

  private final Point diagUp0;

  private final Point diagUp1;

  // for intersection testing, don't need to set precision model
  private final LineIntersector li = new RobustLineIntersector();

  private final BoundingBox boundingBox;

  /**
   * Creates a new intersector for the given query rectangle,
   * specified as an {@link BoundingBox}.
   *
   *
   * @param boundingBox the query rectangle, specified as an BoundingBox
   */
  public RectangleLineIntersector(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;

    /**
     * Up and Down are the diagonal orientations
     * relative to the Left side of the rectangle.
     * Index 0 is the left side, 1 is the right side.
     */
    this.diagUp0 = new PointDoubleXY(boundingBox.getMinX(), boundingBox.getMinY());
    this.diagUp1 = new PointDoubleXY(boundingBox.getMaxX(), boundingBox.getMaxY());
    this.diagDown0 = new PointDoubleXY(boundingBox.getMinX(), boundingBox.getMaxY());
    this.diagDown1 = new PointDoubleXY(boundingBox.getMaxX(), boundingBox.getMinY());
  }

  /**
   * Tests whether the query rectangle intersects a
   * given line segment.
   *
   * @param p0 the first endpoint of the segment
   * @param p1 the second endpoint of the segment
   * @return true if the rectangle intersects the segment
   */
  public boolean intersects(Point p0, Point p1) {
    final double x1 = p0.getX();
    final double y1 = p0.getY();
    final double x2 = p1.getX();
    final double y2 = p1.getY();

    /**
     * If the segment envelope is disjoint from the
     * rectangle envelope, there is no intersection
     */
    if (this.boundingBox.bboxIntersects(x1, y1)) {
      return true;
    } else if (this.boundingBox.bboxIntersects(x2, y2)) {
      return true;
    } else if (this.boundingBox.bboxIntersects(x1, y1, x2, y2)) {
      /**
       * Compute angle of segment.
       * Since the segment is normalized to run left to right,
       * it is sufficient to simply test the Y ordinate.
       * "Upwards" means relative to the left end of the segment.
       */
      boolean isSegUpwards;

      /**
       * Normalize segment.
       * This makes p0 less than p1,
       * so that the segment runs to the right,
       * or vertically upwards.
       */
      if (p0.compareTo(p1) > 0) {
        final Point tmp = p0;
        p0 = p1;
        p1 = tmp;
        isSegUpwards = y1 > y2;
      } else {
        isSegUpwards = y2 > y1;
      }

      /**
       * Since we now know that neither segment endpoint
       * lies in the rectangle, there are two possible
       * situations:
       * 1) the segment is disjoint to the rectangle
       * 2) the segment crosses the rectangle completely.
       *
       * In the case of a crossing, the segment must intersect
       * a diagonal of the rectangle.
       *
       * To distinguish these two cases, it is sufficient
       * to test intersection with
       * a single diagonal of the rectangle,
       * namely the one with slope "opposite" to the slope
       * of the segment.
       * (Note that if the segment is axis-parallel,
       * it must intersect both diagonals, so this is
       * still sufficient.)
       */
      if (isSegUpwards) {
        this.li.computeIntersectionPoints(p0, p1, this.diagDown0, this.diagDown1);
      } else {
        this.li.computeIntersectionPoints(p0, p1, this.diagUp0, this.diagUp1);
      }
      if (this.li.hasIntersection()) {
        return true;
      }
    }
    return false;
  }
}
