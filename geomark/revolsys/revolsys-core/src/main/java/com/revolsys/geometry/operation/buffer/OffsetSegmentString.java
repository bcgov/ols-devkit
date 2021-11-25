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
package com.revolsys.geometry.operation.buffer;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.editor.LineStringEditor;
import com.revolsys.geometry.util.Points;

/**
 * A dynamic list of the vertices in a constructed offset curve.
 * Automatically removes adjacent vertices
 * which are closer than a given tolerance.
 *
 * @author Martin Davis
 *
 */
class OffsetSegmentString extends LineStringEditor {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * The distance below which two adjacent points on the curve
   * are considered to be coincident.
   * This is chosen to be a small fraction of the offset distance.
   */
  private final double minimimVertexDistance;

  public OffsetSegmentString(final GeometryFactory geometryFactory,
    final double minimimVertexDistance) {
    super(geometryFactory);
    if (minimimVertexDistance < geometryFactory.getResolutionX()) {
      this.minimimVertexDistance = 0;
    } else {
      this.minimimVertexDistance = minimimVertexDistance;
    }
  }

  public void addPoint(double x, double y) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    x = geometryFactory.makeXyPrecise(x);
    y = geometryFactory.makeXyPrecise(y);
    if (!isRedundant(x, y)) {
      appendVertex(x, y);
    }
  }

  public void addPoint(final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    addPoint(x, y);
  }

  public void addPoints(final LineString line, final boolean isForward) {
    final int vertexCount = line.getVertexCount();
    if (isForward) {
      for (int i = 0; i < vertexCount; i++) {
        final double x = line.getX(i);
        final double y = line.getY(i);
        addPoint(x, y);
      }
    } else {
      for (int i = vertexCount - 1; i >= 0; i--) {
        final double x = line.getX(i);
        final double y = line.getY(i);
        addPoint(x, y);
      }
    }
  }

  @Override
  public void closeRing() {
    final int vertexCount = getVertexCount();
    if (vertexCount > 0) {
      final double x1 = getX(0);
      final double y1 = getY(0);
      final int indexN = vertexCount - 1;
      final double xn = getX(indexN);
      final double yn = getY(indexN);

      if (x1 != xn || y1 != yn) {
        addPoint(x1, y1);
      }
    }
  }

  public LineString getPoints() {
    return newLineString();
  }

  /**
   * Tests whether the given point is redundant
   * relative to the previous
   * point in the list (up to tolerance).
   *
   * @param pt
   * @return true if the point is redundant
   */
  private boolean isRedundant(final double x, final double y) {
    final int vertexCount = getVertexCount();
    if (vertexCount == 0) {
      return false;
    } else {
      final int indexN = vertexCount - 1;
      final double lastX = getX(indexN);
      final double lastY = getY(indexN);
      if (x == lastX && y == lastY) {
        return true;
      } else if (this.minimimVertexDistance == 0) {
        return false;
      } else {
        final double distance = Points.distance(x, y, lastX, lastY);
        if (distance < this.minimimVertexDistance) {
          return true;
        } else {
          return false;
        }
      }
    }
  }

  @Override
  public String toString() {
    return toString();
  }
}
