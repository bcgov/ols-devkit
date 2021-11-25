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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jeometry.common.function.BiConsumerDouble;
import org.jeometry.common.function.BiFunctionDouble;
import org.jeometry.common.function.Consumer3Double;
import org.jeometry.common.function.Consumer4Double;
import org.jeometry.common.function.Function4Double;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Polygon;

/**
 * Represents a polygon with linear edges, which may include holes.
 * The outer boundary (shell)
 * and inner boundaries (holes) of the polygon are represented by {@link LinearRing}s.
 * The boundary rings of the polygon may have any orientation.
 * Polygons are closed, simple geometries by definition.
 * <p>
 * The polygon model conforms to the assertions specified in the
 * <A HREF="http://www.opengis.org/techno/specs.htm">OpenGIS Simple Features
 * Specification for SQL</A>.
 * <p>
 * A <code>Polygon</code> is topologically valid if and only if:
 * <ul>
 * <li>the coordinates which define it are valid coordinates
 * <li>the linear rings for the shell and holes are valid
 * (i.e. are closed and do not self-intersect)
 * <li>holes touch the shell or another hole at at most one point
 * (which implies that the rings of the shell and holes must not cross)
 * <li>the interior of the polygon is connected,
 * or equivalently no sequence of touching holes
 * makes the interior of the polygon disconnected
 * (i.e. effectively split the polygon into two pieces).
 * </ul>
 *
 *@version 1.7
 */
public class PolygonImpl extends AbstractPolygon {
  private static final long serialVersionUID = 1L;

  private BoundingBox boundingBox;

  /**
   * The {@link GeometryFactory} used to create this Geometry
   */
  private final GeometryFactory geometryFactory;

  private LinearRing[] rings;

  public PolygonImpl(final GeometryFactory factory, final LinearRing ring) {
    this.geometryFactory = factory;
    if (ring == null || ring.isEmpty()) {
      throw new IllegalArgumentException("Polygon ring must not be empty");
    } else {
      this.rings = new LinearRing[] {
        ring
      };
    }
  }

  /**
   *  Constructs a <code>Polygon</code> with the given exterior boundary and
   *  interior boundaries.
   *
   *@param  shell           the outer boundary of the new <code>Polygon</code>,
   *      or <code>null</code> or an empty <code>LinearRing</code> if the empty
   *      geometry is to be created.
   *@param  holes           the inner boundaries of the new <code>Polygon</code>
   *      , or <code>null</code> or empty <code>LinearRing</code>s if the empty
   *      geometry is to be created.
   */
  public PolygonImpl(final GeometryFactory factory, final LinearRing[] rings, final int ringCount) {
    this.geometryFactory = factory;
    if (rings == null || ringCount == 0) {
      throw new IllegalArgumentException("Polygon must not be empty");
    } else {
      this.rings = new LinearRing[ringCount];
      for (int i = 0; i < rings.length; i++) {
        final LinearRing ring = rings[i];
        this.rings[i] = ring;
      }
    }
  }

  /**
   * Creates and returns a full copy of this {@link Polygon} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public PolygonImpl clone() {
    final PolygonImpl poly = (PolygonImpl)super.clone();
    poly.rings = this.rings.clone();
    for (int i = 0; i < this.rings.length; i++) {
      poly.rings[i] = this.rings[i].clone();
    }
    return poly;
  }

  @Override
  public <R> R findSegment(final Function4Double<R> action) {
    for (final Geometry geometry : this.rings) {
      final R result = geometry.findSegment(action);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Override
  public <R> R findVertex(final BiFunctionDouble<R> action) {
    for (final Geometry geometry : this.rings) {
      final R result = geometry.findVertex(action);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Override
  public void forEachGeometry(final Consumer<Geometry> action) {
    for (final LinearRing ring : this.rings) {
      ring.forEachGeometry(action);
    }
  }

  @Override
  public void forEachSegment(final Consumer4Double action) {
    for (final Geometry geometry : this.rings) {
      geometry.forEachSegment(action);
    }
  }

  @Override
  public void forEachVertex(final BiConsumerDouble action) {
    for (final LinearRing ring : this.rings) {
      ring.forEachVertex(action);
    }
  }

  @Override
  public void forEachVertex(final Consumer3Double action) {
    for (final Geometry geometry : this.rings) {
      geometry.forEachVertex(action);
    }
  }

  @Override
  public void forEachVertex(final CoordinatesOperation coordinatesOperation,
    final CoordinatesOperationPoint point, final Consumer<CoordinatesOperationPoint> action) {
    for (final Geometry geometry : this.rings) {
      geometry.forEachVertex(coordinatesOperation, point, action);
    }
  }

  @Override
  public void forEachVertex(final CoordinatesOperationPoint coordinates,
    final Consumer<CoordinatesOperationPoint> action) {
    for (final Geometry geometry : this.rings) {
      geometry.forEachVertex(coordinates, action);
    }
  }

  @Override
  public int getAxisCount() {
    return this.geometryFactory.getAxisCount();
  }

  @Override
  public BoundingBox getBoundingBox() {
    if (this.boundingBox == null) {
      this.boundingBox = newBoundingBox();
    }
    return this.boundingBox;
  }

  @Override
  public double getCoordinate(final int partIndex, final int ringIndex, final int vertexIndex,
    final int axisIndex) {
    if (partIndex == 0) {
      return getCoordinate(ringIndex, vertexIndex, axisIndex);
    } else {
      return Double.NaN;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V extends Geometry> List<V> getGeometryComponents(final Class<V> geometryClass) {
    final List<V> geometries = new ArrayList<>();
    if (geometryClass.isAssignableFrom(getClass())) {
      geometries.add((V)this);
    }
    for (final LinearRing ring : this.rings) {
      if (ring != null) {
        final List<V> ringGeometries = ring.getGeometries(geometryClass);
        geometries.addAll(ringGeometries);
      }
    }
    return geometries;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public LinearRing getRing(final int ringIndex) {
    if (ringIndex < 0 || ringIndex >= this.rings.length) {
      return null;
    } else {
      return this.rings[ringIndex];
    }
  }

  @Override
  public int getRingCount() {
    return this.rings.length;
  }

  @Override
  public List<LinearRing> getRings() {
    return Lists.newArray(this.rings);
  }

  @Override
  public boolean isEmpty() {
    return false;
  }
}
