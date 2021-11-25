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

import com.revolsys.geometry.algorithm.BoundaryNodeRule;
import com.revolsys.geometry.algorithm.CGAlgorithmsDD;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;

/**
 * Models the end of an edge incident on a node.
 * EdgeEnds have a direction
 * determined by the direction of the ray from the initial
 * point to the next point.
 * EdgeEnds are comparable under the ordering
 * "a has a greater angle with the x-axis than b".
 * This ordering is used to sort EdgeEnds around a node.
 * @version 1.7
 */
public class EdgeEnd implements Comparable<Object> {
  private double dx;

  private double dy; // the direction vector for this edge from its starting

  protected Edge edge; // the parent edge of this edge end

  private Label label;

  private Node node; // the node this edge end originates at

  // point

  private int quadrant;

  private double x1;

  private double y1;

  private double x2;

  private double y2;

  protected EdgeEnd(final Edge edge) {
    this.edge = edge;
  }

  public EdgeEnd(final Edge edge, final double x1, final double y1, final double x2,
    final double y2) {
    this(edge, x1, y1, x2, y2, null);
  }

  public EdgeEnd(final Edge edge, final double x1, final double y1, final double x2,
    final double y2, final Label label) {
    this(edge);
    init(x1, y1, x2, y2);
    this.setLabel(label);
  }

  /**
   * Implements the total order relation:
   * <p>
   *    a has a greater angle with the positive x-axis than b
   * <p>
   * Using the obvious algorithm of simply computing the angle is not robust,
   * since the angle calculation is obviously susceptible to roundoff.
   * A robust algorithm is:
   * - first compare the quadrant.  If the quadrants
   * are different, it it trivial to determine which vector is "greater".
   * - if the vectors lie in the same quadrant, the computeOrientation function
   * can be used to decide the relative orientation of the vectors.
   */
  public int compareDirection(final EdgeEnd e) {
    final double dx = getDx();
    final double dy = getDy();
    if (dx == e.getDx() && dy == e.getDy()) {
      return 0;
    }
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
    return CGAlgorithmsDD.orientationIndex(e.x1, e.y1, e.x2, e.y2, this.x2, this.y2);
    // testing only
    // return ShewchuksDeterminant.orientationIndex(p1, p2, q);
    // previous implementation - not quite fully robust
    // return RobustDeterminant.orientationIndex(p1, p2, q);
  }

  @Override
  public int compareTo(final Object obj) {
    final EdgeEnd e = (EdgeEnd)obj;
    return compareDirection(e);
  }

  public void computeLabel(final BoundaryNodeRule boundaryNodeRule) {
    // subclasses should override this if they are using labels
  }

  public Point getCoordinate() {
    return new PointDoubleXY(this.x1, this.y1);
  }

  public Point getDirectedCoordinate() {
    return new PointDoubleXY(this.x2, this.y2);
  }

  public double getDx() {
    return this.dx;
  }

  public double getDy() {
    return this.dy;
  }

  public Edge getEdge() {
    return this.edge;
  }

  public Label getLabel() {
    return this.label;
  }

  public Node getNode() {
    return this.node;
  }

  public int getQuadrant() {
    return this.quadrant;
  }

  public double getX1() {
    return this.x1;
  }

  public double getX2() {
    return this.x2;
  }

  public double getY1() {
    return this.y1;
  }

  public double getY2() {
    return this.y2;
  }

  protected void init(final double x1, final double y1, final double x2, final double y2) {
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
    this.dx = x2 - x1;
    this.dy = y2 - y1;
    this.quadrant = Quadrant.quadrant(this.dx, this.dy);
    if (this.dx == 0 && this.dy == 0) {
      throw new IllegalArgumentException("EdgeEnd with identical endpoints found");
    }
  }

  public void print(final PrintStream out) {
    final double angle = Math.atan2(getDy(), getDx());
    final String className = getClass().getName();
    final int lastDotPos = className.lastIndexOf('.');
    final String name = className.substring(lastDotPos + 1);
    out.print("  " + name + ": " + this.x1 + "," + this.y1 + " - " + this.x2 + "," + this.y2 + " "
      + this.quadrant + ":" + angle + "   " + getLabel());
  }

  protected void setLabel(final Label label) {
    this.label = label;
  }

  public void setNode(final Node node) {
    this.node = node;
  }

  @Override
  public String toString() {
    final double angle = Math.atan2(getDy(), getDx());
    final String className = getClass().getName();
    final int lastDotPos = className.lastIndexOf('.');
    final String name = className.substring(lastDotPos + 1);
    return "  " + name + ": " + this.x1 + "," + this.y1 + " - " + this.x2 + "," + this.y2 + " "
      + this.quadrant + ":" + angle + "   " + getLabel();
  }
}
