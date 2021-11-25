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

import org.jeometry.common.exception.Exceptions;
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
import com.revolsys.geometry.model.GeometryCollection;
import com.revolsys.geometry.model.GeometryDataType;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.editor.AbstractGeometryEditor;
import com.revolsys.geometry.model.editor.GeometryCollectionImplEditor;
import com.revolsys.geometry.model.editor.GeometryEditor;

/**
 * Models a collection of {@link Geometry}s of
 * arbitrary type and dimension.
 *
 *
 *@version 1.7
 */
public class GeometryCollectionImpl implements GeometryCollection {
  private static final long serialVersionUID = -5694727726395021467L;

  /**
   *  The bounding box of this <code>Geometry</code>.
   */
  private BoundingBox boundingBox;

  /**
   *  Internal representation of this <code>GeometryCollection</code>.
   */
  private Geometry[] geometries;

  /**
   * The {@link GeometryFactory} used to create this Geometry
   */
  private final GeometryFactory geometryFactory;

  /**
   * @param geometries
   *            the <code>Geometry</code>s for this <code>GeometryCollection</code>,
   *            or <code>null</code> or an empty array to create the empty
   *            geometry. Elements may be empty <code>Geometry</code>s,
   *            but not <code>null</code>s.
   */
  public GeometryCollectionImpl(final GeometryFactory geometryFactory,
    final Geometry[] geometries) {
    this.geometryFactory = geometryFactory;
    if (geometries == null || geometries.length == 0) {
      throw new IllegalArgumentException("GeometryCollection must not be empty");
    } else if (Geometry.hasNullElements(geometries)) {
      throw new IllegalArgumentException("geometries must not contain null elements");
    } else {
      this.geometries = geometries;
    }
  }

  /**
   * Creates and returns a full copy of this  object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public GeometryCollectionImpl clone() {
    try {
      final GeometryCollectionImpl geometryCollection = (GeometryCollectionImpl)super.clone();
      geometryCollection.geometries = new Geometry[this.geometries.length];
      for (int i = 0; i < this.geometries.length; i++) {
        geometryCollection.geometries[i] = this.geometries[i].clone();
      }
      return geometryCollection;
    } catch (final CloneNotSupportedException e) {
      throw Exceptions.wrap(e);
    }
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
  public <R> R findSegment(final Function4Double<R> action) {
    for (final Geometry geometry : this.geometries) {
      final R result = geometry.findSegment(action);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Override
  public <R> R findVertex(final BiFunctionDouble<R> action) {
    for (final Geometry geometry : this.geometries) {
      final R result = geometry.findVertex(action);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Override
  public void forEachGeometry(final Consumer<Geometry> action) {
    if (this.geometries != null) {
      for (final Geometry geometry : this.geometries) {
        action.accept(geometry);
      }
    }
  }

  @Override
  public void forEachPolygon(final Consumer<Polygon> action) {
    if (this.geometries != null) {
      for (final Geometry geometry : this.geometries) {
        if (geometry instanceof Polygon) {
          final Polygon polygon = (Polygon)geometry;
          action.accept(polygon);
        } else if (geometry instanceof GeometryCollection) {
          geometry.forEachPolygon(action);
        }
      }
    }
  }

  @Override
  public void forEachSegment(final Consumer4Double action) {
    for (final Geometry geometry : this.geometries) {
      geometry.forEachSegment(action);
    }
  }

  @Override
  public void forEachVertex(final BiConsumerDouble action) {
    for (final Geometry geometry : this.geometries) {
      geometry.forEachVertex(action);
    }
  }

  @Override
  public void forEachVertex(final Consumer3Double action) {
    for (final Geometry editor : this.geometries) {
      editor.forEachVertex(action);
    }
  }

  @Override
  public void forEachVertex(final CoordinatesOperation coordinatesOperation,
    final CoordinatesOperationPoint point, final Consumer<CoordinatesOperationPoint> action) {
    for (final Geometry geometry : this.geometries) {
      geometry.forEachVertex(coordinatesOperation, point, action);
    }
  }

  @Override
  public void forEachVertex(final CoordinatesOperationPoint coordinates,
    final Consumer<CoordinatesOperationPoint> action) {
    for (final Geometry geometry : this.geometries) {
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
  public GeometryDataType<?, ?> getDataType() {
    return GeometryDataTypes.GEOMETRY_COLLECTION;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> List<V> getGeometries() {
    return (List<V>)Lists.newArray(this.geometries);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V getGeometry(final int n) {
    return (V)this.geometries[n];
  }

  @Override
  public int getGeometryCount() {
    return this.geometries.length;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public int getSegmentCount() {
    int segmentCount = 0;
    for (final Geometry geometry : geometries()) {
      segmentCount += geometry.getSegmentCount();
    }
    return segmentCount;
  }

  @Override
  public boolean hasGeometryType(final GeometryDataType<?, ?> dataType) {
    for (final Geometry geometry : this.geometries) {
      if (geometry.hasGeometryType(dataType)) {
        return true;
      }
    }
    return false;
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
  public Geometry intersectionBbox(final BoundingBox boundingBox) {
    notNullSameCs(boundingBox);
    if (bboxCoveredBy(boundingBox)) {
      return this;
    } else {
      final List<Geometry> parts = new ArrayList<>();
      for (final Geometry part : this.geometries) {
        final Geometry partIntersection = part.intersectionBbox(boundingBox);
        if (!partIntersection.isEmpty()) {
          parts.add(part);
        }
      }
      if (parts.size() == this.geometries.length) {
        return this;
      } else {
        return this.geometryFactory.geometry(parts);
      }
    }
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean isHomogeneousGeometryCollection() {
    return false;
  }

  @Override
  public GeometryEditor<?> newGeometryEditor() {
    return new GeometryCollectionImplEditor(this);
  }

  @Override
  public GeometryEditor<?> newGeometryEditor(final AbstractGeometryEditor<?> parentEditor) {
    return new GeometryCollectionImplEditor((GeometryCollectionImplEditor)parentEditor, this);
  }

  @Override
  public Geometry prepare() {
    return this;
  }

  @Override
  public String toString() {
    return toEwkt();
  }

}
