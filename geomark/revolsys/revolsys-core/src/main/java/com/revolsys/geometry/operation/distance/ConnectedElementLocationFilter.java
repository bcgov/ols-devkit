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
package com.revolsys.geometry.operation.distance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;

/**
 * A ConnectedElementPointFilter extracts a single point
 * from each connected element in a Geometry
 * (e.g. a polygon, linestring or point)
 * and returns them in a list. The elements of the list are
 * {@link com.revolsys.geometry.operation.distance.GeometryLocation}s.
 *
 * @version 1.7
 */
public class ConnectedElementLocationFilter {

  /**
   * Returns a list containing a point from each Polygon, LineString, and Point
   * found inside the specified geometry. The elements of the list
   * are {@link com.revolsys.geometry.operation.distance.GeometryLocation}s.
   */
  public static List<GeometryLocation> getLocations(final Geometry geometry) {
    final List<GeometryLocation> locations = new ArrayList<>();
    for (final Geometry part : geometry.geometries()) {
      if (part instanceof Point || part instanceof LineString || part instanceof Polygon) {
        locations.add(new GeometryLocation(part, 0, part.getPoint()));
      }
    }
    return locations;
  }

  /**
   * Returns a list containing a point from each Polygon, LineString, and Point
   * found inside the specified geometry.
   */
  public static List<Point> getPoints(final Geometry geometry) {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      return Collections.singletonList(point);
    } else if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      return Collections.singletonList(line.getPoint());
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      return Collections.singletonList(polygon.getPoint());
    } else {
      final List<Point> points = new ArrayList<>();
      for (final Geometry part : geometry.geometries()) {
        if (part instanceof Point || part instanceof LineString || part instanceof Polygon) {
          points.add(part.getPoint());
        }
      }
      return points;
    }
  }
}
