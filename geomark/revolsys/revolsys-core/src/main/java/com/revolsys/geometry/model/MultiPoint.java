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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.measure.Unit;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;

import com.revolsys.geometry.model.editor.MultiPointEditor;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.MultiPointVertex;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.operation.simple.DuplicateVertexError;
import com.revolsys.geometry.operation.valid.GeometryValidationError;
import com.revolsys.util.Property;

/**
 * Models a collection of {@link Point}s.
 * <p>
 * Any collection of Point is a valid MultiPoint.
 *
 *@version 1.7
 */
public interface MultiPoint extends GeometryCollection, Punctual {
  @Override
  default boolean addIsSimpleErrors(final List<GeometryValidationError> errors,
    final boolean shortCircuit) {
    final Set<Point> points = new TreeSet<>();
    for (final Vertex vertex : vertices()) {
      if (points.contains(vertex)) {
        final DuplicateVertexError error = new DuplicateVertexError(vertex.clone());
        if (shortCircuit) {
          return false;
        } else {
          errors.add(error);
        }
      } else {
        points.add(vertex.newPoint2D());
      }
    }
    return errors.isEmpty();
  }

  @Override
  Punctual clone();

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
      for (final Point point : getPoints()) {
        final double distance = geometry.distancePoint(point, terminateDistance);
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
  default double distancePoint(final double x, final double y, final double terminateDistance) {
    if (isEmpty()) {
      return Double.POSITIVE_INFINITY;
    } else {
      double minDistance = Double.MAX_VALUE;
      for (final Point point : getPoints()) {
        final double distance = point.distancePoint(x, y);
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
    }
    return GeometryCollection.super.equalsExact(other, tolerance);
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
   * Zero-dimensional geometries have no boundary by definition,
   * so an empty GeometryCollection is returned.
   *
   * @return an empty GeometryCollection
   * @see Geometry#getBoundary
   */

  @Override
  default Geometry getBoundary() {
    return getGeometryFactory().geometryCollection();
  }

  @Override
  default Dimension getBoundaryDimension() {
    return Dimension.FALSE;
  }

  @Override
  default Point getCentroid() {
    int pointCount = 0;
    double sumX = 0;
    double sumY = 0;
    for (final Point point : points()) {
      if (!point.isEmpty()) {
        pointCount += 1;
        final double x = point.getX();
        final double y = point.getY();
        sumX += x;
        sumY += y;
      }
    }
    final double centroidX = sumX / pointCount;
    final double centroidY = sumY / pointCount;
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.point(centroidX, centroidY);
  }

  @Override
  default double getCoordinate(final int partIndex, final int axisIndex) {
    final Point point = getPoint(partIndex);
    return point.getCoordinate(axisIndex);
  }

  @Override
  default GeometryDataType<MultiPoint, MultiPointEditor> getDataType() {
    return GeometryDataTypes.MULTI_POINT;
  }

  @Override
  default Dimension getDimension() {
    return Dimension.P;
  }

  @Override
  default double getLength() {
    return 0;
  }

  @Override
  default double getLength(final Unit<Length> unit) {
    return 0;
  }

  @Override
  default Point getPoint(final int partIndex) {
    return (Point)getGeometry(partIndex);
  }

  @Override
  default Segment getSegment(final int... segmentId) {
    return null;
  }

  @Override
  default Vertex getToVertex(final int... vertexId) {
    final int[] newVertexId = GeometryCollection.cleanPartId(1, vertexId);
    final int vertexIndex = newVertexId[0];
    final int geometryCount = getGeometryCount();
    if (vertexIndex >= 0 || vertexIndex < geometryCount) {
      return new MultiPointVertex(this, geometryCount - vertexIndex - 1);
    }
    return null;
  }

  @Override
  default Vertex getVertex(final int... vertexId) {
    final int[] newVertexId = GeometryCollection.cleanPartId(1, vertexId);
    final int vertexIndex = newVertexId[0];
    if (vertexIndex >= 0 || vertexIndex < getGeometryCount()) {
      return new MultiPointVertex(this, newVertexId);
    }
    return null;
  }

  @Override
  default boolean hasInvalidXyCoordinates() {
    for (final Point point : points()) {
      if (point.hasInvalidXyCoordinates()) {
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
      final List<Point> parts = new ArrayList<>();
      for (final Point point : getPoints()) {
        if (boundingBox.bboxCovers(point)) {
          parts.add(point);
        } else {
          modified = true;
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
  default boolean intersects(final Geometry geometry) {
    for (final Point point : points()) {
      if (point.intersects(geometry)) {
        return true;
      }
    }
    return false;
  }

  @Override
  default boolean isEquivalentClass(final Geometry other) {
    return other instanceof MultiPoint;
  }

  @Override
  default boolean isHomogeneousGeometryCollection() {
    return true;
  }

  @Override
  default boolean isValid() {
    return true;
  }

  @Override
  default Punctual newGeometry(final GeometryFactory geometryFactory) {
    final List<Point> newPoints = new ArrayList<>();
    final List<Point> points = getPoints();
    for (final Point point : points) {
      final Point newPoint = point.newGeometry(geometryFactory);
      newPoints.add(newPoint);
    }
    return geometryFactory.punctual(newPoints);
  }

  @SuppressWarnings("unchecked")
  @Override
  default <G> G newUsingGeometryFactory(final GeometryFactory factory) {
    if (factory == getGeometryFactory()) {
      return (G)this;
    } else if (isEmpty()) {
      return (G)factory.point();
    } else {
      final Point[] points = new Point[getGeometryCount()];
      for (int i = 0; i < getGeometryCount(); i++) {
        Point point = getPoint(i);
        point = point.newUsingGeometryFactory(factory);
        points[i] = point;
      }
      return (G)factory.punctual(points);
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
      return (G)union();
    }
  }

  @Override
  default Punctual normalize() {
    if (isEmpty()) {
      return this;
    } else {
      final List<Point> geometries = new ArrayList<>();
      for (final Geometry part : geometries()) {
        final Point normalizedPart = (Point)part.normalize();
        geometries.add(normalizedPart);
      }
      Collections.sort(geometries);
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Punctual normalizedGeometry = geometryFactory.punctual(geometries);
      return normalizedGeometry;
    }
  }

  @Override
  default List<Vertex> pointVertices() {
    if (isEmpty()) {
      return Collections.emptyList();
    } else {
      final int vertexCount = getVertexCount();
      final List<Vertex> vertices = new ArrayList<>(vertexCount);
      for (int i = 0; i < vertexCount; i++) {
        final MultiPointVertex vertex = new MultiPointVertex(this, i);
        vertices.add(vertex);
      }
      return vertices;
    }
  }

  @Override
  default Punctual prepare() {
    return this;
  }

  @Override
  default Punctual removeDuplicatePoints() {
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  default <G extends Geometry> G toClockwise() {
    return (G)this;
  }

  @Override
  @SuppressWarnings("unchecked")
  default <G extends Geometry> G toCounterClockwise() {
    return (G)this;
  }

  @Override
  default MultiPointVertex vertices() {
    return new MultiPointVertex(this, -1);
  }
}
