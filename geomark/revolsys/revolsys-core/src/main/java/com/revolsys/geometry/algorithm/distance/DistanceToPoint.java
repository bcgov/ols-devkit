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
package com.revolsys.geometry.algorithm.distance;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.model.segment.LineSegment;

/**
 * Computes the Euclidean distance (L2 metric) from a {@link Point} to a {@link Geometry}.
 * Also computes two points on the geometry which are separated by the distance found.
 */
public class DistanceToPoint {

  public static void computeDistance(final Geometry geom, final Point point,
    final PointPairDistance pointDistance) {
    if (geom instanceof LineString) {
      final LineString line = (LineString)geom;
      computeDistance(line, point, pointDistance);
    } else if (geom instanceof Polygon) {
      final Polygon polygon = (Polygon)geom;
      computeDistance(polygon, point, pointDistance);
    } else if (geom.isGeometryCollection()) {
      for (final Geometry part : geom.geometries()) {
        computeDistance(part, point, pointDistance);
      }
    } else { // assume geom is Point
      pointDistance.setMinimum(geom.getPoint(), point);
    }
  }

  public static void computeDistance(final LineSegment segment, final Point pt,
    final PointPairDistance pointDistance) {
    final Point closestPt = segment.closestPoint(pt);
    pointDistance.setMinimum(closestPt, pt);
  }

  public static void computeDistance(final LineString line, final double x, final double y,
    final PointPairDistance pointDistance) {
    final int vertexCount = line.getVertexCount();
    if (vertexCount > 0) {
      double x1 = line.getX(0);
      double y1 = line.getY(0);
      for (int vertexIndex = 1; vertexIndex < vertexCount; vertexIndex++) {
        final double x2 = line.getX(vertexIndex);
        final double y2 = line.getY(vertexIndex);
        final Point closestPoint = LineSegmentUtil.closestPoint(x1, y1, x2, y2, x, y);
        final double closestX = closestPoint.getX();
        final double closestY = closestPoint.getY();
        pointDistance.setMinimum(closestX, closestY, x, y);
        x1 = x2;
        y1 = y2;
      }
    }
  }

  public static void computeDistance(final LineString line, final Point point,
    final PointPairDistance pointDistance) {
    final double x = point.getX();
    final double y = point.getY();
    computeDistance(line, x, y, pointDistance);
  }

  public static void computeDistance(final Polygon poly, final Point point,
    final PointPairDistance pointDistance) {
    final double x = point.getX();
    final double y = point.getY();

    for (final LinearRing ring : poly.rings()) {
      computeDistance(ring, x, y, pointDistance);
    }
  }

  public DistanceToPoint() {
  }
}
