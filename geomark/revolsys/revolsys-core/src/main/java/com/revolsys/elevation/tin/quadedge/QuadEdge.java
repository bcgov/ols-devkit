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

package com.revolsys.elevation.tin.quadedge;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Side;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDoubleGF;

/**
 * A class that represents the edge data structure which implements the quadedge algebra.
 * The quadedge algebra was described in a well-known paper by Guibas and Stolfi,
 * "Primitives for the manipulation of general subdivisions and the computation of Voronoi diagrams",
 * <i>ACM Transactions on Graphics</i>, 4(2), 1985, 75-123.
 * <p>
 * Each edge object is part of a quartet of 4 edges,
 * linked via their <tt>rot</tt> references.
 * Any edge in the group may be accessed using a series of {@link #rot()} operations.
 * Quadedges in a subdivision are linked together via their <tt>next</tt> references.
 * The linkage between the quadedge quartets determines the topology
 * of the subdivision.
 * <p>
 * The edge class does not contain separate information for vertices or faces; a PointDoubleXYZ is implicitly
 * defined as a ring of edges (created using the <tt>next</tt> field).
 *
 * @author David Skea
 * @author Martin Davis
 */
public class QuadEdge {
  private QuadEdge next; // A reference to a connected edge

  // the dual of this edge, directed from right to left
  private QuadEdge rot;

  private Point fromPoint; // The Point that this edge
                           // represents

  private short visitIndex = Short.MIN_VALUE;

  /**
   * Quadedges must be made using {@link makeEdge},
   * to ensure proper construction.
   */
  QuadEdge() {

  }

  QuadEdge(final Point fromPoint) {
    this.fromPoint = fromPoint;
    this.rot = null;
    this.next = this;
  }

  public QuadEdge(final Point fromPoint, final Point toPoint) {
    final QuadEdge q2 = new QuadEdge(toPoint);
    final QuadEdge q1 = new QuadEdge(null, q2, null);
    final QuadEdge q3 = new QuadEdge(null, this, q1);

    q2.rot = q3;
    q1.next = q3;

    this.fromPoint = fromPoint;
    this.rot = q1;
    this.next = this;

  }

  QuadEdge(final Point fromPoint, final QuadEdge rot, final QuadEdge next) {
    this.fromPoint = fromPoint;
    this.rot = rot;
    this.next = next;
  }

  // final QuadEdge q1 = base.rot();
  // final QuadEdge q2 = new QuadEdge(toPoint);
  // final QuadEdge q3 = q2.rot();
  //
  // q1.init(q2, q3);
  // q3.init(base, q1);

  /**
   * Marks this quadedge as being deleted.
   * This does not free the memory used by
   * this quadedge quartet, but indicates
   * that this edge no longer participates
   * in a subdivision.
   *
   */
  public void delete() {
    this.rot = null;
  }

  /**
   * Gets the next CCW edge around (into) the destination of this edge.
   *
   * @return the next destination edge.
   */
  public final QuadEdge dNext() {
    return this.sym().next.sym();
  }

  @Override
  public boolean equals(final Object other) {
    return other == this;
  }

  /**
   * Gets the next CCW edge around the origin of this edge.
   *
   * @return the next linked edge.
   */
  public final QuadEdge getFromNextEdge() {
    return this.next;
  }

  /**
   * Gets the Point for the edge's origin
   *
   * @return the origin Point
   */

  public final Point getFromPoint() {
    return this.fromPoint;
  }

  /**
   * Gets the CCW edge around the left face following this edge.
   *
   * @return the next left face edge.
   */
  public final QuadEdge getLeftNext() {
    return invRot().next.rot;
  }

  /**
   * Gets the CCW edge around the left face before this edge.
   *
   * @return the previous left face edge.
   */
  public final QuadEdge getLeftPrevious() {
    return this.next.sym();
  }

  /**
   * Gets the primary edge of this quadedge and its <tt>sym</tt>.
   * The primary edge is the one for which the origin
   * and destination points are ordered
   * according to the standard {@link Point} ordering
   *
   * @return the primary quadedge
   */
  public QuadEdge getPrimary() {
    final Point toPoint = getToPoint();
    if (this.fromPoint.compareTo(toPoint) <= 0) {
      return this;
    } else {
      return sym();
    }
  }

  /**
   * Gets the edge around the right face ccw following this edge.
   *
   * @return the next right face edge.
   */
  public final QuadEdge getRightNext() {
    return this.rot.next.invRot();
  }

  /**
   * Gets the edge around the right face ccw before this edge.
   *
   * @return the previous right face edge.
   */
  public final QuadEdge getRightPrevious() {
    return this.sym().next;
  }

  public Side getSide(final double x, final double y) {
    final double x1 = this.fromPoint.getX();
    final double y1 = this.fromPoint.getY();
    final Point toPoint = getToPoint();
    final double x2 = toPoint.getX();
    final double y2 = toPoint.getY();
    return Side.getSide(x1, y1, x2, y2, x, y);
  }

  /**
   * Gets the next CW edge around (into) the destination of this edge.
   *
   * @return the previous destination edge.
   */
  public final QuadEdge getToNextEdge() {
    return this.invRot().next.invRot();
  }

  /**
   * Gets the Point for the edge's destination
   *
   * @return the destination Point
   */

  public final Point getToPoint() {
    return sym().fromPoint;
  }

