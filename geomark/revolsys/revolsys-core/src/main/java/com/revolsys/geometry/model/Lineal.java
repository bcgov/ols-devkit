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

import java.util.List;
import java.util.function.Function;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.geometry.graph.linemerge.LineMerger;
import com.revolsys.geometry.index.LineSegmentIndex;
import com.revolsys.geometry.model.editor.AbstractGeometryEditor;
import com.revolsys.geometry.model.editor.GeometryCollectionImplEditor;
import com.revolsys.geometry.model.editor.LinealEditor;
import com.revolsys.geometry.model.editor.MultiLineStringEditor;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.operation.simple.DuplicateVertexError;
import com.revolsys.geometry.operation.simple.SelfIntersectionPointError;
import com.revolsys.geometry.operation.simple.SelfIntersectionVertexError;
import com.revolsys.geometry.operation.simple.SelfOverlapLineSegmentError;
import com.revolsys.geometry.operation.simple.SelfOverlapSegmentError;
import com.revolsys.geometry.operation.valid.GeometryValidationError;

/**
 * Identifies {@link Geometry} subclasses which
 * are 1-dimensional and have components which are {@link LineString}s.
 *
 */
public interface Lineal extends Geometry {

  static boolean addIsSimpleErrors(final Lineal lineal, final List<GeometryValidationError> errors,
    final boolean shortCircuit) {
    final LineSegmentIndex index = new LineSegmentIndex(lineal);
    for (final Segment segment : lineal.segments()) {
      final int segmentIndex = segment.getSegmentIndex();
      final int partIndex = segment.getPartIndex();
      if (segment.getLength() == 0) {
        errors.add(new DuplicateVertexError(segment.getGeometryVertex(0)));
      } else {
        final List<LineSegment> segments = index.getItems(segment);
        for (final LineSegment lineSegment : segments) {
          final Segment segment2 = (Segment)lineSegment;
          final int partIndex2 = segment2.getPartIndex();
          final int segmentIndex2 = segment2.getSegmentIndex();
          if (partIndex2 > partIndex || partIndex == partIndex2 && segmentIndex2 > segmentIndex) {
            if (segment.equals(lineSegment)) {
              final SelfOverlapSegmentError error = new SelfOverlapSegmentError(segment);
              errors.add(error);
              if (shortCircuit) {
                return false;
              }
            } else {
              final Geometry intersection = segment.getIntersection(lineSegment);
              if (intersection instanceof Point) {
                final Point pointIntersection = (Point)intersection;
                boolean isIntersection = true;

                // Process segments on the same linestring part
                if (partIndex == partIndex2) {
                  // The end of the current segment can touch the start of the
                  // next
                  // segment
                  if (segmentIndex + 1 == segmentIndex2) {
                    if (lineSegment.equalsVertex(2, 0, pointIntersection)) {
                      isIntersection = false;
                    }
                    // A loop can touch itself at the start/end
                  } else if (segment.isLineClosed()) {
                    if (segment.isLineStart() && segment2.isLineEnd()) {
                      if (segment.equalsVertex(2, 0, pointIntersection)) {
                        isIntersection = false;
                      }
                    }
                  }
                } else {
                  if (!segment.isLineClosed() && !segment2.isLineClosed()) {
                    final boolean segment1EndIntersection = segment
                      .isEndIntersection(pointIntersection);
                    final boolean segment2EndIntersection = segment2
                      .isEndIntersection(pointIntersection);

                    if (segment1EndIntersection && segment2EndIntersection) {
                      isIntersection = false;
                    }
                  }
                }
                if (isIntersection) {
                  GeometryValidationError error;
                  if (segment.equalsVertex(2, 0, pointIntersection)) {
                    final Vertex vertex = segment.getGeometryVertex(0);
                    error = new SelfIntersectionVertexError(vertex);
                  } else if (segment.equalsVertex(2, 1, pointIntersection)) {
                    final Vertex vertex = segment.getGeometryVertex(1);
                    error = new SelfIntersectionVertexError(vertex);
                  } else {
                    error = new SelfIntersectionPointError(lineal, pointIntersection);
                  }
                  errors.add(error);
                  if (shortCircuit) {
                    return false;
                  }
                }
              } else if (intersection instanceof LineSegment) {
                final LineSegment lineIntersection = (LineSegment)intersection;
                GeometryValidationError error;
                if (segment.equals(lineIntersection)) {
                  error = new SelfOverlapSegmentError(segment);
                } else if (lineSegment.equals(lineIntersection)) {
                  error = new SelfOverlapSegmentError(segment2);
                } else {
                  error = new SelfOverlapLineSegmentError(lineal, lineIntersection);
                }
                errors.add(error);
                if (shortCircuit) {
                  return false;
                }
              }
            }
          }
        }
      }
    }
    return errors.isEmpty();
  }

