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
package com.revolsys.geometry.algorithm.locate;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;

/**
 * Computes the location of points
 * relative to a {@link Polygonal} {@link Geometry},
 * using a simple O(n) algorithm.
 * This algorithm is suitable for use in cases where
 * only one or a few points will be tested against a given area.
 * <p>
 * The algorithm used is only guaranteed to return correct results
 * for points which are <b>not</b> on the boundary of the Geometry.
 *
 * @version 1.7
 */
public class SimplePointInAreaLocator implements PointOnGeometryLocator {

  private static boolean containsPoint(final Geometry geometry, final double x, final double y) {
    if (geometry instanceof Polygon) {
      return containsPointInPolygon((Polygon)geometry, x, y);
    } else if (geometry.isGeometryCollection()) {
      for (final Geometry part : geometry.geometries()) {
        if (containsPoint(part, x, y)) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean containsPointInPolygon(final Polygon polygon, final double x,
    final double y) {
    if (polygon.isEmpty()) {
      return false;
    } else {
      final LinearRing shell = polygon.getShell();
      if (!isPointInRing(shell, x, y)) {
        return false;
      } else {
        for (final LinearRing hole : polygon.holes()) {
          if (isPointInRing(hole, x, y)) {
            return false;
          }
        }
        return true;
      }
    }
  }

  /**
   * Determines whether a point lies in a LinearRing,
   * using the ring envelope to short-circuit if possible.
   * @param ring a linear ring
   * @param point the point to test
   *
   * @return true if the point lies inside the ring
   */
  private static boolean isPointInRing(final LinearRing ring, final double x, final double y) {
    if (ring.getBoundingBox().bboxIntersects(x, y)) {
      return ring.isPointInRing(x, y);
    } else {
      return false;
    }
  }

  public static Location locate(final Geometry geometry, final double x, final double y) {
    if (geometry.isEmpty()) {
      return Location.EXTERIOR;
    } else if (containsPoint(geometry, x, y)) {
      return Location.INTERIOR;
    } else {
      return Location.EXTERIOR;
    }
  }

  /**
   * Determines the {@link Location} of a point in an areal {@link Geometry}.
   * Currently this will never return a value of BOUNDARY.
   *
   * @param p the point to test
   * @param geometry the areal geometry to test
   * @return the Location of the point in the geometry
   */
  public static Location locate(final Point p, final Geometry geom) {
    final double x = p.getX();
    final double y = p.getY();
    return locate(geom, x, y);
  }

  private final Geometry geometry;

  public SimplePointInAreaLocator(final Geometry geom) {
    this.geometry = geom;
  }

  @Override
  public Location locate(final double x, final double y) {
    return SimplePointInAreaLocator.locate(this.geometry, x, y);
  }

  @Override
  public Location locate(final Point p) {
    return SimplePointInAreaLocator.locate(p, this.geometry);
  }
}
