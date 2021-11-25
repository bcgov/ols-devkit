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

package com.revolsys.geometry.noding.snapround;

import com.revolsys.geometry.algorithm.LineIntersector;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.noding.NodedSegmentString;
import com.revolsys.geometry.util.Assert;

/**
 * Implements a "hot pixel" as used in the Snap Rounding algorithm.
 * A hot pixel contains the interior of the tolerance square and
 * the boundary
 * <b>minus</b> the top and right segments.
 * <p>
 * The hot pixel operations are all computed in the integer domain
 * to avoid rounding problems.
 *
 * @version 1.7
 */
public class HotPixel extends PointDoubleXY {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private static final double SAFE_ENV_EXPANSION_FACTOR = 0.75;

  private final LineIntersector li;

  private final double maxx;

  private final double maxy;

  private final double minx;

  private final double miny;

  private final Point originalPt;

  private BoundingBox safeEnv = null;

  private final double scaleFactor;

  /**
   * Creates a new hot pixel, using a given scale factor.
   * The scale factor must be strictly positive (non-zero).
   *
   * @param point the coordinate at the centre of the pixel
   * @param scaleFactor the scaleFactor determining the pixel size.  Must be > 0
   * @param li the intersector to use for testing intersection with line segments
   *
   */
  public HotPixel(final Point point, final double scaleFactor, final LineIntersector li) {
    this.originalPt = point;
    this.x = point.getX();
    this.y = point.getY();
    this.scaleFactor = scaleFactor;
    this.li = li;
    // tolerance = 0.5;
    if (scaleFactor <= 0) {
      throw new IllegalArgumentException("Scale factor must be > 0");
    }
    if (scaleFactor == 1.0) {
    } else {
      this.x = scale(point.getX());
      this.y = scale(point.getY());
    }
    this.minx = this.x - 0.5;
    this.maxx = this.x + 0.5;
    this.miny = this.y - 0.5;
    this.maxy = this.y + 0.5;
  }

  /**
   * Adds a new node (equal to the snap pt) to the specified segment
   * if the segment passes through the hot pixel
   *
   * @param segStr
   * @param segIndex
   * @return true if a node was added to the segment
   */
  public boolean addSnappedNode(final NodedSegmentString segStr, final int segIndex) {
    double x1 = segStr.getX(segIndex);
    double y1 = segStr.getY(segIndex);
    double x2 = segStr.getX(segIndex + 1);
    double y2 = segStr.getY(segIndex + 1);

    if (this.scaleFactor != 1.0) {
      x1 = scale(x1);
      y1 = scale(y1);
      x2 = scale(x2);
      y2 = scale(y2);
    }
    if (intersectsScaled(x1, y1, x2, y2)) {
      segStr.addIntersection(this, segIndex);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Gets the coordinate this hot pixel is based at.
   *
   * @return the coordinate of the pixel
   */
  public Point getCoordinate() {
    return this.originalPt;
  }

  /**
   * Returns a "safe" envelope that is guaranteed to contain the hot pixel.
   * The envelope returned will be larger than the exact envelope of the
   * pixel.
   *
   * @return an envelope which contains the hot pixel
   */
  public BoundingBox getSafeEnvelope() {
    if (this.safeEnv == null) {
      final double safeTolerance = SAFE_ENV_EXPANSION_FACTOR / this.scaleFactor;
      this.safeEnv = new BoundingBoxDoubleXY(this.originalPt.getX() - safeTolerance,
        this.originalPt.getY() - safeTolerance, this.originalPt.getX() + safeTolerance,
        this.originalPt.getY() + safeTolerance);
    }
    return this.safeEnv;
  }

  private boolean intersectsScaled(final double x1, final double y1, final double x2,
    final double y2) {
    final double segMinx = Math.min(x1, x2);
    final double segMaxx = Math.max(x1, x2);
    final double segMiny = Math.min(y1, y2);
    final double segMaxy = Math.max(y1, y2);

    final boolean isOutsidePixelEnv = this.maxx < segMinx || this.minx > segMaxx
      || this.maxy < segMiny || this.miny > segMaxy;
    if (isOutsidePixelEnv) {
      return false;
    }
    final boolean intersects = intersectsToleranceSquare(x1, y1, x2, y2);

    Assert.isTrue(!(isOutsidePixelEnv && intersects), "Found bad envelope test");

    return intersects;
  }

  /**
   * Tests whether the segment p0-p1 intersects the hot pixel tolerance square.
   * Because the tolerance square point set is partially open (along the
   * top and right) the test needs to be more sophisticated than
   * simply checking for any intersection.
   * However, it can take advantage of the fact that the hot pixel edges
   * do not lie on the coordinate grid.
   * It is sufficient to check if any of the following occur:
   * <ul>
   * <li>a proper intersection between the segment and any hot pixel edge
   * <li>an intersection between the segment and <b>both</b> the left and bottom hot pixel edges
   * (which detects the case where the segment intersects the bottom left hot pixel corner)
   * <li>an intersection between a segment endpoint and the hot pixel coordinate
   * </ul>
   *
   * @param p0
   * @param p1
   * @return
   */
  private boolean intersectsToleranceSquare(final double x1, final double y1, final double x2,
    final double y2) {
    boolean intersectsLeft = false;
    boolean intersectsBottom = false;

    this.li.computeIntersectionLine(x1, y1, x2, y2, this.maxx, this.maxy, this.minx, this.maxy);
    if (this.li.isProper()) {
      return true;
    }

    this.li.computeIntersectionLine(x1, y1, x2, y2, this.minx, this.maxy, this.minx, this.miny);
    if (this.li.isProper()) {
      return true;
    }
    if (this.li.hasIntersection()) {
      intersectsLeft = true;
    }

    this.li.computeIntersectionLine(x1, y1, x2, y2, this.minx, this.miny, this.maxx, this.miny);
    if (this.li.isProper()) {
      return true;
    }
    if (this.li.hasIntersection()) {
      intersectsBottom = true;
    }

    this.li.computeIntersectionLine(x1, y1, x2, y2, this.maxx, this.miny, this.maxx, this.maxy);
    if (this.li.isProper()) {
      return true;
    }

    if (intersectsLeft && intersectsBottom) {
      return true;
    }

    if (equals(x1, y1)) {
      return true;
    }
    if (equals(x2, y2)) {
      return true;
    }

    return false;
  }

  private double scale(final double val) {
    return Math.round(val * this.scaleFactor);
  }

}
