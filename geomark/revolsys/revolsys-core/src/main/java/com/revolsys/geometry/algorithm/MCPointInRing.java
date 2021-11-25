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

import java.util.List;

import com.revolsys.geometry.index.bintree.Bintree;
import com.revolsys.geometry.index.bintree.Interval;
import com.revolsys.geometry.index.chain.MonotoneChain;
import com.revolsys.geometry.index.chain.MonotoneChainSelectAction;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;

/**
 * Implements {@link PointInRing}
 * using {@link MonotoneChain}s and a {@link Bintree} index to
 * increase performance.
 *
 * @version 1.7
 *
 * @see GeometryFactoryIndexedPointInAreaLocator for more general functionality
 */
public class MCPointInRing implements PointInRing {

  private int crossings = 0; // number of segment/ray crossings

  private final Interval interval = new Interval();

  private final LinearRing ring;

  private Bintree tree;

  public MCPointInRing(final LinearRing ring) {
    this.ring = ring;
    buildIndex();
  }

  private void buildIndex() {
    this.tree = new Bintree();

    final LineString points = this.ring.removeDuplicatePoints();
    final MonotoneChain[] mcList = MonotoneChain.getChainsArray(points, null);

    for (final MonotoneChain mc : mcList) {
      this.interval.min = mc.getMinY();
      this.interval.max = mc.getMaxY();
      this.tree.insert(this.interval, mc);
    }
  }

  @Override
  public boolean isInside(final Point pt) {
    this.crossings = 0;

    // test all segments intersected by ray from pt in positive x direction
    final double x = pt.getX();
    final double y = pt.getY();
    final BoundingBox rayEnv = new BoundingBoxDoubleXY(-Double.MAX_VALUE, y, Double.MAX_VALUE, y);

    this.interval.min = y;
    this.interval.max = y;
    final List<MonotoneChain> segs = this.tree.query(this.interval);
    if (!segs.isEmpty()) {
      /*
       * Test if segment crosses ray from test point in positive x direction.
       */
      final MonotoneChainSelectAction mcSelecter = (chain, startIndex) -> {
        final LineString line = chain.getLine();
        final double x1 = line.getX(startIndex) - x;
        final double y1 = line.getY(startIndex) - y;
        final double x2 = line.getX(startIndex + 1) - x;
        final double y2 = line.getY(startIndex + 1) - y;

        if (y1 > 0 && y2 <= 0 || y2 > 0 && y1 <= 0) {
          /*
           * segment straddles x axis, so compute intersection.
           */
          final double xInt = RobustDeterminant.signOfDet2x2(x1, y1, x2, y2) / (y2 - y1);
          /*
           * crosses ray if strictly positive intersection.
           */
          if (0.0 < xInt) {
            this.crossings++;
          }
        }
      };
      for (final MonotoneChain mc : segs) {
        mc.select(rayEnv, mcSelecter);
      }
    }
    /*
     * p is inside if number of crossings is odd.
     */
    if (this.crossings % 2 == 1) {
      return true;
    }
    return false;
  }

}
