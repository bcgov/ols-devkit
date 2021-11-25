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
package com.revolsys.geometry.index.chain;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.geomgraph.Quadrant;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.geometry.noding.SegmentString;
import com.revolsys.geometry.util.RectangleUtil;

/**
 * Monotone Chains are a way of partitioning the segments of a linestring to
 * allow for fast searching of intersections.
 * They have the following properties:
 * <ol>
 * <li>the segments within a monotone chain never intersect each other
 * <li>the envelope of any contiguous subset of the segments in a monotone chain
 * is equal to the envelope of the endpoints of the subset.
 * </ol>
 * Property 1 means that there is no need to test pairs of segments from within
 * the same monotone chain for intersection.
 * <p>
 * Property 2 allows
 * an efficient binary search to be used to find the intersection line of two monotone chains.
 * For many types of real-world data, these properties eliminate a large number of
 * segment comparisons, producing substantial speed gains.
 * <p>
 * One of the goals of this implementation of MonotoneChains is to be
 * as space and time efficient as possible. One design choice that aids this
 * is that a MonotoneChain is based on a subarray of a list of line.
 * This means that new arrays of line (potentially very large) do not
 * have to be allocated.
 * <p>
 *
 * MonotoneChains support the following kinds of queries:
 * <ul>
 * <li>BoundingBox select: determine all the segments in the chain which
 * intersect a given envelope
 * <li>Overlap: determine all the pairs of segments in two chains whose
 * envelopes overlap
 * </ul>
 *
 * This implementation of MonotoneChains uses the concept of internal iterators
 * ({@link MonotoneChainSelectAction} and {@link MonotoneChainOverlapAction})
 * to return the results for queries.
 * This has time and space advantages, since it
 * is not necessary to build lists of instantiated objects to represent the segments
 * returned by the query.
 * Queries made in this manner are thread-safe.
 *
 * @version 1.7
 */
public class MonotoneChain extends BoundingBoxDoubleXY {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * Finds the index of the last point in a monotone chain
   * starting at a given point.
   * Any repeated line (0-length segments) will be included
   * in the monotone chain returned.
   *
   * @return the index of the last point in the monotone chain
   * starting at <code>start</code>.
   */
  private static int findChainEnd(final LineString points, final int start) {
    int safeStart = start;
    // skip any zero-length segments at the start of the sequence
    // (since they cannot be used to establish a quadrant)
    final int size = points.getVertexCount();
    double startX1 = points.getX(safeStart);
    double startY1 = points.getY(safeStart);
    double startX2 = points.getX(safeStart + 1);
    double startY2 = points.getY(safeStart + 1);
    while (safeStart < size - 2 && startX1 == startX2 && startY1 == startY2) {
      startX1 = startX2;
      startY1 = startY2;
      safeStart++;
      startX2 = points.getX(safeStart + 1);
      startY2 = points.getY(safeStart + 1);
    }
    // check if there are NO non-zero-length segments
    if (safeStart >= size - 2) {
      return size - 1;
    }
    // determine overall quadrant for chain (which is the starting quadrant)
    final int chainQuad = Quadrant.quadrant(startX1, startY1, startX2, startY2);
    int last = start + 1;
    if (last < size) {
      double lastX1 = points.getX(last - 1);
      double lastY1 = points.getY(last - 1);
      while (last < size) {
        final double lastX2 = points.getX(last);
        final double lastY2 = points.getY(last);
        // skip zero-length segments, but include them in the chain
        if (!(lastX1 == lastX2 && lastY1 == lastY2)) {
          // compute quadrant for next possible segment in chain
          final int quad = Quadrant.quadrant(lastX1, lastY1, lastX2, lastY2);
          if (quad != chainQuad) {
            break;
          }
        }
        last++;
        lastX1 = lastX2;
        lastY1 = lastY2;
      }
    }
    return last - 1;
  }

  public static MonotoneChain[] getChainsArray(final LineString points,
    final SegmentString context) {
    final List<Integer> indices = getChainStartIndices(points);
    final MonotoneChain[] mcList = new MonotoneChain[indices.size() - 1];
    int startIndex = indices.get(0);
    for (int i = 1; i < indices.size(); i++) {
      final int endIndex = indices.get(i);
      final MonotoneChain chain = new MonotoneChain(points, startIndex, endIndex, context);
      mcList[i - 1] = chain;
      startIndex = endIndex;
    }
    return mcList;
  }

  /**
   * Return an array containing lists of start/end indexes of the monotone chains
   * for the given list of coordinates.
   * The last entry in the array line to the end point of the point array,
   * for use as a sentinel.
   */
  private static List<Integer> getChainStartIndices(final LineString line) {
    // find the startpoint (and endpoints) of all monotone chains in this edge
    int start = 0;
    final List<Integer> startIndexList = new ArrayList<>();
    startIndexList.add(start);
    final int vertexCount = line.getVertexCount();
    do {
      final int last = findChainEnd(line, start);
      startIndexList.add(last);
      start = last;
    } while (start < vertexCount - 1);
    return startIndexList;
  }

  private final SegmentString context;// user-defined information

  private int id;// useful for optimizing chain comparisons

  private final LineString line;

  private final int start;

  private final int end;

