/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * multiPolygon it under the terms of the GNU Lesser General Public
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
import java.util.function.Function;

import com.revolsys.geometry.model.editor.MultiPolygonEditor;
import com.revolsys.geometry.model.prep.PreparedMultiPolygon;
import com.revolsys.geometry.model.segment.MultiPolygonSegment;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.MultiPolygonVertex;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.operation.polygonize.Polygonizer;

/**
 * Models a collection of {@link Polygon}s.
 * <p>
 * As per the OGC SFS specification,
 * the Polygons in a MultiPolygon may not overlap,
 * and may only touch at single points.
 * This allows the topological point-set semantics
 * to be well-defined.
 *
 *
 *@version 1.7
 */
public interface MultiPolygon extends GeometryCollection, Polygonal {
  @Override
  default Polygonal applyPolygonal(final Function<Polygon, Polygon> function) {
    if (!isEmpty()) {
      boolean changed = false;
      final Polygon[] polygons = new Polygon[getGeometryCount()];
      int i = 0;
      for (final Polygon polygon : polygons()) {
        final Polygon newPolygon = function.apply(polygon);
        changed |= polygon != newPolygon;
        polygons[i++] = newPolygon;
      }
      if (changed) {
        final GeometryFactory geometryFactory = getGeometryFactory();
        return geometryFactory.polygonal(polygons);
      }
    }
    return this;
  }

  @Override
  Polygonal clone();

  @Override
  default boolean equalsExact(final Geometry other, final double tolerance) {
    if (!isEquivalentClass(other)) {
      return false;
    }
    return GeometryCollection.super.equalsExact(other, tolerance);
  }

  /**
   * Computes the boundary of this geometry
   *
   * @return a lineal geometry (which may be empty)
   * @see Geometry#getBoundary
   */

