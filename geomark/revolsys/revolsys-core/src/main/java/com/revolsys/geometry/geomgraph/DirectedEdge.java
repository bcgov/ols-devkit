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

import java.io.PrintStream;

import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.TopologyException;

/**
 * @version 1.7
 */
public class DirectedEdge extends EdgeEnd {

  /**
   * Computes the factor for the change in depth when moving from one location to another.
   * E.g. if crossing from the INTERIOR to the EXTERIOR the depth decreases, so the factor is -1
   */
  public static int depthFactor(final Location currLocation, final Location nextLocation) {
    if (currLocation == Location.EXTERIOR && nextLocation == Location.INTERIOR) {
      return 1;
    } else if (currLocation == Location.INTERIOR && nextLocation == Location.EXTERIOR) {
      return -1;
    }
    return 0;
  }

  /**
   * The depth of each side (position) of this edge.
   * The 0 element of the array is never used.
   */
  private final int[] depth = {
    0, -999, -999
  };

  private EdgeRing edgeRing; // the EdgeRing that this edge is part of

  protected boolean isForward;

  private boolean isInResult = false;

  private boolean isVisited = false;

  // containing this edge

  private EdgeRing minEdgeRing; // the MinimalEdgeRing that this edge is part of

  // contains this edge

  private DirectedEdge next; // the next edge in the edge ring for the polygon

  private DirectedEdge nextMin; // the next edge in the MinimalEdgeRing that

  private DirectedEdge sym; // the symmetric edge

  public DirectedEdge(final Edge edge, final boolean isForward) {
    super(edge);
    this.isForward = isForward;
    if (isForward) {
      init(edge.getX(0), edge.getY(0), edge.getX(1), edge.getY(1));
    } else {
      final int n = edge.getVertexCount() - 1;
      init(edge.getX(n), edge.getY(n), edge.getX(n - 1), edge.getY(n - 1));
    }
    computeDirectedLabel();
  }

  /**
   * Compute the label in the appropriate orientation for this DirEdge
   */
  private void computeDirectedLabel() {
    final Label label = new Label(this.edge.getLabel());
    if (!this.isForward) {
      label.flip();
    }
    setLabel(label);
  }

  public int getDepth(final int position) {
    return this.depth[position];
  }

  public int getDepthDelta() {
    int depthDelta = this.edge.getDepthDelta();
    if (!this.isForward) {
      depthDelta = -depthDelta;
    }
    return depthDelta;
  }

  @Override
  public Edge getEdge() {
    return this.edge;
  }

  public EdgeRing getEdgeRing() {
    return this.edgeRing;
  }

  public EdgeRing getMinEdgeRing() {
    return this.minEdgeRing;
  }

  public DirectedEdge getNext() {
    return this.next;
  }

  public DirectedEdge getNextMin() {
    return this.nextMin;
  }

  /**
   * Each Edge gives rise to a pair of symmetric DirectedEdges, in opposite
   * directions.
   * @return the DirectedEdge for the same Edge but in the opposite direction
   */
  public DirectedEdge getSym() {
    return this.sym;
  }

  public boolean isForward() {
    return this.isForward;
  }

  public boolean isInResult() {
    return this.isInResult;
  }

  /**
   * This is an interior Area edge if
   * <ul>
   * <li> its label is an Area label for both Geometries
   * <li> and for each Geometry both sides are in the interior.
   * </ul>
   *
   * @return true if this is an interior Area edge
   */
  public boolean isInteriorAreaEdge() {
    boolean isInteriorAreaEdge = true;
    for (int i = 0; i < 2; i++) {
      if (!(getLabel().isArea(i) && getLabel().getLocation(i, Position.LEFT) == Location.INTERIOR
        && getLabel().getLocation(i, Position.RIGHT) == Location.INTERIOR)) {
        isInteriorAreaEdge = false;
      }
    }
    return isInteriorAreaEdge;
  }

  /**
   * This edge is a line edge if
   * <ul>
   * <li> at least one of the labels is a line label
   * <li> any labels which are not line labels have all Locations = EXTERIOR
   * </ul>
   */
  public boolean isLineEdge() {
    final boolean isLine = getLabel().isLine(0) || getLabel().isLine(1);
    final boolean isExteriorIfArea0 = !getLabel().isArea(0)
      || getLabel().allPositionsEqual(0, Location.EXTERIOR);
    final boolean isExteriorIfArea1 = !getLabel().isArea(1)
      || getLabel().allPositionsEqual(1, Location.EXTERIOR);

    return isLine && isExteriorIfArea0 && isExteriorIfArea1;
  }

  public boolean isVisited() {
    return this.isVisited;
  }

  @Override
  public void print(final PrintStream out) {
    super.print(out);
    out.print(" " + this.depth[Position.LEFT] + "/" + this.depth[Position.RIGHT]);
    out.print(" (" + getDepthDelta() + ")");
    // out.print(" " + this.hashCode());
    // if (next != null) out.print(" next:" + next.hashCode());
    if (this.isInResult) {
      out.print(" inResult");
    }
  }

  public void setDepth(final int position, final int depthVal) {
    if (this.depth[position] != -999) {
      if (this.depth[position] != depthVal) {
        throw new TopologyException("assigned depths do not match", getCoordinate());
      }
    }
    this.depth[position] = depthVal;
  }

  /**
   * Set both edge depths.  One depth for a given side is provided.  The other is
   * computed depending on the Location transition and the depthDelta of the edge.
   */
  public void setEdgeDepths(final int position, final int depth) {
    // get the depth transition delta from R to L for this directed Edge
    int depthDelta = getEdge().getDepthDelta();
    if (!this.isForward) {
      depthDelta = -depthDelta;
    }

    // if moving from L to R instead of R to L must change sign of delta
    int directionFactor = 1;
    if (position == Position.LEFT) {
      directionFactor = -1;
    }

    final int oppositePos = Position.opposite(position);
    final int delta = depthDelta * directionFactor;
    // TESTINGint delta = depthDelta * DirectedEdge.depthFactor(loc,
    // oppositeLoc);
    final int oppositeDepth = depth + delta;
    setDepth(position, depth);
    setDepth(oppositePos, oppositeDepth);
  }

  public void setEdgeRing(final EdgeRing edgeRing) {
    this.edgeRing = edgeRing;
  }

  public void setInResult(final boolean isInResult) {
    this.isInResult = isInResult;
  }

  public void setMinEdgeRing(final EdgeRing minEdgeRing) {
    this.minEdgeRing = minEdgeRing;
  }

  public void setNext(final DirectedEdge next) {
    this.next = next;
  }

  public void setNextMin(final DirectedEdge nextMin) {
    this.nextMin = nextMin;
  }

  public void setSym(final DirectedEdge de) {
    this.sym = de;
  }

  public void setVisited(final boolean isVisited) {
    this.isVisited = isVisited;
  }

  /**
   * setVisitedEdge marks both DirectedEdges attached to a given Edge.
   * This is used for edges corresponding to lines, which will only
   * appear oriented in a single direction in the result.
   */
  public void setVisitedEdge(final boolean isVisited) {
    setVisited(isVisited);
    this.sym.setVisited(isVisited);
  }

}
