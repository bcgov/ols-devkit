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
package com.revolsys.geometry.geomgraph.index;

import com.revolsys.geometry.geomgraph.Edge;
import com.revolsys.geometry.model.LineString;

/**
 * MonotoneChains are a way of partitioning the segments of an edge to
 * allow for fast searching of intersections.
 * They have the following properties:
 * <ol>
 * <li>the segments within a monotone chain will never intersect each other
 * <li>the envelope of any contiguous subset of the segments in a monotone chain
 * is simply the envelope of the endpoints of the subset.
 * </ol>
 * Property 1 means that there is no need to test pairs of segments from within
 * the same monotone chain for intersection.
 * Property 2 allows
 * binary search to be used to find the intersection points of two monotone chains.
 * For many types of real-world data, these properties eliminate a large number of
 * segment comparisons, producing substantial speed gains.
 * @version 1.7
 */
public class MonotoneChainEdge {

  private final Edge edge;

  // the lists of start/end indexes of the monotone chains.
  // Includes the end point of the edge as a sentinel
  final int[] startIndexes;

  public MonotoneChainEdge(final Edge edge) {
    this.edge = edge;
    final MonotoneChainIndexer mcb = new MonotoneChainIndexer();
    this.startIndexes = mcb.getChainStartIndices(edge);
  }

  private void computeIntersections(final LineString line1, final int fromIndex1,
    final double fromX1, final double fromY1, final int toIndex1, final double toX1,
    final double toY1, final Edge edge2, final LineString line2, final int fromIndex2,
    final double fromX2, final double fromY2, final int toIndex2, final double toX2,
    final double toY2, final SegmentIntersector ei) {
    // terminating condition for the recursion
    if (toIndex1 - fromIndex1 == 1 && toIndex2 - fromIndex2 == 1) {
      ei.addIntersections(this.edge, line1, fromIndex1, fromX1, fromY1, toX1, toY1, edge2,
        fromIndex2, fromX2, fromY2, toX2, toY2);
    } else {

      double minX2 = fromX2;
      double minY2 = fromY2;
      double maxX2 = toX2;
      double maxY2 = toY2;
      if (minX2 > maxX2) {
        final double t = minX2;
        minX2 = maxX2;
        maxX2 = t;
      }
      if (minY2 > maxY2) {
        final double t = minY2;
        minY2 = maxY2;
        maxY2 = t;
      }

      double minX1 = fromX1;
      double minY1 = fromY1;
      double maxX1 = toX1;
      double maxY1 = toY1;
      if (minX1 > maxX1) {
        final double t = minX1;
        minX1 = maxX1;
        maxX1 = t;
      }
      if (minY1 > maxY1) {
        final double t = minY1;
        minY1 = maxY1;
        maxY1 = t;
      }

      // Envelopes intersect
      if (!(minX2 > maxX1 || maxX2 < minX1 || minY2 > maxY1 || maxY2 < minY1)) {
        // the chains overlap, so split each in half and iterate (binary search)
        final int midIndex1 = (fromIndex1 + toIndex1) / 2;
        final double midX1 = line1.getX(midIndex1);
        final double midY1 = line1.getY(midIndex1);
        final int midIndex2 = (fromIndex2 + toIndex2) / 2;
        final double midX2 = line2.getX(midIndex2);
        final double midY2 = line2.getY(midIndex2);

        // Assert: mid != start or end (since we checked above for end - start
        // <= 1)
        // check terminating conditions before recursing
        // Reuse the sub-chain. Resetting after each operation
        if (fromIndex1 < midIndex1) {
          if (fromIndex2 < midIndex2) {
            computeIntersections(line1, fromIndex1, fromX1, fromY1, midIndex1, midX1, midY1, edge2,
              line2, fromIndex2, fromX2, fromY2, midIndex2, midX2, midY2, ei);
          }
          if (midIndex2 < toIndex2) {
            computeIntersections(line1, fromIndex1, fromX1, fromY1, midIndex1, midX1, midY1, edge2,
              line2, midIndex2, midX2, midY2, toIndex2, toX2, toY2, ei);
          }
        }
        if (midIndex1 < toIndex1) {
          if (fromIndex2 < midIndex2) {
            computeIntersections(line1, midIndex1, midX1, midY1, toIndex1, toX1, toY1, edge2, line2,
              fromIndex2, fromX2, fromY2, midIndex2, midX2, midY2, ei);
          }
          if (midIndex2 < toIndex2) {
            computeIntersections(line1, midIndex1, midX1, midY1, toIndex1, toX1, toY1, edge2, line2,
              midIndex2, midX2, midY2, toIndex2, toX2, toY2, ei);
          }
        }
      }
    }
  }

  public void computeIntersectsForChain(final int chainIndex1, final MonotoneChainEdge mce,
    final int chainIndex2, final SegmentIntersector si) {
    final Edge edge1 = this.edge;
    final LineString line1 = edge1.getLineString();
    final int fromIndex1 = this.startIndexes[chainIndex1];
    final double fromX1 = line1.getX(fromIndex1);
    final double fromY1 = line1.getY(fromIndex1);
    final int toIndex1 = this.startIndexes[chainIndex1 + 1];
    final double toX1 = line1.getX(toIndex1);
    final double toY1 = line1.getY(toIndex1);

    final Edge edge2 = mce.edge;
    final LineString line2 = edge2.getLineString();
    final int fromIndex2 = mce.startIndexes[chainIndex2];
    final double fromX2 = line2.getX(fromIndex2);
    final double fromY2 = line2.getY(fromIndex2);
    final int toIndex2 = mce.startIndexes[chainIndex2 + 1];
    final double toX2 = line2.getX(toIndex2);
    final double toY2 = line2.getY(toIndex2);

    computeIntersections(line1, fromIndex1, fromX1, fromY1, toIndex1, toX1, toY1, edge2, line2,
      fromIndex2, fromX2, fromY2, toIndex2, toX2, toY2, si);
  }

}
