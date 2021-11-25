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

package com.revolsys.geometry.planargraph;

import org.jeometry.common.function.BiConsumerDouble;
import org.jeometry.common.function.Consumer3Double;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Point;

/**
 * A node in a {@link PlanarGraph}is a location where 0 or more {@link Edge}s
 * meet. A node is connected to each of its incident Edges via an outgoing
 * DirectedEdge. Some clients using a <code>PlanarGraph</code> may want to
 * subclass <code>Node</code> to add their own application-specific
 * data and methods.
 *
 * @version 1.7
 */
public class Node extends GraphComponent implements Point {
  private static final long serialVersionUID = 1L;

  /** The collection of DirectedEdges that leave this Node */
  private final DirectedEdgeStar deStar;

  /** The location of this Node */
  private double x;

  private final double y;

  /**
   * Constructs a Node with the given location.
   */
  public Node(final double x, final double y) {
    this.x = x;
    this.y = y;
    this.deStar = new DirectedEdgeStar();
  }

  /**
   * Adds an outgoing DirectedEdge to this Node.
   */
  public void addOutEdge(final DirectedEdge de) {
    this.deStar.add(de);
  }

  @Override
  public Point clone() {
    return this;
  }

  @Override
  public void copyCoordinates(final double[] coordinates) {
    coordinates[X] = this.x;
    coordinates[Y] = this.y;
    for (int i = 3; i < coordinates.length; i++) {
      coordinates[i] = Double.NaN;
    }
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
  public void forEachVertex(final BiConsumerDouble action) {
    action.accept(this.x, this.y);
  }

  @Override
  public void forEachVertex(final Consumer3Double action) {
    action.accept(this.x, this.y, Double.NaN);
  }

  @Override
  public int getAxisCount() {
    return 2;
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    if (axisIndex == X) {
      return this.x;
    } else if (axisIndex == Y) {
      return this.y;
    } else {
      return Double.NaN;
    }
  }

  /**
   * Returns the number of edges around this Node.
   */
  public int getDegree() {
    return this.deStar.getDegree();
  }

  /**
   * Returns the zero-based index of the given Edge, after sorting in ascending order
   * by angle with the positive x-axis.
   */
  public int getIndex(final Edge edge) {
    return this.deStar.getIndex(edge);
  }

  /**
   * Returns the collection of DirectedEdges that leave this Node.
   */
  public DirectedEdgeStar getOutEdges() {
    return this.deStar;
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
   * Tests whether this node has been removed from its containing graph
   *
   * @return <code>true</code> if this node is removed
   */
  @Override
  public boolean isRemoved() {
    return Double.isNaN(this.x);
  }

  /**
   * Removes this node from its containing graph.
   */
  void remove() {
    this.x = Double.NaN;
  }

  /**
   * Removes a {@link DirectedEdge} incident on this node.
   * Does not change the state of the directed edge.
   */
  public void remove(final DirectedEdge de) {
    this.deStar.remove(de);
  }

}