  public MonotoneChain(final LineString line, final int start, final int end,
    final SegmentString context) {
    this.line = line;
    this.start = start;
    this.end = end;
    this.context = context;
    final double x1 = line.getX(start);
    final double y1 = line.getY(start);
    expandBbox(x1, y1);
    final double x2 = line.getX(end);
    final double y2 = line.getY(end);
    expandBbox(x2, y2);
  }

  private void computeOverlaps(final int start, final int end, final double startX,
    final double startY, final double endX, final double endY, final MonotoneChain chain,
    final int cStart, final int cEnd, final double cStartX, final double cStartY,
    final double cEndX, final double cEndY, final MonotoneChainOverlapAction action) {
    // terminating condition for the recursion
    if (end - start == 1 && cEnd - cStart == 1) {
      action.overlap(this, start, chain, cStart);
    } else if (RectangleUtil.intersectsMinMax(startX, startY, endX, endY, cStartX, cStartY, cEndX,
      cEndY)) {

      // the chains overlap, so split each in half and iterate (binary search)
      final int mid = (start + end) / 2;
      final LineString points = this.line;
      final double midX = points.getX(mid);
      final double midY = points.getY(mid);
      final int cMid = (cStart + cEnd) / 2;
      final LineString chainPoints = chain.line;
      final double cMidX = chainPoints.getX(cMid);
      final double cMidY = chainPoints.getY(cMid);

      // mid != start or end (since we checked above for end - start <= 1)
      // check terminating conditions before recursing
      if (start < mid) {
        if (cStart < cMid) {
          computeOverlaps(start, mid, startX, startY, midX, midY, //
            chain, cStart, cMid, cStartX, cStartY, cMidX, cMidY, action);
        }
        if (cMid < cEnd) {
          computeOverlaps(start, mid, startX, startY, midX, midY, //
            chain, cMid, cEnd, cMidX, cMidY, cEndX, cEndY, action);
        }
      }
      if (mid < end) {
        if (cStart < cMid) {
          computeOverlaps(mid, end, midX, midY, endX, endY, //
            chain, cStart, cMid, cStartX, cStartY, cMidX, cMidY, action);
        }
        if (cMid < cEnd) {
          computeOverlaps(mid, end, midX, midY, endX, endY, //
            chain, cMid, cEnd, cMidX, cMidY, cEndX, cEndY, action);
        }
      }
    }
  }

  /**
   * Determine all the line segments in two chains which may overlap, and process them.
   * <p>
   * The monotone chain search algorithm attempts to optimize
   * performance by not calling the overlap action on chain segments
   * which it can determine do not overlap.
   * However, it *may* call the overlap action on segments
   * which do not actually interact.
   * This saves on the overhead of checking intersection
   * each time, since clients may be able to do this more efficiently.
   *
   * @param searchEnv the search envelope
   * @param action the overlap action to execute on selected segments
   */
  public void computeOverlaps(final MonotoneChain chain, final MonotoneChainOverlapAction action) {
    final int start = this.start;
    final int end = this.end;
    final int cStart = chain.start;
    final int cEnd = chain.end;
    final LineString points = this.line;
    final double x1 = points.getX(start);
    final double y1 = points.getY(start);
    final double x2 = points.getX(end);
    final double y2 = points.getY(end);
    final LineString chainPoints = chain.line;
    final double cx1 = chainPoints.getX(cStart);
    final double cy1 = chainPoints.getY(cStart);
    final double cx2 = chainPoints.getX(cEnd);
    final double cy2 = chainPoints.getY(cEnd);

    computeOverlaps(start, end, x1, y1, x2, y2, chain, cStart, cEnd, cx1, cy1, cx2, cy2, action);
  }

  private void computeSelect(final BoundingBox searchEnv, final int start0, final int end0,
    final MonotoneChainSelectAction mcs) {
    final LineString line = this.line;
    final double x1 = line.getX(start0);
    final double y1 = line.getY(start0);
    final double x2 = line.getX(end0);
    final double y2 = line.getY(end0);

    // terminating condition for the recursion
    if (end0 - start0 == 1) {
      mcs.select(this, start0);
    } else if (searchEnv.bboxIntersects(x1, y1, x2, y2)) {
      // the chains overlap, so split each in half and iterate (binary search)
      final int mid = (start0 + end0) / 2;

      // Assert: mid != start or end (since we checked above for end - start <=
      // 1)
      // check terminating conditions before recursing
      if (start0 < mid) {
        computeSelect(searchEnv, start0, mid, mcs);
      }
      if (mid < end0) {
        computeSelect(searchEnv, mid, end0, mcs);
      }
    }
  }

  public SegmentString getContext() {
    return this.context;
  }

  public int getId() {
    return this.id;
  }

  public LineString getLine() {
    return this.line;
  }

  /**
   * Determine all the line segments in the chain whose envelopes overlap
   * the searchEnvelope, and process them.
   * <p>
   * The monotone chain search algorithm attempts to optimize
   * performance by not calling the select action on chain segments
   * which it can determine are not in the search envelope.
   * However, it *may* call the select action on segments
   * which do not intersect the search envelope.
   * This saves on the overhead of checking envelope intersection
   * each time, since clients may be able to do this more efficiently.
   *
   * @param boundingBox the search envelope
   * @param action the select action to execute on selected segments
   */
  public void select(final BoundingBox boundingBox, final MonotoneChainSelectAction action) {
    computeSelect(boundingBox, this.start, this.end, action);
  }

  public void setId(final int id) {
    this.id = id;
  }
}
