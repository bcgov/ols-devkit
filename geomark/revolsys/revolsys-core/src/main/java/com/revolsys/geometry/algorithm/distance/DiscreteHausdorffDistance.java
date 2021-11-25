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

package com.revolsys.geometry.algorithm.distance;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.Vertex;

/**
 * An algorithm for computing a distance metric
 * which is an approximation to the Hausdorff Distance
 * based on a discretization of the input {@link Geometry}.
 * The algorithm computes the Hausdorff distance restricted to discrete points
 * for one of the geometries.
 * The points can be either the vertices of the geometries (the default),
 * or the geometries with line segments densified by a given fraction.
 * Also determines two points of the Geometries which are separated by the computed distance.
 * <p>
 * This algorithm is an approximation to the standard Hausdorff distance.
 * Specifically,
 * <pre>
 *    for all geometries a, b:    DHD(a, b) <= HD(a, b)
 * </pre>
 * The approximation can be made as close as needed by densifying the input geometries.
 * In the limit, this value will approach the true Hausdorff distance:
 * <pre>
 *    DHD(A, B, densifyFactor) -> HD(A, B) as densifyFactor -> 0.0
 * </pre>
 * The default approximation is exact or close enough for a large subset of useful cases.
 * Examples of these are:
 * <ul>
 * <li>computing distance between Linestrings that are roughly parallel to each other,
 * and roughly equal in length.  This occurs in matching linear networks.
 * <li>Testing similarity of geometries.
 * </ul>
 * An example where the default approximation is not close is:
 * <pre>
 *   A = LINESTRING (0 0, 100 0, 10 100, 10 100)
 *   B = LINESTRING (0 100, 0 10, 80 10)
 *
 *   DHD(A, B) = 22.360679774997898
 *   HD(A, B) ~= 47.8
 * </pre>
 */
public class DiscreteHausdorffDistance {
  public static double distance(final Geometry g0, final Geometry g1) {
    final DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(g0, g1);
    return dist.distance();
  }

  public static double distance(final Geometry g0, final Geometry g1, final double densifyFrac) {
    final DiscreteHausdorffDistance dist = new DiscreteHausdorffDistance(g0, g1);
    dist.setDensifyFraction(densifyFrac);
    return dist.distance();
  }

  /**
   * Value of 0.0 indicates that no densification should take place
   */
  private double densifyFrac = 0.0;

  private final Geometry g0;

  private final Geometry g1;

  private final PointPairDistance ptDist = new PointPairDistance();

  public DiscreteHausdorffDistance(final Geometry g0, final Geometry g1) {
    this.g0 = g0;
    this.g1 = g1;
  }

  private void compute(final Geometry g0, final Geometry g1) {
    computeOrientedDistance(g0, g1, this.ptDist);
    computeOrientedDistance(g1, g0, this.ptDist);
  }

  private void computeOrientedDistance(final Geometry discreteGeom, final Geometry geom,
    final PointPairDistance ptDist) {
    final PointPairDistance maxPtDist = new PointPairDistance();
    final PointPairDistance minPtDist = new PointPairDistance();
    for (final Vertex vertex : discreteGeom.vertices()) {

      minPtDist.initialize();
      DistanceToPoint.computeDistance(geom, vertex, minPtDist);
      maxPtDist.setMaximum(minPtDist);
    }
    ptDist.setMaximum(maxPtDist);

    if (this.densifyFrac > 0) {
      maxPtDist.initialize();
      final int numSubSegs = 0;

      for (final Segment segment : discreteGeom.segments()) {
        final double x1 = segment.getX(0);
        final double y1 = segment.getY(0);
        final double x2 = segment.getX(1);
        final double y2 = segment.getY(1);
        final double delx = (x2 - x1) / numSubSegs;
        final double dely = (y2 - y1) / numSubSegs;

        for (int i = 0; i < numSubSegs; i++) {
          final double x = x1 + i * delx;
          final double y = y1 + i * dely;
          final Point pt = new PointDoubleXY(x, y);
          minPtDist.initialize();
          DistanceToPoint.computeDistance(geom, pt, minPtDist);
          maxPtDist.setMaximum(minPtDist);
        }
      }

      ptDist.setMaximum(maxPtDist);

    }
  }

  public double distance() {
    compute(this.g0, this.g1);
    return this.ptDist.getDistance();
  }

  public Point[] getCoordinates() {
    return this.ptDist.getPoints();
  }

  public double orientedDistance() {
    computeOrientedDistance(this.g0, this.g1, this.ptDist);
    return this.ptDist.getDistance();
  }

  /**
   * Sets the fraction by which to densify each segment.
   * Each segment will be (virtually) split into a number of equal-length
   * subsegments, whose fraction of the total length is closest
   * to the given fraction.
   *
   * @param densifyPercent
   */
  public void setDensifyFraction(final double densifyFrac) {
    if (densifyFrac > 1.0 || densifyFrac <= 0.0) {
      throw new IllegalArgumentException("Fraction is not in range (0.0 - 1.0]");
    }

    this.densifyFrac = densifyFrac;
  }

}
