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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.revolsys.geometry.algorithm.CGAlgorithms;
import com.revolsys.geometry.algorithm.CGAlgorithmsDD;
import com.revolsys.geometry.geomgraph.Quadrant;
import com.revolsys.geometry.model.Point;

/**
 * Represents a directed edge in a {@link PlanarGraph}. A DirectedEdge may or
 * may not have a reference to a parent {@link Edge} (some applications of
 * planar graphs may not require explicit Edge objects to be created). Usually
 * a client using a <code>PlanarGraph</code> will subclass <code>DirectedEdge</code>
 * to add its own application-specific data and methods.
 *
 * @version 1.7
 */
public class DirectedEdge extends GraphComponent implements Comparable<DirectedEdge> {
  /**
   * Returns a List containing the parent Edge (possibly null) for each of the given
   * DirectedEdges.
   */
  public static List<Edge> toEdges(final Collection<DirectedEdge> dirEdges) {
    final List<Edge> edges = new ArrayList<>();
    for (final DirectedEdge directedEdge : dirEdges) {
      edges.add(directedEdge.parentEdge);
    }
    return edges;
  }

  private final double angle;

  private final boolean edgeDirection;

  private final Node from;

  private final Point directionPoint;

  private Edge parentEdge;

  private final int quadrant;

  private DirectedEdge sym = null; // optional

  private final Node to;

  /**
   * Constructs a DirectedEdge connecting the <code>from</code> node to the
   * <code>to</code> node.
   *
   * @param directionPoint
   *   specifies this DirectedEdge's direction vector
   *   (determined by the vector from the <code>from</code> node
   *   to <code>directionPt</code>)
   * @param edgeDirection
   *   whether this DirectedEdge's direction is the same as or
   *   opposite to that of the parent Edge (if any)
   */
  public DirectedEdge(final Node from, final Node to, final Point directionPoint,
    final boolean edgeDirection) {
    this.from = from;
    this.to = to;
    this.edgeDirection = edgeDirection;
    this.directionPoint = directionPoint;
    final double dx = this.directionPoint.getX() - from.getX();
    final double dy = this.directionPoint.getY() - from.getY();
    this.quadrant = Quadrant.quadrant(dx, dy);
    this.angle = Math.atan2(dy, dx);
    // Assert.isTrue(! (dx == 0 && dy == 0),
    // "EdgeEnd with identical endpoints found");
  }

  /**
   * Returns 1 if this DirectedEdge has a greater angle with the
   * positive x-axis than b", 0 if the DirectedEdges are collinear, and -1 otherwise.
   * <p>
   * Using the obvious algorithm of simply computing the angle is not robust,
   * since the angle calculation is susceptible to roundoff. A robust algorithm
   * is:
   * <ul>
   * <li>first compare the quadrants. If the quadrants are different, it it
   * trivial to determine which vector is "greater".
   * <li>if the vectors lie in the same quadrant, the robust
   * {@link CGAlgorithms#computeOrientation(Point, Point, Point)}
   * function can be used to decide the relative orientation of the vectors.
   * </ul>
   */
  public int compareDirection(final DirectedEdge e) {
    // if the rays are in different quadrants, determining the ordering is
    // trivial
    if (this.quadrant > e.quadrant) {
      return 1;
    }
    if (this.quadrant < e.quadrant) {
      return -1;
    }
    // vectors are in the same quadrant - check relative orientation of
    // direction vectors
    // this is > e if it is CCW of e
    /**
     * MD - 9 Aug 2010 It seems that the basic algorithm is slightly orientation
     * dependent, when computing the orientation of a point very close to a
     * line. This is possibly due to the arithmetic in the translation to the
     * origin.
     *
     * For instance, the following situation produces identical results in spite
     * of the inverse orientation of the line segment:
     *
     * Point p0 = new PointDouble((double)219.3649559090992, 140.84159161824724);
     * Point p1 = new PointDouble((double)168.9018919682399, -5.713787599646864);
     *
     * Point p = new PointDouble((double)186.80814046338352, 46.28973405831556); int
     * orient = orientationIndex(p0, p1, p); int orientInv =
     * orientationIndex(p1, p0, p);
     *
     * A way to force consistent results is to normalize the orientation of the
     * vector using the following code. However, this may make the results of
     * orientationIndex inconsistent through the triangle of points, so it's not
     * clear this is an appropriate patch.
     *
     */
    return CGAlgorithmsDD.orientationIndex(e.from, e.directionPoint, this.directionPoint);
    // testing only
    // return ShewchuksDeterminant.orientationIndex(p1, p2, q);
    // previous implementation - not quite fully robust
    // return RobustDeterminant.orientationIndex(p1, p2, q);
  }

