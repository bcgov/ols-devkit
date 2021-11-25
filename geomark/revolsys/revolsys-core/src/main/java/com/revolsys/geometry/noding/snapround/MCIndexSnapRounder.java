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
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.noding.InteriorIntersectionFinderAdder;
import com.revolsys.geometry.noding.MCIndexNoder;
import com.revolsys.geometry.noding.NodedSegmentString;
import com.revolsys.geometry.noding.Noder;
import com.revolsys.geometry.noding.SegmentString;

/**
 * Uses Snap Rounding to compute a rounded,
 * fully noded arrangement from a set of {@link SegmentString}s.
 * Implements the Snap Rounding technique described in
 * papers by Hobby, Guibas & Marimont, and Goodrich et al.
 * Snap Rounding assumes that all vertices lie on a uniform grid;
 * hence the precision model of the input must be fixed precision,
 * and all the input vertices must be rounded to that precision.
 * <p>
 * This implementation uses a monotone chains and a spatial index to
 * speed up the intersection tests.
 * <p>
 * This implementation appears to be fully robust using an integer precision model.
 * It will function with non-integer precision models, but the
 * results are not 100% guaranteed to be correctly noded.
 *
 * @version 1.7
 */
public class MCIndexSnapRounder implements Noder {

  private final LineIntersector li;

  private Collection nodedSegStrings;

  private MCIndexNoder noder;

  private MCIndexPointSnapper pointSnapper;

  private final double scaleFactor;

  public MCIndexSnapRounder(final double scale) {
    this.li = new RobustLineIntersector(scale);
    this.scaleFactor = scale;
  }

  /**
   * Snaps segments to nodes created by segment intersections.
   */
  private void computeIntersectionSnaps(final Collection<Point> snapPts) {
    for (final Point snapPt : snapPts) {
      final HotPixel hotPixel = new HotPixel(snapPt, this.scaleFactor, this.li);
      this.pointSnapper.snap(hotPixel);
    }
  }

  @Override
  public void computeNodes(final Collection<NodedSegmentString> inputSegmentStrings) {
    this.nodedSegStrings = inputSegmentStrings;
    this.noder = new MCIndexNoder();
    this.pointSnapper = new MCIndexPointSnapper(this.noder.getIndex());
    snapRound(inputSegmentStrings, this.li);

    // testing purposes only - remove in final version
    // checkCorrectness(inputSegmentStrings);
  }

  /**
   * Snaps segments to all vertices.
   *
   * @param edges the list of segment strings to snap together
   */
  public void computeVertexSnaps(final Collection<NodedSegmentString> edges) {
    for (final NodedSegmentString edge0 : edges) {
      computeVertexSnaps(edge0);
    }
  }

  /**
   * Snaps segments to the vertices of a Segment String.
   */
  private void computeVertexSnaps(final NodedSegmentString segment) {
    final LineString points = segment.getLineString();
    for (int i = 0; i < points.getVertexCount(); i++) {
      final Point point = points.getPoint(i);
      final HotPixel hotPixel = new HotPixel(point, this.scaleFactor, this.li);
      final boolean isNodeAdded = this.pointSnapper.snap(hotPixel, segment, i);
      // if a node is created for a vertex, that vertex must be noded too
      if (isNodeAdded) {
        segment.addIntersection(point, i);
      }
    }
  }

  /**
   * Computes all interior intersections in the collection of {@link SegmentString}s,
   * and returns their @link Coordinate}s.
   *
   * Does NOT node the segStrings.
   *
   * @return a list of Point for the intersections
   */
  private List findInteriorIntersections(final Collection segStrings, final LineIntersector li) {
    final InteriorIntersectionFinderAdder intFinderAdder = new InteriorIntersectionFinderAdder(li);
    this.noder.setSegmentIntersector(intFinderAdder);
    this.noder.computeNodes(segStrings);
    return intFinderAdder.getInteriorIntersections();
  }

  @Override
  public Collection<NodedSegmentString> getNodedSubstrings() {
    return NodedSegmentString.getNodedSubstrings(this.nodedSegStrings);
  }

  private void snapRound(final Collection segStrings, final LineIntersector li) {
    final List intersections = findInteriorIntersections(segStrings, li);
    computeIntersectionSnaps(intersections);
    computeVertexSnaps(segStrings);
  }

}