  void init(final QuadEdge rot, final QuadEdge next) {
    this.rot = rot;
    this.next = next;
  }

  /**
   * Gets the dual of this edge, directed from its left to its right.
   *
   * @return the inverse rotated edge.
   */
  public final QuadEdge invRot() {
    return this.rot.sym();
  }

  /**
   * Tests whether this edge has been deleted.
   *
   * @return true if this edge has not been deleted.
   */
  public boolean isLive() {
    return this.rot != null;
  }

  public boolean isSwapRequired(final double x, final double y) {
    final QuadEdge previousEdge = oPrev();
    final Point previousToPoint = previousEdge.getToPoint();
    final double previousToX = previousToPoint.getX();
    final double previousToY = previousToPoint.getY();

    final Point fromPoint = this.fromPoint;
    final double fromX = fromPoint.getX();
    final double fromY = fromPoint.getY();

    final Point toPoint = getToPoint();
    final double toX = toPoint.getX();
    final double toY = toPoint.getY();

    final double orientationIndex = (fromX - previousToX) * (toY - previousToY)
      - (fromY - previousToY) * (toX - previousToX);

    if (orientationIndex < 0) {
      // On right side
      final double fromDeltaX = fromX - x;
      final double fromDeltaY = fromY - y;
      final double toDeltaX = toX - x;
      final double toDeltaY = toY - y;
      final double previousToDeltaX = previousToX - x;
      final double previousToDeltaY = previousToY - y;
      final double abdet = fromDeltaX * previousToDeltaY - previousToDeltaX * fromDeltaY;
      final double bcdet = previousToDeltaX * toDeltaY - toDeltaX * previousToDeltaY;
      final double cadet = toDeltaX * fromDeltaY - fromDeltaX * toDeltaY;
      final double alift = fromDeltaX * fromDeltaX + fromDeltaY * fromDeltaY;
      final double blift = previousToDeltaX * previousToDeltaX
        + previousToDeltaY * previousToDeltaY;
      final double clift = toDeltaX * toDeltaX + toDeltaY * toDeltaY;

      final double disc = alift * bcdet + blift * cadet + clift * abdet;
      final boolean inCircle = disc > 0;
      if (inCircle) {
        return true;
      }
    }
    return false;
  }

  public boolean isVisited(final short visitIndex) {
    return this.visitIndex == visitIndex;
  }

  public LineSegment newLineString(final GeometryFactory geometryFactory) {
    final Point toPoint = getToPoint();
    if (this.fromPoint == null) {
      if (toPoint == null) {
        return new LineSegmentDoubleGF(geometryFactory, 2);
      } else {
        return new LineSegmentDoubleGF(geometryFactory, toPoint, toPoint);
      }
    } else if (toPoint == null) {
      return new LineSegmentDoubleGF(geometryFactory, this.fromPoint, this.fromPoint);
    } else {
      return new LineSegmentDoubleGF(geometryFactory, this.fromPoint, toPoint);
    }
  }

  /**
   * Gets the next CW edge around (from) the origin of this edge.
   *
   * @return the previous edge.
   */
  public final QuadEdge oPrev() {
    return this.rot.next.rot;
  }

  /**
   * Gets the dual of this edge, directed from its right to its left.
   *
   * @return the rotated edge
   */
  public final QuadEdge rot() {
    return this.rot;
  }

  /***********************************************************************************************
   * Data Access
   **********************************************************************************************/

  public void setVisited(final short visitIndex) {
    this.visitIndex = visitIndex;
  }

  /**
   * Splices two edges together or apart.
   * Splice affects the two edge rings around the origins of a and b, and, independently, the two
   * edge rings around the left faces of <tt>a</tt> and <tt>b</tt>.
   * In each case, (i) if the two rings are distinct,
   * Splice will combine them into one, or (ii) if the two are the same ring, Splice will break it
   * into two separate pieces. Thus, Splice can be used both to attach the two edges together, and
   * to break them apart.
   *
   * @param edge an edge to splice
   *
   */
  public void splice(final QuadEdge edge) {
    final QuadEdge fromNextEdge1 = this.next;
    final QuadEdge fromNextEdge2 = edge.next;

    final QuadEdge alpha = fromNextEdge1.rot;
    final QuadEdge beta = fromNextEdge2.rot;

    final QuadEdge fromNextEdgeRot1 = beta.next;
    final QuadEdge fromNextEdgeRot2 = alpha.next;

    this.next = fromNextEdge2;
    edge.next = fromNextEdge1;
    alpha.next = fromNextEdgeRot1;
    beta.next = fromNextEdgeRot2;
  }

  /**
   * Turns an edge counterclockwise inside its enclosing quadrilateral.
   *
   * @param edge the quadedge to turn
   */
  public void swap() {
    final QuadEdge edgePrevious = oPrev();
    final QuadEdge sym = sym();
    final QuadEdge b = sym.oPrev();
    splice(edgePrevious);
    sym.splice(b);
    splice(edgePrevious.getLeftNext());
    sym.splice(b.getLeftNext());
    this.fromPoint = edgePrevious.getToPoint();
    sym.fromPoint = b.getToPoint();
  }

  /**
   * Gets the edge from the destination to the origin of this edge.
   *
   * @return the sym of the edge
   */
  public final QuadEdge sym() {
    return this.rot.rot;
  }

  @Override
  public String toString() {
    return newLineString(GeometryFactory.DEFAULT_3D).toString();
  }
}