  /**
   * Returns 1 if this DirectedEdge has a greater angle with the
   * positive x-axis than b", 0 if the DirectedEdges are collinear, and -1 otherwise.
   * <p>
   * Using the obvious algorithm of simply computing the angle is not robust,
   * since the angle calculation is susceptible to roundoff. A robust algorithm
   * is:
   * <ul>
   * <li>first compare the quadrants. If the quadrants are different, it it
   * trivial to determine which vector is "greater".
   * <li>if the vectors lie in the same quadrant, the robust
   * {@link CGAlgorithms#computeOrientation(Point, Point, Point)}
   * function can be used to decide the relative orientation of the vectors.
   * </ul>
   */
  @Override
  public int compareTo(final DirectedEdge de) {
    return compareDirection(de);
  }

  /**
   * Returns the angle that the start of this DirectedEdge makes with the
   * positive x-axis, in radians.
   */
  public double getAngle() {
    return this.angle;
  }

  /**
   * Returns a point to which an imaginary line is drawn from the from-node to
   * specify this DirectedEdge's orientation.
   */
  public Point getDirectionPoint() {
    return this.directionPoint;
  }

  /**
   * Returns this DirectedEdge's parent Edge, or null if it has none.
   */
  public Edge getEdge() {
    return this.parentEdge;
  }

  /**
   * Returns whether the direction of the parent Edge (if any) is the same as that
   * of this Directed Edge.
   */
  public boolean getEdgeDirection() {
    return this.edgeDirection;
  }

  /**
   * Returns the node from which this DirectedEdge leaves.
   */
  public Node getFromNode() {
    return this.from;
  }

  /**
   * Returns 0, 1, 2, or 3, indicating the quadrant in which this DirectedEdge's
   * orientation lies.
   */
  public int getQuadrant() {
    return this.quadrant;
  }

  /**
   * Returns the symmetric DirectedEdge -- the other DirectedEdge associated with
   * this DirectedEdge's parent Edge.
   */
  public DirectedEdge getSym() {
    return this.sym;
  }

  /**
   * Returns the node to which this DirectedEdge goes.
   */
  public Node getToNode() {
    return this.to;
  }

  /**
   * Tests whether this directed edge has been removed from its containing graph
   *
   * @return <code>true</code> if this directed edge is removed
   */
  @Override
  public boolean isRemoved() {
    return this.parentEdge == null;
  }

  /**
   * Prints a detailed string representation of this DirectedEdge to the given PrintStream.
   */
  public void print(final PrintStream out) {
    final String className = getClass().getName();
    final int lastDotPos = className.lastIndexOf('.');
    final String name = className.substring(lastDotPos + 1);
    out.print("  " + name + ": " + this.from + " - " + this.directionPoint + " " + this.quadrant
      + ":" + this.angle);
  }

  /**
   * Removes this directed edge from its containing graph.
   */
  void remove() {
    this.sym = null;
    this.parentEdge = null;
  }

  /**
   * Associates this DirectedEdge with an Edge (possibly null, indicating no associated
   * Edge).
   */
  public void setEdge(final Edge parentEdge) {
    this.parentEdge = parentEdge;
  }

  /**
   * Sets this DirectedEdge's symmetric DirectedEdge, which runs in the opposite
   * direction.
   */
  public void setSym(final DirectedEdge sym) {
    this.sym = sym;
  }

}
