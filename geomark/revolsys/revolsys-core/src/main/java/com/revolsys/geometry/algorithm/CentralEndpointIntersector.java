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

import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.util.Points;

/**
 * Computes an approximate intersection of two line segments
 * by taking the most central of the endpoints of the segments.
 * This is effective in cases where the segments are nearly parallel
 * and should intersect at an endpoint.
 * It is also a reasonable strategy for cases where the
 * endpoint of one segment lies on or almost on the interior of another one.
 * Taking the most central endpoint ensures that the computed intersection
 * point lies in the envelope of the segments.
 * Also, by always returning one of the input points, this should result
 * in reducing segment fragmentation.
 * Intended to be used as a last resort for
 * computing ill-conditioned intersection situations which
 * cause other methods to fail.
 *
 * @author Martin Davis
 * @version 1.8
 */
public class CentralEndpointIntersector {
  public static double average(final int axisIndex, final double... coordinates) {
    double sum = 0;
    final int vertexCount = coordinates.length / 2;
    for (int coordinateIndex = axisIndex; coordinateIndex < coordinates.length; coordinateIndex = 2) {
      final double value = coordinates[coordinateIndex];
      sum += value;
    }
    return sum / vertexCount;
  }

  public static Point getIntersection(final double... coordinates) {
    final double averageX = average(0, coordinates);
    final double averageY = average(1, coordinates);
    double intersectionX = Double.NaN;
    double intersectionY = Double.NaN;
    double minDistance = Double.POSITIVE_INFINITY;

    final int vertexCount = coordinates.length / 2;
    int coordinateIndex = 0;
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      final double x = coordinates[coordinateIndex++];
      final double y = coordinates[coordinateIndex++];
      final double distance = Points.distance(averageX, averageY, x, y);
      if (distance < minDistance) {
        minDistance = distance;
        intersectionX = x;
        intersectionY = y;
      }
    }
    return new PointDoubleXY(intersectionX, intersectionY);
  }
}
