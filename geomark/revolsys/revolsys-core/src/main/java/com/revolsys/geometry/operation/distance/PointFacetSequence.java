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

package com.revolsys.geometry.operation.distance;

import org.jeometry.common.function.BiConsumerDouble;
import org.jeometry.common.function.Consumer3Double;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.model.impl.AbstractPoint;

/**
 * Represents a sequence of facets (points or line segments)
 * of a {@link Geometry}
 * specified by a subsequence of a {@link LineString}.
 *
 * @author Martin Davis
 *
 */
public class PointFacetSequence extends AbstractPoint implements FacetSequence {

  private static final long serialVersionUID = 1L;

  public static double computePointLineDistance(final double x, final double y,
    final FacetSequence facetSeq) {
    double minDistance = java.lang.Double.MAX_VALUE;

    double x1 = facetSeq.getX(0);
    double y1 = facetSeq.getY(0);
    final int vertexCount = facetSeq.getVertexCount();
    for (int vertexIndex = 1; vertexIndex < vertexCount; vertexIndex++) {
      final double x2 = facetSeq.getX(vertexIndex);
      final double y2 = facetSeq.getY(vertexIndex);
      final double dist = LineSegmentUtil.distanceLinePoint(x1, y1, x2, y2, x, y);
      if (dist == 0.0) {
        return 0.0;
      } else if (dist < minDistance) {
        minDistance = dist;
      }
      x1 = x2;
      y1 = y2;
    }
    return minDistance;
  }

  private final Point point;

  public PointFacetSequence(final Point point) {
    this.point = point;
  }

  @Override
  public double distance(final FacetSequence other) {
    final boolean isPointOther = other.isPoint();
    final double x = getX(0);
    final double y = getY(0);
    if (isPointOther) {
      return this.point.distancePoint(x, y);
    } else {
      return computePointLineDistance(x, y, other);
    }
  }

  @Override
  public void forEachVertex(final BiConsumerDouble action) {
    this.point.forEachVertex(action);
  }

  @Override
  public void forEachVertex(final Consumer3Double action) {
    this.point.forEachVertex(action);
  }

  @Override
  public int getAxisCount() {
    return 2;
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    return this.point.getCoordinate(axisIndex);
  }

  @Override
  public double getCoordinate(final int vertexIndex, final int axisIndex) {
    return this.point.getCoordinate(axisIndex);
  }

  @Override
  public Point getPoint(final int vertexIndex) {
    return this.point;
  }

  @Override
  public int getVertexCount() {
    return 1;
  }

  @Override
  public double getX() {
    return this.point.getX();
  }

  @Override
  public double getX(final int vertexIndex) {
    return this.point.getX();
  }

  @Override
  public double getY() {
    return this.point.getY();
  }

  @Override
  public double getY(final int vertexIndex) {
    return this.point.getY();
  }

  @Override
  public boolean isEmpty() {
    return this.point.isEmpty();
  }

  @Override
  public boolean isPoint() {
    return true;
  }

  @Override
  public String toString() {
    return this.point.toString();
  }
}
