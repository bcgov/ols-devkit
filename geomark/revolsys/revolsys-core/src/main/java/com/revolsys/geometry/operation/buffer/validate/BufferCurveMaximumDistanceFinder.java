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
package com.revolsys.geometry.operation.buffer.validate;

import com.revolsys.geometry.algorithm.distance.PointPairDistance;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.Vertex;

/**
 * Finds the approximate maximum distance from a buffer curve to
 * the originating geometry.
 * This is similar to the Discrete Oriented Hausdorff distance
 * from the buffer curve to the input.
 * <p>
 * The approximate maximum distance is determined by testing
 * all vertices in the buffer curve, as well
 * as midpoints of the curve segments.
 * Due to the way buffer curves are constructed, this
 * should be a very close approximation.
 *
 * @author mbdavis
 *
 */
public class BufferCurveMaximumDistanceFinder {
  private final Geometry inputGeom;

  private final PointPairDistance maxPtDist = new PointPairDistance();

  public BufferCurveMaximumDistanceFinder(final Geometry inputGeom) {
    this.inputGeom = inputGeom;
  }

  private void computeMaxMidpointDistance(final Geometry curve) {
    final PointPairDistance maxPtDist = new PointPairDistance();

    final PointPairDistance minPtDist = new PointPairDistance();
    for (final Segment segment : curve.segments()) {
      final Point midPoint = segment.midPoint();
      minPtDist.initialize();
      DistanceToPointFinder.computeDistance(this.inputGeom, midPoint, minPtDist);
      maxPtDist.setMaximum(minPtDist);
    }
    this.maxPtDist.setMaximum(maxPtDist);
  }

  private void computeMaxVertexDistance(final Geometry curve) {
    final PointPairDistance maxPtDist = new PointPairDistance();
    final PointPairDistance minPtDist = new PointPairDistance();
    for (final Vertex vertex : curve.vertices()) {
      minPtDist.initialize();
      DistanceToPointFinder.computeDistance(this.inputGeom, vertex, minPtDist);
      maxPtDist.setMaximum(minPtDist);
    }
    this.maxPtDist.setMaximum(maxPtDist);
  }

  public double findDistance(final Geometry bufferCurve) {
    computeMaxVertexDistance(bufferCurve);
    computeMaxMidpointDistance(bufferCurve);
    return this.maxPtDist.getDistance();
  }

  public PointPairDistance getDistancePoints() {
    return this.maxPtDist;
  }

}
