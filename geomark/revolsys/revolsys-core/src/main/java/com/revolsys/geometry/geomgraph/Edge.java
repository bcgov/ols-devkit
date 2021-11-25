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
package com.revolsys.geometry.geomgraph;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.geometry.algorithm.LineIntersector;
import com.revolsys.geometry.geomgraph.index.MonotoneChainEdge;
import com.revolsys.geometry.model.DelegatingLineString;
import com.revolsys.geometry.model.Dimension;
import com.revolsys.geometry.model.IntersectionMatrix;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.LineStringDouble;

/**
 * @version 1.7
 */
public class Edge extends GraphComponent implements DelegatingLineString {

  private static final long serialVersionUID = 1L;

  /**
   * Updates an IM from the label for an edge.
   * Handles edges from both L and A geometries.
   */
  public static void updateIM(final Label label, final IntersectionMatrix im) {
    im.setAtLeastIfValid(label.getLocation(0, Position.ON), label.getLocation(1, Position.ON),
      Dimension.L);
    if (label.isArea()) {
      im.setAtLeastIfValid(label.getLocation(0, Position.LEFT), label.getLocation(1, Position.LEFT),
        Dimension.A);
      im.setAtLeastIfValid(label.getLocation(0, Position.RIGHT),
        label.getLocation(1, Position.RIGHT), Dimension.A);
    }
  }

  private final Depth depth = new Depth();

  private int depthDelta = 0; // the change in area depth from the R to L side

  private final EdgeIntersectionList eiList = new EdgeIntersectionList(this);

  private boolean isIsolated = true;

  private MonotoneChainEdge mce;

  private String name;

  private final LineString line;

  public Edge(final LineString points) {
    this(points, null);
  }

  public Edge(final LineString line, final Label label) {
    this.line = line;
    this.label = label;
  }

  /**
   * Add an EdgeIntersection for intersection intIndex.
   * An intersection that falls exactly on a vertex of the edge is normalized
   * to use the higher of the two possible segmentIndexes
   */
  public void addIntersection(final LineIntersector li, final int segmentIndex, final int geomIndex,
    final int intIndex) {
    final Point intPt = li.getIntersection(intIndex);
    int normalizedSegmentIndex = segmentIndex;
    double dist = li.getEdgeDistance(geomIndex, intIndex);
    // normalize the intersection point location
    final int nextSegIndex = normalizedSegmentIndex + 1;
    if (nextSegIndex < getVertexCount()) {
      final double nextX = getX(nextSegIndex);
      final double nextY = getY(nextSegIndex);
      // Normalize segment index if intPt falls on vertex
      // The check for point equality is 2D only - Z values are ignored
      if (intPt.equalsVertex(nextX, nextY)) {
        normalizedSegmentIndex = nextSegIndex;
        dist = 0.0;
      }
    }
    /**
     * Add the intersection point to edge intersection list.
     */
    this.eiList.add(intPt.getX(), intPt.getY(), normalizedSegmentIndex, dist);
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

  @Override
  public Edge clone() {
    try {
      return (Edge)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw Exceptions.wrap(e);
    }
  }

  /**
   * Update the IM with the contribution for this component.
   * A component only contributes if it has a labelling for both parent geometries
   */
  @Override
  public void computeIM(final IntersectionMatrix im) {
    updateIM(this.label, im);
  }

  /**
   * equals is defined to be:
   * <p>
   * e1 equals e2
   * <b>iff</b>
   * the coordinates of e1 are the same or the reverse of the coordinates in e2
   */
  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof Edge)) {
      return false;
    }
    final Edge e = (Edge)o;

    final int vertexCount = getVertexCount();
    if (vertexCount != e.getVertexCount()) {
      return false;
    }

    boolean isEqualForward = true;
    boolean isEqualReverse = true;
    int iRev = vertexCount;
    for (int i = 0; i < vertexCount; i++) {
      if (!getPoint(i).equals(2, e.getPoint(i))) {
        isEqualForward = false;
      }
      if (!getPoint(i).equals(2, e.getPoint(--iRev))) {
        isEqualReverse = false;
      }
      if (!isEqualForward && !isEqualReverse) {
        return false;
      }
    }
    return true;
  }

  public Edge getCollapsedEdge() {
    final LineString points = new LineStringDouble(getPoint(0), getPoint(1));
    final Label lineLabel = Label.toLineLabel(this.label);
    final Edge edge = new Edge(points, lineLabel);
    return edge;
  }

  public Depth getDepth() {
    return this.depth;
  }

  // of this edge

  /**
   * The depthDelta is the change in depth as an edge is crossed from R to L
   * @return the change in depth as the edge is crossed from R to L
   */
  public int getDepthDelta() {
    return this.depthDelta;
  }

  public EdgeIntersectionList getEdgeIntersectionList() {
    return this.eiList;
  }

  @Override
  public LineString getLineString() {
    return this.line;
  }

  public int getMaximumSegmentIndex() {
    return getVertexCount() - 1;
  }

  public MonotoneChainEdge getMonotoneChainEdge() {
    if (this.mce == null) {
      this.mce = new MonotoneChainEdge(this);
    }
    return this.mce;
  }

  @Override
  public Point getPoint() {
    final LineString line = getLineString();
    return line.getPoint();
  }

  @Override
  public double getX(final int vertexIndex) {
    return this.line.getX(vertexIndex);
  }

  @Override
  public double getY(final int vertexIndex) {
    return this.line.getY(vertexIndex);
  }

  /**
   * An Edge is collapsed if it is an Area edge and it consists of
   * two segments which are equal and opposite (eg a zero-width V).
   */
  public boolean isCollapsed() {
    if (!this.label.isArea()) {
      return false;
    } else if (getVertexCount() != 3) {
      return false;
    } else {
      final double x1 = getX(0);
      final double x2 = getX(2);
      if (x1 == x2) {
        final double y1 = getY(0);
        final double y2 = getY(2);
        return y1 == y2;
      } else {
        return false;
      }
    }
  }

  @Override
  public boolean isIsolated() {
    return this.isIsolated;
  }

  /**
   * @return true if the coordinate sequences of the Edges are identical
   */
  public boolean isPointwiseEqual(final Edge e) {
    final int vertexCount = getVertexCount();
    if (vertexCount != e.getVertexCount()) {
      return false;
    }

    for (int i = 0; i < vertexCount; i++) {
      if (!getPoint(i).equals(2, e.getPoint(i))) {
        return false;
      }
    }
    return true;
  }

  public void setDepthDelta(final int depthDelta) {
    this.depthDelta = depthDelta;
  }

  public void setIsolated(final boolean isIsolated) {
    this.isIsolated = isIsolated;
  }

  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    final StringBuilder buf = new StringBuilder();
    buf.append("edge " + this.name + ": ");
    buf.append(toWkt());
    buf.append("  " + this.label + " " + this.depthDelta);
    return buf.toString();
  }

}
