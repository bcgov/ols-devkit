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
package com.revolsys.geometry.operation.buffer;

/**
 * @version 1.7
 */
import java.util.List;

import com.revolsys.geometry.algorithm.CGAlgorithms;
import com.revolsys.geometry.algorithm.CGAlgorithmsDD;
import com.revolsys.geometry.geomgraph.DirectedEdge;
import com.revolsys.geometry.geomgraph.DirectedEdgeStar;
import com.revolsys.geometry.geomgraph.Edge;
import com.revolsys.geometry.geomgraph.Node;
import com.revolsys.geometry.geomgraph.Position;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.util.Assert;

/**
 * A RightmostEdgeFinder find the DirectedEdge in a list which has the highest coordinate,
 * and which is oriented L to R at that point. (I.e. the right side is on the RHS of the edge.)
 *
 * @version 1.7
 */
class RightmostEdgeFinder {

  private Point minCoord = null;

  private DirectedEdge minDe = null;

  // private Point extremeCoord;
  private int minIndex = -1;

  private DirectedEdge orientedDe = null;

  /**
   * A RightmostEdgeFinder finds the DirectedEdge with the rightmost coordinate.
   * The DirectedEdge returned is guaranteed to have the R of the world on its RHS.
   */
  public RightmostEdgeFinder() {
  }

  private void checkForRightmostCoordinate(final DirectedEdge de) {
    final Edge edge = de.getEdge();
    final LineString line = edge.getLineString();
    final int maxVertexCount = line.getVertexCount() - 1;
    for (int i = 0; i < maxVertexCount; i++) {
      // only check vertices which are the start or end point of a
      // non-horizontal segment
      // <FIX> MD 19 Sep 03 - NO! we can test all vertices, since the rightmost
      // must have a non-horiz segment adjacent to it
      final int i1 = i;
      final double x = line.getX(i1);
      if (this.minCoord == null || x > this.minCoord.getX()) {
        this.minDe = de;
        this.minIndex = i;
        this.minCoord = line.getPoint(i1);
      }
      // }
    }
  }

  public void findEdge(final List<DirectedEdge> dirEdgeList) {
    /**
     * Check all forward DirectedEdges only.  This is still general,
     * because each edge has a forward DirectedEdge.
     */
    for (final DirectedEdge de : dirEdgeList) {
      if (de.isForward()) {
        checkForRightmostCoordinate(de);
      }
    }

    /**
     * If the rightmost point is a node, we need to identify which of
     * the incident edges is rightmost.
     */
    Assert.isTrue(
      this.minIndex != 0 || this.minCoord.equalsVertex(this.minDe.getX1(), this.minDe.getY1()),
      "inconsistency in rightmost processing");
    if (this.minIndex == 0) {
      findRightmostEdgeAtNode();
    } else {
      findRightmostEdgeAtVertex();
    }
    /**
     * now check that the extreme side is the R side.
     * If not, use the sym instead.
     */
    this.orientedDe = this.minDe;
    final int rightmostSide = getRightmostSide(this.minDe, this.minIndex);
    if (rightmostSide == Position.LEFT) {
      this.orientedDe = this.minDe.getSym();
    }
  }

  private void findRightmostEdgeAtNode() {
    final Node node = this.minDe.getNode();
    final DirectedEdgeStar star = (DirectedEdgeStar)node.getEdges();
    this.minDe = star.getRightmostEdge();
    // the DirectedEdge returned by the previous call is not
    // necessarily in the forward direction. Use the sym edge if it isn't.
    if (!this.minDe.isForward()) {
      this.minDe = this.minDe.getSym();
      this.minIndex = this.minDe.getEdge().getVertexCount() - 1;
    }
  }

  private void findRightmostEdgeAtVertex() {
    /**
     * The rightmost point is an interior vertex, so it has a segment on either side of it.
     * If these segments are both above or below the rightmost point, we need to
     * determine their relative orientation to decide which is rightmost.
     */
    final Edge edge = this.minDe.getEdge();
    Assert.isTrue(this.minIndex > 0 && this.minIndex < edge.getVertexCount(),
      "rightmost point expected to be interior vertex of edge");
    final Point pPrev = edge.getPoint(this.minIndex - 1);
    final Point pNext = edge.getPoint(this.minIndex + 1);
    final int orientation = CGAlgorithmsDD.orientationIndex(this.minCoord, pNext, pPrev);
    boolean usePrev = false;
    // both segments are below min point
    if (pPrev.getY() < this.minCoord.getY() && pNext.getY() < this.minCoord.getY()
      && orientation == CGAlgorithms.COUNTERCLOCKWISE) {
      usePrev = true;
    } else if (pPrev.getY() > this.minCoord.getY() && pNext.getY() > this.minCoord.getY()
      && orientation == CGAlgorithms.CLOCKWISE) {
      usePrev = true;
    }
    // if both segments are on the same side, do nothing - either is safe
    // to select as a rightmost segment
    if (usePrev) {
      this.minIndex = this.minIndex - 1;
    }
  }

  public Point getCoordinate() {
    return this.minCoord;
  }

  public DirectedEdge getEdge() {
    return this.orientedDe;
  }

  private int getRightmostSide(final DirectedEdge de, final int index) {
    int side = getRightmostSideOfSegment(de, index);
    if (side < 0) {
      side = getRightmostSideOfSegment(de, index - 1);
    }
    if (side < 0) {
      // reaching here can indicate that segment is horizontal
      // Assert.shouldNeverReachHere("problem with finding rightmost side of
      // segment at "
      // + de.getCoordinate());
      // testing only
      this.minCoord = null;
      checkForRightmostCoordinate(de);
    }
    return side;
  }

  private int getRightmostSideOfSegment(final DirectedEdge de, final int i) {
    final Edge edge = de.getEdge();
    final LineString line = edge.getLineString();
    if (i < 0 || i + 1 >= line.getVertexCount()) {
      return -1;
    }
    final double y1 = line.getY(i);
    final double y2 = line.getY(i + 1);
    if (y1 == y2) {
      return -1; // indicates edge is parallel to x-axis
    }

    int pos = Position.LEFT;
    if (y1 < y2) {
      pos = Position.RIGHT;
    }
    return pos;
  }
}
