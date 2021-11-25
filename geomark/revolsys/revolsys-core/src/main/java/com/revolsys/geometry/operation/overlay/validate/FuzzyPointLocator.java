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

package com.revolsys.geometry.operation.overlay.validate;

import com.revolsys.geometry.algorithm.PointLocator;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;

/**
 * Finds the most likely {@link Location} of a point relative to
 * the polygonal components of a geometry, using a tolerance value.
 * If a point is not clearly in the Interior or Exterior,
 * it is considered to be on the Boundary.
 * In other words, if the point is within the tolerance of the Boundary,
 * it is considered to be on the Boundary; otherwise,
 * whether it is Interior or Exterior is determined directly.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class FuzzyPointLocator {
  private final double boundaryDistanceTolerance;

  private final Geometry g;

  private final Lineal linework;

  private final PointLocator ptLocator = new PointLocator();

  public FuzzyPointLocator(final Geometry g, final double boundaryDistanceTolerance) {
    this.g = g;
    this.boundaryDistanceTolerance = boundaryDistanceTolerance;
    this.linework = extractLinework(g);
  }

  /**
   * Extracts linework for polygonal components.
   *
   * @param geometry the geometry from which to extract
   * @return a lineal geometry containing the extracted linework
   */
  private Lineal extractLinework(final Geometry geometry) {
    final GeometryFactory geometryFactory = geometry.getGeometryFactory();
    if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      return geometryFactory.lineal(polygon.getRings());
    } else {
      return geometryFactory.lineString();
    }
  }

  public Location getLocation(final Point pt) {
    if (isWithinToleranceOfBoundary(pt)) {
      return Location.BOUNDARY;
      /*
       * double dist = linework.distance(point); // if point is close to
       * boundary, it is considered to be on the boundary if (dist < tolerance)
       * return Location.BOUNDARY;
       */
    }

    // now we know point must be clearly inside or outside geometry, so return
    // actual location value

    return this.ptLocator.locate(this.g, pt);
  }

  private boolean isWithinToleranceOfBoundary(final Point pt) {
    final double x = pt.getX();
    final double y = pt.getY();
    for (int i = 0; i < this.linework.getGeometryCount(); i++) {
      final LineString line = (LineString)this.linework.getGeometry(i);
      for (int j = 0; j < line.getVertexCount() - 1; j++) {
        final double x1 = line.getX(j);
        final double y1 = line.getY(j + 1);
        final double x2 = line.getX(j);
        final double y2 = line.getY(j + 1);
        final double dist = LineSegmentUtil.distanceLinePoint(x1, y1, x2, y2, x, y);
        if (dist <= this.boundaryDistanceTolerance) {
          return true;
        }
      }
    }
    return false;
  }
}
