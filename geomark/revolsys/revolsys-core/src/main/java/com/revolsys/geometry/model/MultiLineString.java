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
package com.revolsys.geometry.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import javax.measure.Unit;
import javax.measure.quantity.Area;

import com.revolsys.geometry.graph.linemerge.LineMerger;
import com.revolsys.geometry.model.editor.MultiLineStringEditor;
import com.revolsys.geometry.model.prep.PreparedMultiLineString;
import com.revolsys.geometry.model.segment.MultiLineStringSegment;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.MultiLineStringVertex;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.operation.BoundaryOp;
import com.revolsys.geometry.operation.valid.GeometryValidationError;
import com.revolsys.util.Property;

/**
 * Models a collection of (@link LineString}s.
 * <p>
 * Any collection of LineStrings is a valid MultiLineString.
 *
 *@version 1.7
 */
public interface MultiLineString extends GeometryCollection, Lineal {
  @Override
  default boolean addIsSimpleErrors(final List<GeometryValidationError> errors,
    final boolean shortCircuit) {
    return Lineal.addIsSimpleErrors(this, errors, shortCircuit);
  }

  @Override
  default Lineal applyLineal(final Function<LineString, LineString> function) {
    if (!isEmpty()) {
      boolean changed = false;
      final List<LineString> lines = new ArrayList<>();
      for (final LineString line : lineStrings()) {
        final LineString newLine = function.apply(line);
        changed |= line != newLine;
        lines.add(newLine);
      }
      if (changed) {
        final GeometryFactory geometryFactory = getGeometryFactory();
        return geometryFactory.lineal(lines);
      }
    }
    return this;
  }

  @Override
  Lineal clone();

  @Override
  default double distanceGeometry(Geometry geometry, final double terminateDistance) {
    if (isEmpty()) {
      return Double.POSITIVE_INFINITY;
    } else if (Property.isEmpty(geometry)) {
      return Double.POSITIVE_INFINITY;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      geometry = geometry.convertGeometry(geometryFactory, 2);
      double minDistance = Double.MAX_VALUE;
      for (final LineString line : lineStrings()) {
        final double distance = geometry.distanceLine(line);
        if (distance < minDistance) {
          minDistance = distance;
          if (distance <= terminateDistance) {
            return distance;
          }
        }
      }
      return minDistance;
    }
  }

  @Override
  default boolean equalsExact(final Geometry other, final double tolerance) {
    if (!isEquivalentClass(other)) {
      return false;
    } else {
      return GeometryCollection.super.equalsExact(other, tolerance);
    }
  }

  @Override
  default double getArea() {
    return 0;
  }

  @Override
  default double getArea(final Unit<Area> unit) {
    return 0;
  }

  /**
   * Gets the boundary of this geometry.
   * The boundary of a lineal geometry is always a zero-dimensional geometry (which may be empty).
   *
   * @return the boundary geometry
   * @see Geometry#getBoundary
   */
  @Override
  default Geometry getBoundary() {
    return new BoundaryOp(this).getBoundary();
  }

  @Override
  default Dimension getBoundaryDimension() {
    if (isClosed()) {
      return Dimension.FALSE;
    }
    return Dimension.L;
  }

  @Override
  default double getCoordinate(final int partIndex, final int vertexIndex, final int axisIndex) {
    final LineString line = getGeometry(partIndex);
    if (line == null) {
      return Double.NaN;
    } else {
      return line.getCoordinate(vertexIndex, axisIndex);
    }
  }

  @Override
  default GeometryDataType<MultiLineString, MultiLineStringEditor> getDataType() {
    return GeometryDataTypes.MULTI_LINE_STRING;
  }

  @Override
  default Dimension getDimension() {
    return Dimension.L;
  }

  @Override
  default LineString getLineString(final int partIndex) {
    return (LineString)getGeometry(partIndex);
  }

  int getLineStringCount();

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  default <V extends LineString> List<V> getLineStrings() {
    return (List)getGeometries();
  }

  @Override
  default Segment getSegment(final int... segmentId) {
    final int[] newId = GeometryCollection.cleanPartId(2, segmentId);
    final int partIndex = newId[0];
    if (partIndex >= 0 && partIndex < getGeometryCount()) {
      final LineString line = getLineString(partIndex);
      final int segmentIndex = newId[1];
      if (segmentIndex >= 0 && segmentIndex < line.getSegmentCount()) {
        return new MultiLineStringSegment(this, partIndex, segmentIndex);
      }
    }
    return null;
  }

  @Override
  default int getSegmentCount() {
    int segmentCount = 0;
    for (final LineString line : lineStrings()) {
      segmentCount += line.getSegmentCount();
    }
    return segmentCount;
  }

  @Override
  default Vertex getToVertex(final int... vertexId) {
    final int[] newId = GeometryCollection.cleanPartId(2, vertexId);
    final int partIndex = newId[0];
    final int geometryCount = getGeometryCount();
    if (partIndex >= 0 && partIndex < geometryCount) {
      final LineString line = getLineString(partIndex);
      int vertexIndex = newId[1];
      final int vertexCount = line.getVertexCount();
      vertexIndex = vertexCount - 1 - vertexIndex;
      if (vertexIndex >= 0 && vertexIndex < vertexCount) {
        newId[1] = vertexIndex;
        return new MultiLineStringVertex(this, newId);
      }
    }
    return null;
  }

