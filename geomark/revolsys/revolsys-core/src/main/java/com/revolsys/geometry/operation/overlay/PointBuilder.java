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
package com.revolsys.geometry.operation.overlay;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.algorithm.PointLocator;
import com.revolsys.geometry.geomgraph.Label;
import com.revolsys.geometry.geomgraph.Node;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;

/**
 * Constructs {@link Point}s from the nodes of an overlay graph.
 * @version 1.7
 */
public class PointBuilder {
  private final GeometryFactory geometryFactory;

  private final OverlayOp op;

  private final List<Point> resultLineString = new ArrayList<>();

  public PointBuilder(final OverlayOp op, final GeometryFactory geometryFactory,
    final PointLocator ptLocator) {
    this.op = op;
    this.geometryFactory = geometryFactory;
    // ptLocator is never used in this class
  }

  /**
   * Computes the Point geometries which will appear in the result,
   * given the specified overlay operation.
   *
   * @return a list of the Point objects in the result
   */
  public List<Point> build(final int opCode) {
    extractNonCoveredResultNodes(opCode);
    /**
     * It can happen that connected result nodes are still covered by
     * result geometries, so must perform this filter.
     * (For instance, this can happen during topology collapse).
     */
    return this.resultLineString;
  }

  /**
   * Determines nodes which are in the result, and creates {@link Point}s for them.
   *
   * This method determines nodes which are candidates for the result via their
   * labelling and their graph topology.
   *
   * @param opCode the overlay operation
   */
  private void extractNonCoveredResultNodes(final int opCode) {
    // testing only
    // if (true) return resultNodeList;

    for (final Object element : this.op.getGraph().getNodes()) {
      final Node n = (Node)element;

      // filter out nodes which are known to be in the result
      if (n.isInResult()) {
        continue;
      }
      // if an incident edge is in the result, then the node
      // coordinate is
      // included already
      if (n.isIncidentEdgeInResult()) {
        continue;
      }
      if (n.getEdges().getDegree() == 0 || opCode == OverlayOp.INTERSECTION) {

        /**
         * For nodes on edges, only INTERSECTION can result in edge nodes being included even
         * if none of their incident edges are included
         */
        final Label label = n.getLabel();
        if (OverlayOp.isResultOfOp(label, opCode)) {
          filterCoveredNodeToPoint(n);
        }
      }
    }
    // System.out.println("connectedResultNodes collected = " +
    // connectedResultNodes.size());
  }

  /**
   * Converts non-covered nodes to Point objects and adds them to the result.
   *
   * A node is covered if it is contained in another element Geometry
   * with higher dimension (e.g. a node point might be contained in a polygon,
   * in which case the point can be eliminated from the result).
   *
   * @param n the node to test
   */
  private void filterCoveredNodeToPoint(final Node n) {
    final double x = n.getX();
    final double y = n.getY();
    if (!this.op.isCoveredByLA(x, y)) {
      final Point pt = this.geometryFactory.point(x, y);
      this.resultLineString.add(pt);
    }
  }
}
