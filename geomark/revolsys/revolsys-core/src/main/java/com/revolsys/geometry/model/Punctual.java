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
import java.util.Set;
import java.util.TreeSet;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.function.Consumer4Double;
import org.jeometry.common.function.Function4Double;

import com.revolsys.geometry.model.editor.AbstractGeometryEditor;
import com.revolsys.geometry.model.editor.MultiPointEditor;
import com.revolsys.geometry.model.editor.PunctualEditor;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.util.Pair;

/**
 * Identifies {@link Geometry} subclasses which
 * are 0-dimensional and with components which are {@link Point}s.
 *
 * @author Martin Davis
 *
 */
public interface Punctual extends Geometry {
  @SuppressWarnings("unchecked")
  static <G extends Geometry> G newPunctual(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Punctual) {
      return (G)value;
    } else if (value instanceof Geometry) {
      final Geometry geometry = (Geometry)value;
      throw new IllegalArgumentException(
        "Expecting a Punctual geometry not " + geometry.getGeometryType() + "\n" + geometry);
    } else {
      final String string = DataTypes.toString(value);
      final Geometry geometry = GeometryFactory.DEFAULT_3D.geometry(string, false);
      return (G)newPunctual(geometry);
    }
  }

  @Override
  default Pair<GeometryComponent, Double> findClosestGeometryComponent(final double x,
    final double y, final double maxDistance) {
    if (isEmpty()) {
      return new Pair<>();
    } else {
      Vertex closestComponent = null;
      double closestDistance = Double.POSITIVE_INFINITY;
      for (final Vertex vertex : vertices()) {
        if (vertex.equalsVertex(x, y)) {
          return new Pair<>(vertex, 0.0);
        } else {
          final double fromDistance = vertex.distancePoint(x, y);
          if (fromDistance <= maxDistance) {
            if (fromDistance < closestDistance) {
              closestDistance = fromDistance;
              closestComponent = vertex.clone();
            }
          }
        }
      }

      if (Double.isFinite(closestDistance)) {
        return new Pair<>(closestComponent, closestDistance);
      } else {
        return new Pair<>();
      }
    }
  }

  @Override
  default <R> R findSegment(final Function4Double<R> action) {
    return null;
  }

  @Override
  default void forEachSegment(final Consumer4Double action) {
  }

  double getCoordinate(int partIndex, int axisIndex);

  @Override
  default Point getInteriorPoint() {
    if (isEmpty()) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      return geometryFactory.point();
    } else {
      final Point centroid = getCentroid();
      final double centroidX = centroid.getX();
      final double centroidY = centroid.getY();
      double minDistance = Double.MAX_VALUE;
      Point interiorPoint = null;
      for (final Point point : points()) {
        final double distance = point.distancePoint(centroidX, centroidY);
        if (distance < minDistance) {
          interiorPoint = point;
          minDistance = distance;
        }
      }
      return interiorPoint;
    }
  }

  default double getM(final int partIndex) {
    return getCoordinate(partIndex, M);
  }

  Point getPoint(int i);

  default <V extends Point> List<V> getPoints() {
    return getGeometries();
  }

  default double getX(final int partIndex) {
    return getCoordinate(partIndex, X);
  }

  default double getY(final int partIndex) {
    return getCoordinate(partIndex, Y);
  }

  default double getZ(final int partIndex) {
    return getCoordinate(partIndex, Z);
  }

  @Override
  default boolean hasGeometryType(final GeometryDataType<?, ?> dataType) {
    return GeometryDataTypes.POINT == dataType;
  }

  default boolean hasPoint(final double x, final double y) {
    return false;
  }

  default boolean hasPoint(final Point point) {
    final Point convertedPoint = point.convertPoint2d(this);
    final double x = convertedPoint.getX();
    final double y = convertedPoint.getY();
    return hasPoint(x, y);
  }

  @Override
  Punctual newGeometry(final GeometryFactory geometryFactory);

  @Override
  default PunctualEditor newGeometryEditor() {
    return new MultiPointEditor(this);
  }

  @Override
  default PunctualEditor newGeometryEditor(final AbstractGeometryEditor<?> parentEditor) {
    return new MultiPointEditor(parentEditor, this);
  }

  @Override
  default PunctualEditor newGeometryEditor(final int axisCount) {
    final PunctualEditor geometryEditor = newGeometryEditor();
    geometryEditor.setAxisCount(axisCount);
    return geometryEditor;
  }

  default Punctual newPunctual(final GeometryFactory geometryFactory, final Point... points) {
    return geometryFactory.punctual(points);
  }

  default Iterable<Point> points() {
    return getGeometries();
  }

  default double setCoordinate(final int partIndex, final int axisIndex, final double coordinate) {
    throw new UnsupportedOperationException();
  }

  default double setM(final int partIndex, final double m) {
    return setCoordinate(partIndex, M, m);
  }

  default double setX(final int partIndex, final double x) {
    return setCoordinate(partIndex, X, x);
  }

  default double setY(final int partIndex, final double y) {
    return setCoordinate(partIndex, Y, y);
  }

  default double setZ(final int partIndex, final double z) {
    return setCoordinate(partIndex, Z, z);
  }

  @Override
  default Punctual union() {
    if (isEmpty()) {
      return this;
    } else {
      final Set<Point> newPoints = new TreeSet<>();
      for (final Point point : points()) {
        if (!point.isEmpty()) {
          newPoints.add(point);
        }
      }
      final GeometryFactory geometryFactory = getGeometryFactory();
      return geometryFactory.punctual(newPoints);
    }
  }
}
