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

import com.revolsys.geometry.algorithm.RayCrossingCounter;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygonal;

/**
 * Determines the {@link Location} of {@link Coordinates}s relative to
 * a {@link Polygonal} geometry, using indexing for efficiency.
 * This algorithm is suitable for use in cases where
 * many points will be tested against a given area.
 *
 * Thread-safe and immutable.
 *
 * @author Martin Davis
 *
 */
public class IndexedPointInAreaLocator implements PointOnGeometryLocator {

  private final Geometry geometry;

  private final GeometrySegmentYIntervalIndex index;

  /**
   * Creates a new locator for a given {@link Geometry}
   *
   * @param geometry the Geometry to locate in
   */
  public IndexedPointInAreaLocator(final Geometry geometry) {
    if (!(geometry instanceof Polygonal || geometry instanceof LinearRing)) {
      throw new IllegalArgumentException("Argument must be Polygonal or LinearRing");
    }
    this.geometry = geometry;
    this.index = new GeometrySegmentYIntervalIndex(geometry);
  }

  public Geometry getGeometry() {
    return this.geometry;
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometry.getGeometryFactory();
  }

  public GeometrySegmentYIntervalIndex getIndex() {
    return this.index;
  }

  public boolean intersects(final Point point) {
    final Location location = locate(point);
    return !location.equals(Location.EXTERIOR);
  }

  @Override
  public Location locate(final double x, final double y) {
    final RayCrossingCounter visitor = new RayCrossingCounter(x, y);
    this.index.query(y, visitor);

    return visitor.getLocation();
  }

  /**
   * Determines the {@link Location} of a point in an areal {@link Geometry}.
   *
   * @param p the point to test
   * @return the location of the point in the geometry
   */
  @Override
  public Location locate(final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    return locate(x, y);
  }

}
