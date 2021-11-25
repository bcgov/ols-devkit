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
package com.revolsys.geometry.noding.snapround;

import java.util.Collection;
import java.util.List;

import com.revolsys.geometry.algorithm.LineIntersector;
import com.revolsys.geometry.algorithm.RobustLineIntersector;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.noding.InteriorIntersectionFinderAdder;
import com.revolsys.geometry.noding.MCIndexNoder;
import com.revolsys.geometry.noding.NodedSegmentString;
import com.revolsys.geometry.noding.Noder;
import com.revolsys.geometry.noding.SegmentString;
import com.revolsys.geometry.noding.SinglePassNoder;

/**
 * Uses Snap Rounding to compute a rounded,
 * fully noded arrangement from a set of {@link SegmentString}s.
 * Implements the Snap Rounding technique described in
 * the papers by Hobby, Guibas & Marimont, and Goodrich et al.
 * Snap Rounding assumes that all vertices lie on a uniform grid;
 * hence the precision model of the input must be fixed precision,
 * and all the input vertices must be rounded to that precision.
 * <p>
 * This implementation uses simple iteration over the line segments.
 * This is not the most efficient approach for large sets of segments.
 * <p>
 * This implementation appears to be fully robust using an integer precision model.
 * It will function with non-integer precision models, but the
 * results are not 100% guaranteed to be correctly noded.
 *
 * @version 1.7
 */
public class SimpleSnapRounder implements Noder {

  private final LineIntersector li;

  private Collection nodedSegStrings;

  private final double scaleFactor;

  public SimpleSnapRounder(final double scale) {
    this.scaleFactor = scale;
    this.li = new RobustLineIntersector(this.scaleFactor);
  }

  /**
   * @param inputSegmentStrings a Collection of NodedSegmentStrings
   */
  @Override
  public void computeNodes(final Collection<NodedSegmentString> inputSegmentStrings) {
    this.nodedSegStrings = inputSegmentStrings;
    snapRound(inputSegmentStrings, this.li);

    // testing purposes only - remove in final version
    // checkCorrectness(inputSegmentStrings);
  }

  /**
   * Computes nodes introduced as a result of snapping segments to snap points (hot pixels)
   * @param li
   */
  private void computeSnaps(final Collection segStrings, final Collection snapPts) {
    for (final Object segString : segStrings) {
      final NodedSegmentString ss = (NodedSegmentString)segString;
      computeSnaps(ss, snapPts);
    }
  }

  private void computeSnaps(final NodedSegmentString ss, final Collection<Point> snapPts) {
    for (final Point snapPt : snapPts) {
      final HotPixel hotPixel = new HotPixel(snapPt, this.scaleFactor, this.li);
      for (int i = 0; i < ss.size() - 1; i++) {
        hotPixel.addSnappedNode(ss, i);
      }
    }
  }

  /**
   * Computes nodes introduced as a result of
   * snapping segments to vertices of other segments
   *
   * @param edges the list of segment strings to snap together
   */
  public void computeVertexSnaps(final Collection edges) {
    for (final Object edge : edges) {
      final NodedSegmentString edge0 = (NodedSegmentString)edge;
      for (final Object edge2 : edges) {
        final NodedSegmentString edge1 = (NodedSegmentString)edge2;
        computeVertexSnaps(edge0, edge1);
      }
    }
  }

  /**
   * Performs a brute-force comparison of every segment in each {@link SegmentString}.
   * This has n^2 performance.
   */
  private void computeVertexSnaps(final NodedSegmentString segment1,
    final NodedSegmentString segment2) {
    for (int i0 = 0; i0 < segment1.size() - 1; i0++) {
      final Point point1 = segment1.getPoint(i0);
      final HotPixel hotPixel = new HotPixel(point1, this.scaleFactor, this.li);
      for (int i1 = 0; i1 < segment2.size() - 1; i1++) {
        // don't snap a vertex to itself
        if (segment1 == segment2) {
          if (i0 == i1) {
            continue;
          }
        }
        // System.out.println("trying " + pts0[i0] + " against " + pts1[i1] +
        // pts1[i1 + 1]);
        final boolean isNodeAdded = hotPixel.addSnappedNode(segment2, i1);
        // if a node is created for a vertex, that vertex must be noded too
        if (isNodeAdded) {
          segment1.addIntersection(point1, i0);
        }
      }
    }
  }

  /**
   * Computes all interior intersections in the collection of {@link SegmentString}s,
   * and returns their {@link Coordinates}s.
   *
   * Does NOT node the segStrings.
   *
   * @return a list of Point for the intersections
   */
  private List findInteriorIntersections(final Collection segStrings, final LineIntersector li) {
    final InteriorIntersectionFinderAdder intFinderAdder = new InteriorIntersectionFinderAdder(li);
    final SinglePassNoder noder = new MCIndexNoder();
    noder.setSegmentIntersector(intFinderAdder);
    noder.computeNodes(segStrings);
    return intFinderAdder.getInteriorIntersections();
  }

  /**
   * @return a Collection of NodedSegmentStrings representing the substrings
   *
   */
  @Override
  public Collection<NodedSegmentString> getNodedSubstrings() {
    return NodedSegmentString.getNodedSubstrings(this.nodedSegStrings);
  }

  private void snapRound(final Collection segStrings, final LineIntersector li) {
    final List intersections = findInteriorIntersections(segStrings, li);
    computeSnaps(segStrings, intersections);
    computeVertexSnaps(segStrings);
  }

}
