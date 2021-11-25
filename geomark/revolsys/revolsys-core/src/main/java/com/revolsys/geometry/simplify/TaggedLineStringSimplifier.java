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

package com.revolsys.geometry.simplify;

import java.util.List;

import com.revolsys.geometry.algorithm.LineIntersector;
import com.revolsys.geometry.algorithm.RobustLineIntersector;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDouble;

/**
 * Simplifies a TaggedLineString, preserving topology
 * (in the sense that no new intersections are introduced).
 * Uses the recursive Douglas-Peucker algorithm.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class TaggedLineStringSimplifier {
  /**
   * Tests whether a segment is in a section of a TaggedLineString
   * @param taggedLine
   * @param sectionIndex
   * @param seg
   * @return
   */
  private static boolean isInLineSection(final TaggedLineString line, final int[] sectionIndex,
    final TaggedLineSegment seg) {
    // not in this taggedLine
    if (seg.getParent() != line.getParent()) {
      return false;
    }
    final int segIndex = seg.getIndex();
    if (segIndex >= sectionIndex[0] && segIndex < sectionIndex[1]) {
      return true;
    }
    return false;
  }

  private double distanceTolerance = 0.0;

  private LineSegmentIndex inputIndex = new LineSegmentIndex();

  private final LineIntersector li = new RobustLineIntersector();

  private LineString line;

  private LineSegmentIndex outputIndex = new LineSegmentIndex();

  private TaggedLineString taggedLine;

  public TaggedLineStringSimplifier(final LineSegmentIndex inputIndex,
    final LineSegmentIndex outputIndex) {
    this.inputIndex = inputIndex;
    this.outputIndex = outputIndex;
  }

  private int findFurthestPoint(final int i, final int j, final double[] maxDistance) {
    final Point p0 = this.line.getVertex(i);
    final Point p1 = this.line.getVertex(j);
    double maxDist = -1.0;
    int maxIndex = i;
    for (int k = i + 1; k < j; k++) {
      final Point midPt = this.line.getVertex(k);
      final double distance = LineSegmentUtil.distanceLinePoint(p0, p1, midPt);
      if (distance > maxDist) {
        maxDist = distance;
        maxIndex = k;
      }
    }
    maxDistance[0] = maxDist;
    return maxIndex;
  }

  /**
   * Flattens a section of the taggedLine between
   * indexes <code>start</code> and <code>end</code>,
   * replacing them with a taggedLine between the endpoints.
   * The input and output indexes are updated
   * to reflect this.
   *
   * @param start the start index of the flattened section
   * @param end the end index of the flattened section
   * @return the new segment created
   */
  private LineSegment flatten(final int start, final int end) {
    // make a new segment for the simplified geometry
    final Point p0 = this.line.getVertex(start);
    final Point p1 = this.line.getVertex(end);
    final LineSegment newSeg = new LineSegmentDouble(p0, p1);
    // update the indexes
    remove(this.taggedLine, start, end);
    this.outputIndex.add(newSeg);
    return newSeg;
  }

  private boolean hasBadInputIntersection(final TaggedLineString parentLine,
    final int[] sectionIndex, final LineSegment candidateSeg) {
    final List<TaggedLineSegment> querySegs = this.inputIndex.query(candidateSeg);
    for (final TaggedLineSegment querySeg : querySegs) {
      if (hasInteriorIntersection(querySeg, candidateSeg)) {
        if (isInLineSection(parentLine, sectionIndex, querySeg)) {
          continue;
        }
        return true;
      }
    }
    return false;
  }

  private boolean hasBadIntersection(final TaggedLineString parentLine, final int[] sectionIndex,
    final LineSegment candidateSeg) {
    if (hasBadOutputIntersection(candidateSeg)) {
      return true;
    }
    if (hasBadInputIntersection(parentLine, sectionIndex, candidateSeg)) {
      return true;
    }
    return false;
  }

  private boolean hasBadOutputIntersection(final LineSegment candidateSeg) {
    final List<LineSegment> querySegs = this.outputIndex.query(candidateSeg);
    for (final LineSegment querySeg : querySegs) {
      if (hasInteriorIntersection(querySeg, candidateSeg)) {
        return true;
      }
    }
    return false;
  }

  private boolean hasInteriorIntersection(final LineSegment seg0, final LineSegment seg1) {
    this.li.computeIntersectionPoints(seg0.getP0(), seg0.getP1(), seg1.getP0(), seg1.getP1());
    return this.li.isInteriorIntersection();
  }

  /**
   * Remove the segs in the section of the taggedLine
   * @param taggedLine
   * @param pts
   * @param sectionStartIndex
   * @param sectionEndIndex
   */
  private void remove(final TaggedLineString line, final int start, final int end) {
    for (int i = start; i < end; i++) {
      final TaggedLineSegment seg = line.getSegment(i);
      this.inputIndex.remove(seg);
    }
  }

  /**
   * Sets the distance tolerance for the simplification.
   * All vertices in the simplified geometry will be within this
   * distance of the original geometry.
   *
   * @param distanceTolerance the approximation tolerance to use
   */
  public void setDistanceTolerance(final double distanceTolerance) {
    this.distanceTolerance = distanceTolerance;
  }

  /**
   * Simplifies the given {@link TaggedLineString}
   * using the distance tolerance specified.
   *
   * @param taggedLine the linestring to simplify
   */
  void simplify(final TaggedLineString taggedLine) {
    this.taggedLine = taggedLine;
    this.line = taggedLine.getParent();
    simplifySection(0, this.line.getVertexCount() - 1, 0);
  }

  private void simplifySection(final int i, final int j, int depth) {
    depth += 1;
    final int[] sectionIndex = new int[2];
    if (i + 1 == j) {
      final LineSegment newSeg = this.taggedLine.getSegment(i);
      this.taggedLine.addToResult(newSeg);
      // leave this segment in the input index, for efficiency
      return;
    }

    boolean isValidToSimplify = true;

    /**
     * Following logic ensures that there is enough points in the output taggedLine.
     * If there is already more points than the minimum, there's nothing to check.
     * Otherwise, if in the worst case there wouldn't be enough points,
     * don't flatten this segment (which avoids the worst case scenario)
     */
    if (this.taggedLine.getResultSize() < this.taggedLine.getMinimumSize()) {
      final int worstCaseSize = depth + 1;
      if (worstCaseSize < this.taggedLine.getMinimumSize()) {
        isValidToSimplify = false;
      }
    }

    final double[] distance = new double[1];
    final int furthestPtIndex = findFurthestPoint(i, j, distance);
    // flattening must be less than distanceTolerance
    if (distance[0] > this.distanceTolerance) {
      isValidToSimplify = false;
    }
    // test if flattened section would cause intersection
    // final LineSegment candidateSeg = new LineSegmentDouble();
    final Point p0 = this.line.getVertex(i);
    final Point p1 = this.line.getVertex(j);
    final LineSegment candidateSeg = new LineSegmentDouble(p0, p1);
    sectionIndex[0] = i;
    sectionIndex[1] = j;
    if (hasBadIntersection(this.taggedLine, sectionIndex, candidateSeg)) {
      isValidToSimplify = false;
    }

    if (isValidToSimplify) {
      final LineSegment newSeg = flatten(i, j);
      this.taggedLine.addToResult(newSeg);
      return;
    }
    simplifySection(i, furthestPtIndex, depth);
    simplifySection(furthestPtIndex, j, depth);
  }
}