  @SuppressWarnings("unchecked")
  static <G extends Geometry> G newLineal(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Lineal) {
      final Lineal lineal = (Lineal)value;
      if (lineal.getGeometryCount() == 1) {
        return lineal.getGeometry(0);
      } else {
        return (G)value;
      }
    } else if (value instanceof Geometry) {
      final Geometry geometry = (Geometry)value;
      if (geometry.isGeometryCollection()) {
        if (geometry.isEmpty()) {
          final GeometryFactory geometryFactory = geometry.getGeometryFactory();
          return (G)geometryFactory.polygon();
        } else if (geometry.getGeometryCount() == 1) {
          final Geometry part = geometry.getGeometry(0);
          if (part instanceof Lineal) {
            final Lineal lineal = (Lineal)part;
            return (G)lineal;
          }
        }
      }
      throw new IllegalArgumentException(
        "Expecting a Lineal geometry not " + geometry.getGeometryType() + "\n" + geometry);
    } else {
      final String string = DataTypes.toString(value);
      final Geometry geometry = GeometryFactory.DEFAULT_3D.geometry(string, false);
      return (G)newLineal(geometry);
    }
  }

  @Override
  default boolean addIsSimpleErrors(final List<GeometryValidationError> errors,
    final boolean shortCircuit) {
    return addIsSimpleErrors(this, errors, shortCircuit);
  }

  Lineal applyLineal(final Function<LineString, LineString> function);

  double getCoordinate(int partIndex, int vertexIndex, int axisIndex);

  LineString getLineString(int partIndex);

  default double getM(final int partIndex, final int vertexIndex) {
    return getCoordinate(partIndex, vertexIndex, M);
  }

  default double getX(final int partIndex, final int vertexIndex) {
    return getCoordinate(partIndex, vertexIndex, X);
  }

  default double getY(final int partIndex, final int vertexIndex) {
    return getCoordinate(partIndex, vertexIndex, Y);
  }

  default double getZ(final int partIndex, final int vertexIndex) {
    return getCoordinate(partIndex, vertexIndex, Z);
  }

  @Override
  default boolean hasGeometryType(final GeometryDataType<?, ?> dataType) {
    return GeometryDataTypes.LINE_STRING == dataType;
  }

  boolean isClosed();

  Iterable<LineString> lineStrings();

  default Lineal mergeLines() {
    if (isEmpty()) {
      return this;
    } else {
      final LineMerger merger = new LineMerger(this);
      return merger.getLineal();
    }
  }

  @Override
  Lineal newGeometry(final GeometryFactory geometryFactory);

  @Override
  default LinealEditor newGeometryEditor() {
    return new MultiLineStringEditor(this);
  }

  @Override
  default LinealEditor newGeometryEditor(final AbstractGeometryEditor<?> parentEditor) {
    return new MultiLineStringEditor((GeometryCollectionImplEditor)parentEditor, this);
  }

  @Override
  default LinealEditor newGeometryEditor(final int axisCount) {
    final LinealEditor geometryEditor = newGeometryEditor();
    geometryEditor.setAxisCount(axisCount);
    return geometryEditor;
  }

  default Lineal newLineal(final GeometryFactory geometryFactory, final LineString... lines) {
    return geometryFactory.lineal(lines);
  }

  @Override
  Lineal normalize();

  default double setCoordinate(final int partIndex, final int vertexIndex, final int axisIndex,
    final double coordinate) {
    throw new UnsupportedOperationException();
  }

  default double setM(final int partIndex, final int vertexIndex, final double m) {
    return setCoordinate(partIndex, vertexIndex, M, m);
  }

  default double setX(final int partIndex, final int vertexIndex, final double x) {
    return setCoordinate(partIndex, vertexIndex, X, x);
  }

  default double setY(final int partIndex, final int vertexIndex, final double y) {
    return setCoordinate(partIndex, vertexIndex, Y, y);
  }

  default double setZ(final int partIndex, final int vertexIndex, final double z) {
    return setCoordinate(partIndex, vertexIndex, Z, z);
  }

}
