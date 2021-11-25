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

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.geomgraph.Edge;
import com.revolsys.geometry.geomgraph.Quadrant;

/**
 * MonotoneChains are a way of partitioning the segments of an edge to
 * allow for fast searching of intersections.
 * Specifically, a sequence of contiguous line segments
 * is a monotone chain iff all the vectors defined by the oriented segments
 * lies in the same quadrant.
 * <p>
 * Monotone Chains have the following useful properties:
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
 *
 * @version 1.7
 */
public class MonotoneChainIndexer {

  public static int[] toIntArray(final List<Integer> list) {
    final int[] array = new int[list.size()];
    for (int i = 0; i < array.length; i++) {
      array[i] = list.get(i);
    }
    return array;
  }

  public MonotoneChainIndexer() {
  }

  /**
   * @return the index of the last point in the monotone chain
   */
  private int findChainEnd(final Edge edge, final int start) {
    // determine quadrant for chain
    final int chainQuad = Quadrant.quadrant(edge.getX(start), edge.getY(start),
      edge.getX(start + 1), edge.getY(start + 1));
    int last = start + 1;
    final int vertexCount = edge.getVertexCount();
    while (last < vertexCount) {
      // compute quadrant for next possible segment in chain
      final int quad = Quadrant.quadrant(edge.getX(last - 1), edge.getY(last - 1), edge.getX(last),
        edge.getY(last));
      if (quad != chainQuad) {
        break;
      }
      last++;
    }
    return last - 1;
  }

  public int[] getChainStartIndices(final Edge edge) {
    // find the startpoint (and endpoints) of all monotone chains in this edge
    int start = 0;
    final List<Integer> startIndexList = new ArrayList<>();
    startIndexList.add(start);
    final int vertexCount = edge.getVertexCount();
    do {
      final int last = findChainEnd(edge, start);
      startIndexList.add(last);
      start = last;
    } while (start < vertexCount - 1);
    // copy list to an array of ints, for efficiency
    final int[] startIndex = toIntArray(startIndexList);
    return startIndex;
  }

}