  @Override
  default Vertex getVertex(final int... vertexId) {
    final int[] newId = GeometryCollection.cleanPartId(2, vertexId);
    final int partIndex = newId[0];
    final int geometryCount = getGeometryCount();
    if (partIndex >= 0 && partIndex < geometryCount) {
      if (partIndex >= 0 && partIndex < getGeometryCount()) {
        final int vertexIndex = newId[1];
        final LineString line = getLineString(partIndex);
        final int vertexCount = line.getVertexCount();
        if (vertexIndex >= 0 && vertexIndex < vertexCount) {
          return new MultiLineStringVertex(this, newId);
        }
      }
    }
    return null;
  }

  @Override
  default boolean hasInvalidXyCoordinates() {
    for (final LineString line : lineStrings()) {
      if (line.hasInvalidXyCoordinates()) {
        return true;
      }
    }
    return false;
  }

  @Override
  default Geometry intersectionBbox(final BoundingBox boundingBox) {
    notNullSameCs(boundingBox);
    if (bboxCoveredBy(boundingBox)) {
      return this;
    } else {
      boolean modified = false;
      final List<Geometry> parts = new ArrayList<>();
      for (final Geometry part : getLineStrings()) {
        final Geometry partIntersection = part.intersectionBbox(boundingBox);
        if (partIntersection != part) {
          modified = true;
        }
        if (!partIntersection.isEmpty()) {
          parts.add(partIntersection);
        }
      }
      if (modified) {
        final GeometryFactory geometryFactory = getGeometryFactory();
        return geometryFactory.geometry(parts);
      } else {
        return this;
      }
    }
  }

  @Override
  default boolean isClosed() {
    if (isEmpty()) {
      return false;
    } else {
      for (final LineString line : getLineStrings()) {
        if (line.isEmpty()) {
          return false;
        } else if (!line.isClosed()) {
          return false;
        }
      }
      return true;
    }
  }

  @Override
  default boolean isEquivalentClass(final Geometry other) {
    return other instanceof MultiLineString;
  }

  @Override
  default boolean isHomogeneousGeometryCollection() {
    return true;
  }

  @Override
  default Iterable<LineString> lineStrings() {
    return getGeometries();
  }

  @Override
  default Lineal newGeometry(final GeometryFactory geometryFactory) {
    final List<LineString> lines = new ArrayList<>();
    for (final LineString line : getLineStrings()) {
      final LineString newLine = line.newGeometry(geometryFactory);
      lines.add(newLine);
    }
    return geometryFactory.lineal(lines);
  }

  @SuppressWarnings("unchecked")
  @Override
  default <G> G newUsingGeometryFactory(final GeometryFactory factory) {
    if (factory == getGeometryFactory()) {
      return (G)this;
    } else if (isEmpty()) {
      return (G)factory.lineString();
    } else {
      final LineString[] lines = new LineString[getGeometryCount()];
      for (int i = 0; i < getGeometryCount(); i++) {
        LineString line = getLineString(i);
        line = line.newUsingGeometryFactory(factory);
        lines[i] = line;
      }
      return (G)factory.lineal(lines);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  default <G extends Geometry> G newValidGeometry() {
    if (isEmpty()) {
      return (G)this;
    } else if (isValid()) {
      return (G)normalize();
    } else {
      final LineMerger lines = new LineMerger(this);
      return (G)lines.getLineal();
    }
  }

  @Override
  default Lineal normalize() {
    if (isEmpty()) {
      return this;
    } else {
      final List<LineString> geometries = new ArrayList<>();
      for (final LineString part : lineStrings()) {
        final LineString normalizedPart = part.normalize();
        geometries.add(normalizedPart);
      }
      Collections.sort(geometries);
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Lineal normalizedGeometry = geometryFactory.lineal(geometries);
      return normalizedGeometry;
    }
  }

  @Override
  default Lineal prepare() {
    return new PreparedMultiLineString(this);
  }

  @Override
  default Lineal removeDuplicatePoints() {
    if (isEmpty()) {
      return this;
    } else {
      final List<LineString> lines = new ArrayList<>();
      for (final LineString line : getLineStrings()) {
        if (line != null && !line.isEmpty()) {
          lines.add(line.removeDuplicatePoints());
        }
      }
      final GeometryFactory geometryFactory = getGeometryFactory();
      return geometryFactory.lineal(lines);
    }
  }

  /**
   * Creates a {@link Lineal} in the reverse
   * order to this object.
   * Both the order of the component LineStrings
   * and the order of their coordinate sequences
   * are reversed.
   *
   * @return a {@link Lineal} in the reverse order
   */
  @Override
  default Lineal reverse() {
    final LinkedList<LineString> revLines = new LinkedList<>();
    for (final Geometry geometry : geometries()) {
      final LineString line = (LineString)geometry;
      final LineString reverse = line.reverse();
      revLines.addFirst(reverse);
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.lineal(revLines);
  }

  @Override
  default Iterable<Segment> segments() {
    return new MultiLineStringSegment(this, 0, -1);
  }

  @SuppressWarnings("unchecked")
  @Override
  default <G extends Geometry> G toClockwise() {
    return (G)applyLineal(LineString::toClockwise);
  }

  @SuppressWarnings("unchecked")
  @Override
  default <G extends Geometry> G toCounterClockwise() {
    return (G)applyLineal(LineString::toCounterClockwise);
  }

  @Override
  default Vertex vertices() {
    return new MultiLineStringVertex(this, 0, -1);
  }

}
