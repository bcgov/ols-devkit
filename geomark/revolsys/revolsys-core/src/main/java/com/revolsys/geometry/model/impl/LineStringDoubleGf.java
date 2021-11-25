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

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;

/**
 *  Models an OGC-style <code>LineString</code>.
 *  A LineString consists of a sequence of two or more vertices,
 *  along with all points along the linearly-interpolated curves
 *  (line segments) between each
 *  pair of consecutive vertices.
 *  Consecutive vertices may be equal.
 *  The line segments in the line may intersect each other (in other words,
 *  the linestring may "curl back" in itself and self-intersect.
 *  Linestrings with exactly two identical points are invalid.
 *  <p>
 * A linestring must have either 0 or 2 or more points.
 * If these conditions are not met, the constructors throw
 * an {@link IllegalArgumentException}
 *
 *@version 1.7
 */
public class LineStringDoubleGf extends LineStringDouble {

  private static final long serialVersionUID = 3110669828065365560L;

  public static double[] getNewCoordinates(final GeometryFactory geometryFactory,
    final int axisCount, final int vertexCount, final double... coordinates) {
    final int axisCountThis = geometryFactory.getAxisCount();
    double[] newCoordinates;
    if (axisCount < 0 || axisCount == 1) {
      throw new IllegalArgumentException("axisCount must be 0 or > 1 not " + axisCount);
    } else {
      final int oldCoordinateCount = coordinates.length;
      if (coordinates == null || axisCount == 0 || vertexCount == 0 || oldCoordinateCount == 0) {
        newCoordinates = null;
      } else {
        final int newCoordinateCount = vertexCount * axisCount;
        if (newCoordinateCount > oldCoordinateCount) {
          throw new IllegalArgumentException("axisCount=" + axisCount + " * vertexCount="
            + vertexCount + " > coordinates.length=" + oldCoordinateCount);
        } else {
          newCoordinates = new double[axisCountThis * vertexCount];
          for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
            for (int axisIndex = 0; axisIndex < axisCountThis; axisIndex++) {
              double value;
              if (axisIndex < axisCount) {
                value = coordinates[vertexIndex * axisCount + axisIndex];
                value = geometryFactory.makePrecise(axisIndex, value);
              } else {
                value = Double.NaN;
              }
              newCoordinates[vertexIndex * axisCountThis + axisIndex] = value;
            }
          }
        }
      }
    }
    return newCoordinates;
  }

  public static double[] getNewCoordinates(final GeometryFactory geometryFactory,
    final int axisCount, final int vertexCount, final Number... coordinates) {
    final int axisCountThis = geometryFactory.getAxisCount();
    double[] newCoordinates;
    if (axisCount < 0 || axisCount == 1) {
      throw new IllegalArgumentException("axisCount must 0 or > 1 not " + axisCount);
    } else if (coordinates == null || axisCount == 0 || vertexCount == 0
      || coordinates.length == 0) {
      newCoordinates = null;
    } else {
      final int coordinateCount = vertexCount * axisCount;
      if (coordinates.length % axisCount != 0) {
        throw new IllegalArgumentException("coordinates.length=" + coordinates.length
          + " must be a multiple of axisCount=" + axisCount);
      } else if (coordinateCount > coordinates.length) {
        throw new IllegalArgumentException("axisCount=" + axisCount + " * vertexCount="
          + vertexCount + " > coordinates.length=" + coordinates.length);
      } else {
        newCoordinates = new double[axisCountThis * vertexCount];
        for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
          for (int axisIndex = 0; axisIndex < axisCountThis; axisIndex++) {
            double value;
            if (axisIndex < axisCount) {
              value = coordinates[vertexIndex * axisCount + axisIndex].doubleValue();
              value = geometryFactory.makePrecise(axisIndex, value);
            } else {
              value = Double.NaN;
            }
            newCoordinates[vertexIndex * axisCountThis + axisIndex] = value;
          }
        }
      }
    }
    return newCoordinates;
  }

  public static double[] getNewCoordinates(final GeometryFactory geometryFactory,
    final LineString line) {
    final int axisCount = geometryFactory.getAxisCount();
    final int vertexCount = line.getVertexCount();
    final int coordinateCount = vertexCount * axisCount;

    final double[] newCoordinates = new double[coordinateCount];
    final int copyAxisCount = Math.min(axisCount, line.getAxisCount());
    int coordinateIndex = 0;
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      for (int axisIndex = 0; axisIndex < copyAxisCount; axisIndex++) {
        double value = line.getCoordinate(vertexIndex, axisIndex);
        value = geometryFactory.makePrecise(axisIndex, value);
        newCoordinates[coordinateIndex + axisIndex] = value;
      }
      coordinateIndex += axisCount;
    }
    return newCoordinates;
  }

  public static LineString newLineStringDoubleGf(final GeometryFactory geometryFactory,
    final int axisCount, final double... coordinates) {
    return new LineStringDoubleGf(geometryFactory, axisCount, coordinates.length / axisCount,
      coordinates);
  }

  /**
   *  The bounding box of this <code>Geometry</code>.
   */
  private BoundingBox boundingBox;

  /**
   * The {@link GeometryFactory} used to create this Geometry
   */
  private final GeometryFactory geometryFactory;

  public LineStringDoubleGf(final GeometryFactory geometryFactory) {
    super(geometryFactory.getAxisCount());
    this.geometryFactory = geometryFactory;
  }

  public LineStringDoubleGf(final GeometryFactory geometryFactory, final int axisCount,
    final int vertexCount, final double... coordinates) {
    super(axisCount, vertexCount, coordinates);
    this.geometryFactory = geometryFactory;
  }

  /**
   * Creates and returns a full copy of this {@link LineString} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public LineStringDoubleGf clone() {
    return (LineStringDoubleGf)super.clone();
  }

  @Override
  public BoundingBox getBoundingBox() {
    if (this.boundingBox == null) {
      this.boundingBox = newBoundingBox();
    }
    return this.boundingBox;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

}
