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
package com.revolsys.geometry.noding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.revolsys.geometry.algorithm.LineIntersector;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.AbstractDelegatingLineString;

/**
 * Represents a list of contiguous line segments,
 * and supports noding the segments.
 * The line segments are represented by an array of {@link Coordinates}s.
 * Intended to optimize the noding of contiguous segments by
 * reducing the number of allocated objects.
 * SegmentStrings can carry a context object, which is useful
 * for preserving topological or parentage information.
 * All noded substrings are initialized with the same context object.
 *
 * @version 1.7
 */
public class NodedSegmentString extends AbstractDelegatingLineString
  implements NodableSegmentString {
  private static final long serialVersionUID = 1L;

  /**
   * Gets the {@link SegmentString}s which result from splitting this string at node points.
   *
   * @param segments a Collection of NodedSegmentStrings
   * @return a Collection of NodedSegmentStrings representing the substrings
   */
  public static List<NodedSegmentString> getNodedSubstrings(
    final Collection<NodedSegmentString> segments) {
    final List<NodedSegmentString> nodedSegments = new ArrayList<>();
    for (final NodedSegmentString segmentString : segments) {
      final SegmentNodeList nodeList = segmentString.getNodeList();
      nodeList.addSplitEdges(nodedSegments);
    }
    return nodedSegments;
  }

  private Object data;

  private final SegmentNodeList nodeList = new SegmentNodeList(this);

  /**
   * Creates a new segment string from a list of vertices.
   *
   * @param line the vertices of the segment string
   * @param data the user-defined data of this segment string (may be null)
   */
  public NodedSegmentString(final LineString line, final Object data) {
    super(line);
    this.data = data;
  }

  /**
   * Add an SegmentNode for intersection intIndex.
   * An intersection that falls exactly on a vertex
   * of the SegmentString is normalized
   * to use the higher of the two possible segmentIndexes
   */
  public void addIntersection(final LineIntersector li, final int segmentIndex, final int geomIndex,
    final int intIndex) {
    final Point point = li.getIntersection(intIndex);
    addIntersection(point, segmentIndex);
  }

  /**
   * Adds an intersection node for a given point and segment to this segment string.
   *
   * @param point the location of the intersection
   * @param segmentIndex the index of the segment containing the intersection
   */
  @Override
  public void addIntersection(final Point point, final int segmentIndex) {
    addIntersectionNode(point, segmentIndex);
  }

  /**
   * Adds an intersection node for a given point and segment to this segment string.
   * If an intersection already exists for this exact location, the existing
   * node will be returned.
   *
   * @param point the location of the intersection
   * @param segmentIndex the index of the segment containing the intersection
   * @return the intersection node for the point
   */
  public SegmentNode addIntersectionNode(final Point point, final int segmentIndex) {
    final double x = point.getX();
    final double y = point.getY();
    int normalizedSegmentIndex = segmentIndex;
    // normalize the intersection point location
    final int nextSegIndex = normalizedSegmentIndex + 1;
    if (nextSegIndex < size()) {
      // Normalize segment index if point falls on vertex
      // The check for point equality is 2D only - Z values are ignored
      if (equalsVertex(nextSegIndex, x, y)) {
        normalizedSegmentIndex = nextSegIndex;
      }
    }
    /**
     * Add the intersection point to edge intersection list.
     */
    final SegmentNode ei = this.nodeList.add(x, y, normalizedSegmentIndex);
    return ei;
  }

  /**
   * Adds EdgeIntersections for one or both
   * intersections found for a segment of an edge to the edge intersection list.
   */
  public void addIntersections(final LineIntersector li, final int segmentIndex,
    final int geomIndex) {
    for (int i = 0; i < li.getIntersectionCount(); i++) {
      addIntersection(li, segmentIndex, geomIndex, i);
    }
  }

  /**
   * Gets the user-defined data for this segment string.
   *
   * @return the user-defined data
   */
  @Override
  public Object getData() {
    return this.data;
  }

  public SegmentNodeList getNodeList() {
    return this.nodeList;
  }

  /**
   * Gets the octant of the segment starting at vertex <code>index</code>.
   *
   * @param index the index of the vertex starting the segment.  Must not be
   * the last index in the vertex list
   * @return the octant of the segment at the vertex
   */
  public int getSegmentOctant(final int index) {
    if (index == size() - 1) {
      return -1;
    } else {
      final double x1 = getX(index);
      final double y1 = getY(index);
      final double x2 = getX(index + 1);
      final double y2 = getY(index + 1);
      return safeOctant(x1, y1, x2, y2);
    }
  }

  private int safeOctant(final double x1, final double y1, final double x2, final double y2) {
    if (x1 == x2 && y1 == y2) {
      return 0;
    } else {
      return Octant.octant(x1, y1, x2, y2);
    }
  }

  /**
   * Sets the user-defined data for this segment string.
   *
   * @param data an Object containing user-defined data
   */
  @Override
  public void setData(final Object data) {
    this.data = data;
  }

  @Override
  public int size() {
    return getVertexCount();
  }

  @Override
  public String toString() {
    if (getVertexCount() == 0) {
      return "LINESTRING EMPTY\t" + this.data;
    } else if (getVertexCount() < 2) {
      return GeometryFactory.floating2d(0).point(this.line) + "\t" + this.data;
    } else {
      return GeometryFactory.floating2d(0).lineString(this.line) + "\t" + this.data;
    }
  }
}
