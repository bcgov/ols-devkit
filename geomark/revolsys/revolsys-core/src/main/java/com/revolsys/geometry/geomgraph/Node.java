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

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.IntersectionMatrix;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Point;

/**
 * @version 1.7
 */
public class Node extends GraphComponent implements Point {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final double x;

  private final double y;

  protected EdgeEndStar edges;

  public Node(final double x, final double y, final EdgeEndStar edges) {
    this.x = x;
    this.y = y;
    this.edges = edges;
    this.label = new Label(0, Location.NONE);
  }

  /**
   * Add the edge to the list of edges at this node
   */
  public void add(final EdgeEnd e) {
    // Assert: start pt of e is equal to node point
    this.edges.insert(e);
    e.setNode(this);
  }

  @Override
  public Point clone() {
    return this;
  }

  /**
   * Basic nodes do not compute IMs
   */
  @Override
  protected void computeIM(final IntersectionMatrix im) {
  }

  /**
   * The location for a given eltIndex for a node will be one
   * of { null, INTERIOR, BOUNDARY }.
   * A node may be on both the boundary and the interior of a geometry;
   * in this case, the rule is that the node is considered to be in the boundary.
   * The merged location is the maximum of the two input values.
   */
  Location computeMergedLocation(final Label label2, final int eltIndex) {
    Location loc = Location.NONE;
    loc = this.label.getLocation(eltIndex);
    if (!label2.isNull(eltIndex)) {
      final Location nLoc = label2.getLocation(eltIndex);
      if (loc != Location.BOUNDARY) {
        loc = nLoc;
      }
    }
    return loc;
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof Point) {
      final Point point = (Point)other;
      return equals(point);
    } else if (other instanceof BoundingBox) {
      final BoundingBox boundingBox = (BoundingBox)other;
      return bboxEquals(boundingBox);
    } else {
      return false;
    }
  }

  @Override
  public int getAxisCount() {
    return 2;
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    if (axisIndex == 0) {

    }
    return 0;
  }

  public EdgeEndStar getEdges() {
    return this.edges;
  }

  @Override
  public Point getPoint() {
    return this;
  }

  @Override
  public double getX() {
    return this.x;
  }

  @Override
  public double getY() {
    return this.y;
  }

  @Override
  public int hashCode() {
    return Point.hashCode(this);
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  /**
   * Tests whether any incident edge is flagged as
   * being in the result.
   * This test can be used to determine if the node is in the result,
   * since if any incident edge is in the result, the node must be in the result as well.
   *
   * @return <code>true</code> if any indicident edge in the in the result
   */
  public boolean isIncidentEdgeInResult() {
    for (final Object element : getEdges().getEdges()) {
      final DirectedEdge de = (DirectedEdge)element;
      if (de.getEdge().isInResult()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isIsolated() {
    return this.label.getGeometryCount() == 1;
  }

  /**
   * To merge labels for two nodes,
   * the merged location for each LabelElement is computed.
   * The location for the corresponding node LabelElement is set to the result,
   * as long as the location is non-null.
   */

  public void mergeLabel(final Label label2) {
    for (int i = 0; i < 2; i++) {
      final Location loc = computeMergedLocation(label2, i);
      final Location thisLoc = this.label.getLocation(i);
      if (thisLoc == Location.NONE) {
        this.label.setLocation(i, loc);
      }
    }
  }

  public void mergeLabel(final Node n) {
    mergeLabel(n.label);
  }

  public void print(final PrintStream out) {
    out.println("node " + toString() + " lbl: " + this.label);
  }

  public void setLabel(final int argIndex, final Location onLocation) {
    if (this.label == null) {
      this.label = new Label(argIndex, onLocation);
    } else {
      this.label.setLocation(argIndex, onLocation);
    }
  }

  /**
   * Updates the label of a node to BOUNDARY,
   * obeying the mod-2 boundaryDetermination rule.
   */
  public void setLabelBoundary(final int argIndex) {
    if (this.label == null) {
      return;
    }

    // determine the current location for the point (if any)
    Location loc = Location.NONE;
    if (this.label != null) {
      loc = this.label.getLocation(argIndex);
    }
    // flip the loc
    Location newLoc;
    switch (loc) {
      case BOUNDARY:
        newLoc = Location.INTERIOR;
      break;
      case INTERIOR:
        newLoc = Location.BOUNDARY;
      break;
      default:
        newLoc = Location.BOUNDARY;
      break;
    }
    this.label.setLocation(argIndex, newLoc);
  }

  @Override
  public String toString() {
    return toWkt();
  }
}
