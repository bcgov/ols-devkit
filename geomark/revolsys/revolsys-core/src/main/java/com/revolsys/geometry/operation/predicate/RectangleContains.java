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

package com.revolsys.geometry.operation.predicate;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;

/**
 * Optimized implementation of the <tt>contains</tt> spatial predicate
 * for cases where the first {@link Geometry} is a rectangle.
 * This class works for all input geometries.
 * <p>
 * As a further optimization,
 * this class can be used to test
 * many geometries against a single
 * rectangle in a slightly more efficient way.
 *
 * @version 1.7
 */
public class RectangleContains {

  /**
   * Tests whether a rectangle contains a given geometry.
   *
   * @param rectangle a rectangular Polygon
   * @param b a Geometry of any type
   * @return true if the geometries intersect
   */
  public static boolean contains(final Polygonal rectangle, final Geometry b) {
    final RectangleContains rc = new RectangleContains(rectangle);
    return rc.contains(b);
  }

  private final BoundingBox rectEnv;

  /**
   * Construct a new new contains computer for two geometries.
   *
   * @param rectangle a rectangular geometry
   */
  public RectangleContains(final Polygonal rectangle) {
    this.rectEnv = rectangle.getBoundingBox();
  }

  public boolean contains(final Geometry geom) {
    // the test geometry must be wholly contained in the rectangle envelope
    if (!this.rectEnv.bboxCovers(geom.getBoundingBox())) {
      return false;
    }

    /**
     * Check that geom is not contained entirely in the rectangle boundary.
     * According to the somewhat odd spec of the SFS, if this
     * is the case the geometry is NOT contained.
     */
    if (isContainedInBoundary(geom)) {
      return false;
    }
    return true;
  }

  private boolean isContainedInBoundary(final Geometry geom) {
    // polygons can never be wholely contained in the boundary
    if (geom instanceof Polygon) {
      return false;
    }
    if (geom instanceof Point) {
      return isPointContainedInBoundary((Point)geom);
    }
    if (geom instanceof LineString) {
      return isLineStringContainedInBoundary((LineString)geom);
    }

    for (int i = 0; i < geom.getGeometryCount(); i++) {
      final Geometry comp = geom.getGeometry(i);
      if (!isContainedInBoundary(comp)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Tests if a line segment is contained in the boundary of the target rectangle.
   * @param p0 an endpoint of the segment
   * @param p1 an endpoint of the segment
   * @return true if the line segment is contained in the boundary
   */
  private boolean isLineSegmentContainedInBoundary(final Point p0, final Point p1) {
    if (p0.equals(p1)) {
      return isPointContainedInBoundary(p0);
    }

    // we already know that the segment is contained in the rectangle envelope
    if (p0.getX() == p1.getX()) {
      if (p0.getX() == this.rectEnv.getMinX() || p0.getX() == this.rectEnv.getMaxX()) {
        return true;
      }
    } else if (p0.getY() == p1.getY()) {
      if (p0.getY() == this.rectEnv.getMinY() || p0.getY() == this.rectEnv.getMaxY()) {
        return true;
      }
    }
    /**
     * Either
     *   both x and y values are different
     * or
     *   one of x and y are the same, but the other ordinate is not the same as a boundary ordinate
     *
     * In either case, the segment is not wholely in the boundary
     */
    return false;
  }

  /**
   * Tests if a linestring is completely contained in the boundary of the target rectangle.
   * @param line the linestring to test
   * @return true if the linestring is contained in the boundary
   */
  private boolean isLineStringContainedInBoundary(final LineString line) {
    final LineString seq = line;
    for (int i = 0; i < seq.getVertexCount() - 1; i++) {
      final Point p0 = seq.getPoint(i);
      final Point p1 = seq.getPoint(i + 1);

      if (!isLineSegmentContainedInBoundary(p0, p1)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Tests if a point is contained in the boundary of the target rectangle.
   *
   * @param point the point to test
   * @return true if the point is contained in the boundary
   */
  private boolean isPointContainedInBoundary(final Point point) {
    /**
     * contains = false iff the point is properly contained in the rectangle.
     *
     * This code assumes that the point lies in the rectangle envelope
     */
    return point.getX() == this.rectEnv.getMinX() || point.getX() == this.rectEnv.getMaxX()
      || point.getY() == this.rectEnv.getMinY() || point.getY() == this.rectEnv.getMaxY();
  }

}
