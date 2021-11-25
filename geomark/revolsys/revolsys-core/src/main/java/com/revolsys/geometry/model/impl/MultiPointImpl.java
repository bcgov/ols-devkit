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

import java.util.List;
import java.util.function.Consumer;

import org.jeometry.common.function.BiConsumerDouble;
import org.jeometry.common.function.BiFunctionDouble;
import org.jeometry.common.function.Consumer3Double;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.MultiPoint;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Punctual;

/**
 * Models a collection of {@link Point}s.
 * <p>
 * Any collection of Point is a valid MultiPoint.
 *
 *@version 1.7
 */
public class MultiPointImpl implements MultiPoint {
  private static final long serialVersionUID = -8048474874175355449L;

  /**
   *  The bounding box of this <code>Geometry</code>.
   */
  private BoundingBox boundingBox;

  private final GeometryFactory geometryFactory;

  private Point[] points;

  public MultiPointImpl(final GeometryFactory geometryFactory, final Point... points) {
    this.geometryFactory = geometryFactory;
    if (points == null || points.length == 0) {
      throw new IllegalArgumentException("MultiLineString must not be empty");
    } else if (Geometry.hasNullElements(points)) {
      throw new IllegalArgumentException("geometries must not contain null elements");
    } else {
      this.points = points;
    }
  }

  /**
   * Creates and returns a full copy of this object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public Punctual clone() {
    final Point[] newPoints = this.points.clone();
    return newPunctual(this.geometryFactory, newPoints);
  }

  /**
   * Tests whether this geometry is structurally and numerically equal
   * to a given <code>Object</code>.
   * If the argument <code>Object</code> is not a <code>Geometry</code>,
   * the result is <code>false</code>.
   * Otherwise, the result is computed using
   * {@link #equals(2,Geometry)}.
   * <p>
   * This method is provided to fulfill the Java contract
   * for value-based object equality.
   * In conjunction with {@link #hashCode()}
   * it provides semantics which are most useful
   * for using
   * <code>Geometry</code>s as keys and values in Java collections.
   * <p>
   * Note that to produce the expected result the input geometries
   * should be in normal form.  It is the caller's
   * responsibility to perform this where required
   * (using {@link Geometry#norm()
   * or {@link #normalize()} as appropriate).
   *
   * @param other the Object to compare
   * @return true if this geometry is exactly equal to the argument
   *
   * @see #equals(2,Geometry)
   * @see #hashCode()
   * @see #norm()
   * @see #normalize()
   */
  @Override
  public boolean equals(final Object other) {
    if (other instanceof Geometry) {
      final Geometry geometry = (Geometry)other;
      return equals(2, geometry);
    } else {
      return false;
    }
  }

  @Override
  public <R> R findVertex(final BiFunctionDouble<R> action) {
    for (final Geometry geometry : this.points) {
      final R result = geometry.findVertex(action);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Override
  public void forEachGeometry(final Consumer<Geometry> action) {
    if (this.points != null) {
      for (final Geometry geometry : this.points) {
        action.accept(geometry);
      }
    }
  }

  @Override
  public void forEachVertex(final BiConsumerDouble action) {
    for (final Point point : this.points) {
      point.forEachVertex(action);
    }
  }

  @Override
  public void forEachVertex(final Consumer3Double action) {
    for (final Geometry geometry : this.points) {
      geometry.forEachVertex(action);
    }
  }

  @Override
  public void forEachVertex(final CoordinatesOperation coordinatesOperation,
    final CoordinatesOperationPoint point, final Consumer<CoordinatesOperationPoint> action) {
    for (final Geometry geometry : this.points) {
      geometry.forEachVertex(coordinatesOperation, point, action);
    }
  }

  @Override
  public void forEachVertex(final CoordinatesOperationPoint coordinates,
    final Consumer<CoordinatesOperationPoint> action) {
    for (final Geometry geometry : this.points) {
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

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> List<V> getGeometries() {
    return (List<V>)Lists.newArray(this.points);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V getGeometry(final int n) {
    return (V)this.points[n];
  }

  @Override
  public int getGeometryCount() {
    return this.points.length;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  /**
   * Gets a hash code for the Geometry.
   *
   * @return an integer value suitable for use as a hashcode
   */

  @Override
  public int hashCode() {
    return getBoundingBox().hashCode();
  }

  @Override
  public boolean hasPoint(final double x, final double y) {
    for (final Point point : this.points) {
      if (point.hasPoint(x, y)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public String toString() {
    return toEwkt();
  }

}
