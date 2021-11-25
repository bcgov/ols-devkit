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
package com.revolsys.geometry.model.impl;

import java.io.StringWriter;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.record.io.format.wkt.EWktWriter;

/**
 * Models an OGC SFS <code>LinearRing</code>.
 * A <code>LinearRing</code> is a {@link LineString} which is both closed and simple.
 * In other words,
 * the first and last coordinate in the ring must be equal,
 * and the interior of the ring must not self-intersect.
 * Either orientation of the ring is allowed.
 * <p>
 * A ring must have either 0 or 4 or more points.
 * The first and last points must be equal (in 2D).
 * If these conditions are not met, the constructors throw
 * an {@link IllegalArgumentException}
 *
 * @version 1.7
 */
public class LinearRingDoubleGf extends LineStringDoubleGf implements LinearRing {
  private static final long serialVersionUID = -4261142084085851829L;

  public LinearRingDoubleGf(final GeometryFactory factory) {
    super(factory);
  }

  /**
   * Constructs a <code>LinearRing</code> with the vertices
   * specifed by the given {@link LineString}.
   *
   *@param  coordinates  a sequence points forming a closed and simple linestring, or
   *      <code>null</code> to create the empty geometry.
   *
   * @throws IllegalArgumentException if the ring is not closed, or has too few points
   *
   */
  public LinearRingDoubleGf(final GeometryFactory factory, final int axisCount,
    final int vertexCount, final double... coordinates) {
    super(factory, axisCount, vertexCount, coordinates);
    validate();
  }

  public LinearRingDoubleGf(final int axisCount, final double... coordinates) {
    this(GeometryFactory.DEFAULT_2D, axisCount, coordinates.length / axisCount, coordinates);
  }

  @Override
  public LinearRingDoubleGf clone() {
    return (LinearRingDoubleGf)super.clone();
  }

  /**
   * Tests whether this ring is closed.
   * Empty rings are closed by definition.
   *
   * @return true if this ring is closed
   */
  @Override
  public boolean isClosed() {
    if (isEmpty()) {
      // empty LinearRings are closed by definition
      return true;
    } else {
      return super.isClosed();
    }
  }

  @Override
  public LinearRing newGeometry(final GeometryFactory geometryFactory) {
    return (LinearRing)super.newGeometry(geometryFactory);
  }

  @Override
  public LinearRing reverse() {
    final int vertexCount = getVertexCount();
    final int axisCount = getAxisCount();
    final double[] coordinates = new double[vertexCount * axisCount];
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        final int coordinateIndex = (vertexCount - 1 - vertexIndex) * axisCount + axisIndex;
        coordinates[coordinateIndex] = getCoordinate(vertexIndex, axisIndex);
      }
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    final LinearRing reverseLine = geometryFactory.linearRing(axisCount, coordinates);
    return reverseLine;

  }

  private void validate() {
    if (isClosed()) {
      final int vertexCount = getVertexCount();
      if (vertexCount >= 1 && vertexCount <= 2) {
        throw new IllegalArgumentException("Invalid number of points in LinearRing (found "
          + vertexCount + " - must be 0 or >= 3): " + this);
      }
    } else {
      final StringWriter out = new StringWriter();
      EWktWriter.write(out, (LineString)this);
      throw new IllegalArgumentException(
        "Points of LinearRing do not form a closed linestring: " + out);
    }
  }
}