  @Override
  default Geometry getBoundary() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (isEmpty()) {
      return geometryFactory.lineString();
    } else {
      final List<LineString> allRings = new ArrayList<>();
      for (final Polygon polygon : polygons()) {
        final Geometry rings = polygon.getBoundary();
        for (int j = 0; j < rings.getGeometryCount(); j++) {
          final LineString ring = (LineString)rings.getGeometry(j);
          allRings.add(ring);
        }
      }
      return geometryFactory.lineal(allRings);
    }
  }

  @Override
  default Dimension getBoundaryDimension() {
    return Dimension.L;
  }

  @Override
  default GeometryDataType<MultiPolygon, MultiPolygonEditor> getDataType() {
    return GeometryDataTypes.MULTI_POLYGON;
  }

  @Override
  default Dimension getDimension() {
    return Dimension.A;
  }

  @Override
  default Polygon getPolygon(final int partIndex) {
    return (Polygon)getGeometry(partIndex);
  }

  @Override
  default Segment getSegment(final int... segmentId) {
    final int[] newSegmentId = GeometryCollection.cleanPartId(3, segmentId);

    final int partIndex = newSegmentId[0];
    final int geometryCount = getGeometryCount();
    if (partIndex >= 0 && partIndex < geometryCount) {
      final Polygon polygon = getPolygon(partIndex);
      final int ringIndex = newSegmentId[1];
      final int ringCount = polygon.getRingCount();
      if (ringIndex >= 0 && ringIndex < ringCount) {
        final LinearRing ring = polygon.getRing(ringIndex);
        final int segmentIndex = newSegmentId[2];
        final int segmentCount = ring.getSegmentCount();
        if (segmentIndex >= 0 && segmentIndex < segmentCount) {
          return new MultiPolygonSegment(this, partIndex, ringIndex, segmentIndex);
        }
      }
    }
    return null;
  }

  @Override
  default int getSegmentCount() {
    int segmentCount = 0;
    for (final Polygon polygon : polygons()) {
      segmentCount += polygon.getSegmentCount();
    }
    return segmentCount;
  }

  @Override
  default Vertex getToVertex(final int... vertexId) {
    final int[] newVertexId = GeometryCollection.cleanPartId(3, vertexId);
    final int partIndex = newVertexId[0];
    final int geometryCount = getGeometryCount();
    if (partIndex >= 0 && partIndex < geometryCount) {
      final Polygon polygon = getPolygon(partIndex);
      final int ringIndex = newVertexId[1];
      final int ringCount = polygon.getRingCount();
      if (ringIndex >= 0 && ringIndex < ringCount) {
        final LinearRing ring = polygon.getRing(ringIndex);
        int vertexIndex = newVertexId[2];
        final int vertexCount = ring.getVertexCount();
        vertexIndex = vertexCount - 2 - vertexIndex;
        if (vertexIndex >= 0 && vertexIndex <= vertexCount) {
          newVertexId[2] = vertexIndex;
          return new MultiPolygonVertex(this, newVertexId);
        }
      }
    }
    return null;
  }

  @Override
  default Vertex getVertex(final int... vertexId) {
    final int[] newVertexId = GeometryCollection.cleanPartId(3, vertexId);
    final int partIndex = newVertexId[0];
    final int geometryCount = getGeometryCount();
    if (partIndex >= 0 && partIndex < geometryCount) {
      final Polygon polygon = getPolygon(partIndex);
      final int ringIndex = newVertexId[1];
      final int ringCount = polygon.getRingCount();
      if (ringIndex >= 0 && ringIndex < ringCount) {
        final LinearRing ring = polygon.getRing(ringIndex);
        final int vertexIndex = newVertexId[2];
        final int vertexCount = ring.getVertexCount();
        if (vertexIndex >= 0 && vertexIndex < vertexCount) {
          return new MultiPolygonVertex(this, newVertexId);
        }
      }
    }
    return null;
  }

  @Override
  default boolean hasInvalidXyCoordinates() {
    for (final Polygon polygon : polygons()) {
      if (polygon.hasInvalidXyCoordinates()) {
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
      for (final Geometry part : getPolygons()) {
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
  default boolean isContainedInBoundary(final BoundingBox boundingBox) {
    return false;
  }

  @Override
  default boolean isEmpty() {
    return getPolygonCount() == 0;
  }

  @Override
  default boolean isEquivalentClass(final Geometry other) {
    return other instanceof MultiPolygon;
  }

  @Override
  default boolean isHomogeneousGeometryCollection() {
    return true;
  }

  @Override
  default Polygonal newGeometry(final GeometryFactory geometryFactory) {
    final List<Polygon> polygons = new ArrayList<>();
    forEachPolygon(polygon -> {
      final Polygon newPolygon = polygon.newGeometry(geometryFactory);
      polygons.add(newPolygon);
    });
    return geometryFactory.polygonal(polygons);
  }

  @SuppressWarnings("unchecked")
  @Override
  default <G> G newUsingGeometryFactory(final GeometryFactory factory) {
    if (factory == getGeometryFactory()) {
      return (G)this;
    } else if (isEmpty()) {
      return (G)factory.polygonal();
    } else {
      final Polygon[] polygons = new Polygon[getGeometryCount()];
      for (int i = 0; i < getGeometryCount(); i++) {
        Polygon polygon = getPolygon(i);
        polygon = polygon.newUsingGeometryFactory(factory);
        polygons[i] = polygon;
      }
      return (G)factory.polygonal(polygons);
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
      final Polygonizer polygonizer = new Polygonizer();
      for (final Polygon polygon : polygons()) {
        polygonizer.addPolygon(polygon);
      }
      final Polygonal polygonal = polygonizer.getPolygonal();
      if (polygonal.isEmpty()) {
        return (G)this;
      } else {
        return (G)polygonal;
      }
    }
  }

  @Override
  default Polygonal normalize() {
    if (isEmpty()) {
      return this;
    } else {
      final List<Polygon> geometries = new ArrayList<>();
      for (final Geometry part : geometries()) {
        final Polygon normalizedPart = (Polygon)part.normalize();
        geometries.add(normalizedPart);
      }
      Collections.sort(geometries);
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Polygonal normalizedGeometry = geometryFactory.polygonal(geometries);
      return normalizedGeometry;
    }
  }

  @Override
  default PreparedMultiPolygon prepare() {
    return new PreparedMultiPolygon(this);
  }

  @Override
  default Polygonal removeDuplicatePoints() {
    return applyPolygonal(Polygon::removeDuplicatePoints);
  }

  /**
   * Creates a {@link Polygonal} with
   * every component reversed.
   * The order of the components in the collection are not reversed.
   *
   * @return a MultiPolygon in the reverse order
   */

  @Override
  default Polygonal reverse() {
    final List<Polygon> polygons = new ArrayList<>();
    for (final Polygon polygon : polygons()) {
      final Polygon reverse = polygon.reverse();
      polygons.add(reverse);
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.polygonal(polygons);
  }

  @Override
  default Iterable<Segment> segments() {
    return new MultiPolygonSegment(this, 0, 0, -1);
  }

  @SuppressWarnings("unchecked")
  @Override
  default <G extends Geometry> G toClockwise() {
    return (G)applyPolygonal(Polygon::toClockwise);
  }

  @SuppressWarnings("unchecked")
  @Override
  default <G extends Geometry> G toCounterClockwise() {
    return (G)applyPolygonal(Polygon::toCounterClockwise);
  }

  @Override
  default MultiPolygonVertex vertices() {
    return new MultiPolygonVertex(this, 0, 0, -1);
  }
}
