/*
 * The JCS Conflation Suite (JCS) is a library of Java classes that
 * can be used to build automated or semi-automated conflation solutions.
 *
 * Copyright (C) 2003 Vivid Solutions
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */

package com.revolsys.geometry.algorithm;

import com.revolsys.geometry.algorithm.distance.PointPairDistance;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.vertex.Vertex;

/**
 * Implements algorithm for computing a distance metric which can be thought of
 * as the "Vertex Hausdorff Distance". This is the Hausdorff distance restricted
 * to vertices for one of the geometries. Also computes two points of the
 * Geometries which are separated by the computed distance.
 * <p>
 * <b>NOTE: This algorithm does NOT compute the full Hausdorff distance
 * correctly, but an approximation that is correct for a large subset of useful
 * cases. One important part of this subset is Linestrings that are roughly
 * parallel to each other, and roughly equal in length - just what is needed for
 * line matching. </b>
 */
public class VertexHausdorffDistance {

  public static double distance(final Geometry geometry1, final Geometry geometry2) {
    final VertexHausdorffDistance vhd = new VertexHausdorffDistance(geometry1, geometry2);
    return vhd.distance();
  }

  private final PointPairDistance pointDistance = new PointPairDistance();

  public VertexHausdorffDistance(final Geometry geometry1, final Geometry geometry2) {
    computeMaxPointDistance(geometry1, geometry2, this.pointDistance);
    computeMaxPointDistance(geometry2, geometry1, this.pointDistance);
  }

  public VertexHausdorffDistance(final LineSegment line1, final LineSegment line2) {
    computeMaxPointDistance(line1, line2, this.pointDistance);
    computeMaxPointDistance(line2, line1, this.pointDistance);
  }

  private void computeMaxPointDistance(final Geometry pointGeometry, final Geometry geometry,
    final PointPairDistance pointDistance) {
    pointDistance.setMaximum(pointDistance);
    final PointPairDistance maxPointDist = new PointPairDistance();
    final PointPairDistance minPointDist = new PointPairDistance();
    for (final Vertex vertex : pointGeometry.vertices()) {
      minPointDist.initialize();
      final double x = vertex.getX();
      final double y = vertex.getY();
      minPointDist.setMinimum(geometry, x, y);
      maxPointDist.setMaximum(minPointDist);
    }
    pointDistance.setMaximum(maxPointDist);
  }

  /**
   * Computes the maximum oriented distance between two line segments, as well
   * as the point pair separated by that distance.
   *
   * @param line1 the line segment containing the furthest point
   * @param line2 the line segment containing the closest point
   * @param pointDistance the point pair and distance to be updated
   */
  private void computeMaxPointDistance(final LineSegment line1, final LineSegment line2,
    final PointPairDistance pointDistance) {
    for (int i = 0; i < 2; i++) {
      final double line2x = line2.getX(i);
      final double line2y = line2.getY(i);
      final Point closestPoint = line1.closestPoint(line2x, line2y);
      final double closestX = closestPoint.getX();
      final double closestY = closestPoint.getY();
      pointDistance.setMaximum(closestX, closestY, line2x, line2y);
    }
  }

  public double distance() {
    return this.pointDistance.getDistance();
  }

  public Point[] getCoordinates() {
    return this.pointDistance.getPoints();
  }

}
