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

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.util.Points;

/**
 * Represents a sequence of facets (points or line segments)
 * of a {@link Geometry}
 * specified by a subsequence of a {@link LineString}.
 *
 * @author Martin Davis
 *
 */
public class LineFacetSequence implements FacetSequence {
  private final LineString line;

  private final int start;

  private final int vertexCount;

  /**
   * Creates a new sequence for a single point from a LineString.
   *
   * @param line the sequence holding the points in the facet sequence
   * @param start the index of the point
   */
  public LineFacetSequence(final LineString line, final int start) {
    this(line, start, start + 1);
  }

  /**
   * Creates a new section based on a LineString.
   *
   * @param line the sequence holding the points in the section
   * @param start the index of the start point
   * @param end the index of the end point + 1
   */
  public LineFacetSequence(final LineString line, final int start, final int end) {
    this.line = line;
    this.start = start;
    this.vertexCount = end - start;
  }

  private double computeLineLineDistance(final FacetSequence facetSeq) {
    // both linear - compute minimum segment-segment distance
    double minDistance = Double.MAX_VALUE;

    final int lastVertexIndex1 = getVertexCount() - 1;
    for (int vertexIndex1 = 0; vertexIndex1 < lastVertexIndex1; vertexIndex1++) {
      final double line1x1 = getX(vertexIndex1);
      final double line1y1 = getY(vertexIndex1);
      final double line1x2 = getX(vertexIndex1 + 1);
      final double line1y2 = getY(vertexIndex1 + 1);
      final int lastVertexIndex2 = facetSeq.getVertexCount() - 1;
      for (int vertexIndex2 = 0; vertexIndex2 < lastVertexIndex2; vertexIndex2++) {
        final double line2x1 = facetSeq.getX(vertexIndex1);
        final double line2y1 = facetSeq.getY(vertexIndex1);
        final double line2x2 = facetSeq.getX(vertexIndex1 + 1);
        final double line2y2 = facetSeq.getY(vertexIndex1 + 1);

        final double dist = LineSegmentUtil.distanceLineLine(line1x1, line1y1, line1x2, line1y2,
          line2x1, line2y1, line2x2, line2y2);
        if (dist == 0.0) {
          return 0.0;
        } else if (dist < minDistance) {
          minDistance = dist;
        }
      }
    }
    return minDistance;
  }

  @Override
  public double distance(final FacetSequence facetSeq) {
    final boolean isPoint = isPoint();
    final boolean isPointOther = facetSeq.isPoint();

    if (isPoint) {
      final double x = getX(0);
      final double y = getY(0);
      if (isPointOther) {
        final double x2 = facetSeq.getX(0);
        final double y2 = facetSeq.getY(0);

        return Points.distance(x, x, x2, y2);
      } else {
        return PointFacetSequence.computePointLineDistance(x, y, facetSeq);
      }
    } else if (isPointOther) {
      final double x = facetSeq.getX(0);
      final double y = facetSeq.getY(0);
      return PointFacetSequence.computePointLineDistance(x, y, this);
    } else {
      return computeLineLineDistance(facetSeq);
    }

  }

  @Override
  public double getCoordinate(final int vertexIndex, final int axisIndex) {
    return this.line.getCoordinateFast(this.start + vertexIndex, axisIndex);
  }

  @Override
  public Point getPoint(final int index) {
    return this.line.getPoint(this.start + index);
  }

  @Override
  public int getVertexCount() {
    return this.vertexCount;
  }

  @Override
  public double getX(final int vertexIndex) {
    return this.line.getX(this.start + vertexIndex);
  }

  @Override
  public double getY(final int vertexIndex) {
    return this.line.getY(this.start + vertexIndex);
  }

  @Override
  public boolean isPoint() {
    return this.vertexCount == 1;
  }

}
