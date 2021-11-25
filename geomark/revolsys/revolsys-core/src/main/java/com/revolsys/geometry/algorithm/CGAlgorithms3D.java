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

package com.revolsys.geometry.algorithm;

import com.revolsys.geometry.math.Vector3D;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXYZ;

/**
 * Basic computational geometry algorithms
 * for geometry and coordinates defined in 3-dimensional Cartesian space.
 *
 * @author mdavis
 *
 */
public class CGAlgorithms3D {
  public static double distance(final Point p0, final Point p1) {
    // default to 2D distance if either Z is not set
    if (Double.isNaN(p0.getZ()) || Double.isNaN(p1.getZ())) {
      return p0.distancePoint(p1);
    }

    final double dx = p0.getX() - p1.getX();
    final double dy = p0.getY() - p1.getY();
    final double dz = p0.getZ() - p1.getZ();
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  public static double distancePointSegment(final Point p, final Point A, final Point B) {
    // if start = end, then just compute distance to one of the endpoints
    if (A.equals(3, B)) {
      return distance(p, A);
    }

    // otherwise use comp.graphics.algorithms Frequently Asked Questions method
    /*
     * (1) r = AC dot AB --------- ||AB||^2 r has the following meaning: r=0 P =
     * A r=1 P = B r<0 P is on the backward extension of AB r>1 P is on the
     * forward extension of AB 0<r<1 P is interior to AB
     */

    final double len2 = (B.getX() - A.getX()) * (B.getX() - A.getX())
      + (B.getY() - A.getY()) * (B.getY() - A.getY())
      + (B.getZ() - A.getZ()) * (B.getZ() - A.getZ());
    if (Double.isNaN(len2)) {
      throw new IllegalArgumentException("Ordinates must not be NaN");
    }
    final double r = ((p.getX() - A.getX()) * (B.getX() - A.getX())
      + (p.getY() - A.getY()) * (B.getY() - A.getY())
      + (p.getZ() - A.getZ()) * (B.getZ() - A.getZ())) / len2;

    if (r <= 0.0) {
      return distance(p, A);
    }
    if (r >= 1.0) {
      return distance(p, B);
    }

    // compute closest point q on line segment
    final double qx = A.getX() + r * (B.getX() - A.getX());
    final double qy = A.getY() + r * (B.getY() - A.getY());
    final double qz = A.getZ() + r * (B.getZ() - A.getZ());
    // result is distance from p to q
    final double dx = p.getX() - qx;
    final double dy = p.getY() - qy;
    final double dz = p.getZ() - qz;
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  /**
   * Computes the distance between two 3D segments.
   *
   * @param A the start point of the first segment
   * @param B the end point of the first segment
   * @param C the start point of the second segment
   * @param D the end point of the second segment
   * @return the distance between the segments
   */
  public static double distanceSegmentSegment(final Point A, final Point B, final Point C,
    final Point D) {
    /**
     * This calculation is susceptible to roundoff errors when
     * passed large ordinate values.
     * It may be possible to improve this by using {@link DD} arithmetic.
     */
    if (A.equals(3, B)) {
      return distancePointSegment(A, C, D);
    }
    if (C.equals(3, B)) {
      return distancePointSegment(C, A, B);
    }

    /**
     * Algorithm derived from http://softsurfer.com/Archive/algorithm_0106/algorithm_0106.htm
     */
    final double a = Vector3D.dot(A, B, A, B);
    final double b = Vector3D.dot(A, B, C, D);
    final double c = Vector3D.dot(C, D, C, D);
    final double d = Vector3D.dot(A, B, C, A);
    final double e = Vector3D.dot(C, D, C, A);

    final double denom = a * c - b * b;
    if (Double.isNaN(denom)) {
      throw new IllegalArgumentException("Ordinates must not be NaN");
    }

    double s;
    double t;
    if (denom <= 0.0) {
      /**
       * The lines are parallel.
       * In this case solve for the parameters s and t by assuming s is 0.
       */
      s = 0;
      // choose largest denominator for optimal numeric conditioning
      if (b > c) {
        t = d / b;
      } else {
        t = e / c;
      }
    } else {
      s = (b * e - c * d) / denom;
      t = (a * e - b * d) / denom;
    }
    if (s < 0) {
      return distancePointSegment(A, C, D);
    } else if (s > 1) {
      return distancePointSegment(B, C, D);
    } else if (t < 0) {
      return distancePointSegment(C, A, B);
    } else if (t > 1) {
      return distancePointSegment(D, A, B);
    }
    /**
     * The closest points are in interiors of segments,
     * so compute them directly
     */
    final double x1 = A.getX() + s * (B.getX() - A.getX());
    final double y1 = A.getY() + s * (B.getY() - A.getY());
    final double z1 = A.getZ() + s * (B.getZ() - A.getZ());

    final double x2 = C.getX() + t * (D.getX() - C.getX());
    final double y2 = C.getY() + t * (D.getY() - C.getY());
    final double z2 = C.getZ() + t * (D.getZ() - C.getZ());

    // length (p1-p2)
    return distance(new PointDoubleXYZ(x1, y1, z1), new PointDoubleXYZ(x2, y2, z2));
  }

}
