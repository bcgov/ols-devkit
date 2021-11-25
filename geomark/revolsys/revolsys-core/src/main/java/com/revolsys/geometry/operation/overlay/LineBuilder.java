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
import com.revolsys.geometry.geomgraph.DirectedEdge;
import com.revolsys.geometry.geomgraph.DirectedEdgeStar;
import com.revolsys.geometry.geomgraph.Edge;
import com.revolsys.geometry.geomgraph.Label;
import com.revolsys.geometry.geomgraph.Node;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;

/**
 * Forms JTS LineStrings out of a the graph of {@link DirectedEdge}s
 * created by an {@link OverlayOp}.
 *
 * @version 1.7
 */
public class LineBuilder {
  private final GeometryFactory geometryFactory;

  private final List<Edge> lineEdgesList = new ArrayList<>();

  private final OverlayOp op;

  private final List<LineString> resultLineList = new ArrayList<>();

  public LineBuilder(final OverlayOp op, final GeometryFactory geometryFactory,
    final PointLocator ptLocator) {
    this.op = op;
    this.geometryFactory = geometryFactory;
  }

  /**
   * @return a list of the LineStrings in the result of the specified overlay operation
   */
  public List<LineString> build(final int opCode) {
    findCoveredLineEdges();
    collectLines(opCode);
    // labelIsolatedLines(lineEdgesList);
    buildLines(opCode);
    return this.resultLineList;
  }

  private void buildLines(final int opCode) {
    for (final Edge e : this.lineEdgesList) {
      final LineString line = this.geometryFactory.lineString(e.getLineString());
      this.resultLineList.add(line);
      e.setInResult(true);
    }
  }

  /**
   * Collect edges from Area inputs which should be in the result but
   * which have not been included in a result area.
   * This happens ONLY:
   * <ul>
   * <li>during an intersection when the boundaries of two
   * areas touch in a line segment
   * <li> OR as a result of a dimensional collapse.
   * </ul>
   */
  private void collectBoundaryTouchEdge(final DirectedEdge de, final int opCode,
    final List<Edge> edges) {
    final Label label = de.getLabel();
    if (de.isLineEdge()) {
      return; // only interested in area edges
    }
    if (de.isVisited()) {
      return; // already processed
    }
    if (de.isInteriorAreaEdge()) {
      return; // added to handle dimensional collapses
    }
    if (de.getEdge().isInResult()) {
      return; // if the edge linework is already included, don't include it
      // again
    }

    // sanity check for labelling of result edgerings
    if (!(!(de.isInResult() || de.getSym().isInResult()) || !de.getEdge().isInResult())) {
      throw new IllegalStateException();
    }

    // include the linework if it's in the result of the operation
    if (OverlayOp.isResultOfOp(label, opCode) && opCode == OverlayOp.INTERSECTION) {
      edges.add(de.getEdge());
      de.setVisitedEdge(true);
    }
  }

  /**
   * Collect line edges which are in the result.
   * Line edges are in the result if they are not part of
   * an area boundary, if they are in the result of the overlay operation,
   * and if they are not covered by a result area.
   *
   * @param de the directed edge to test
   * @param opCode the overlap operation
   * @param edges the list of included line edges
   */
  private void collectLineEdge(final DirectedEdge de, final int opCode, final List<Edge> edges) {
    final Label label = de.getLabel();
    final Edge e = de.getEdge();
    // include L edges which are in the result
    if (de.isLineEdge()) {
      if (!de.isVisited() && OverlayOp.isResultOfOp(label, opCode) && !e.isCovered()) {
        // Debug.println("de: " + de.getLabel());
        // Debug.println("edge: " + e.getLabel());

        edges.add(e);
        de.setVisitedEdge(true);
      }
    }
  }

  private void collectLines(final int opCode) {
    for (final Object element : this.op.getGraph().getEdgeEnds()) {
      final DirectedEdge de = (DirectedEdge)element;
      collectLineEdge(de, opCode, this.lineEdgesList);
      collectBoundaryTouchEdge(de, opCode, this.lineEdgesList);
    }
  }

  /**
   * Find and mark L edges which are "covered" by the result area (if any).
   * L edges at nodes which also have A edges can be checked by checking
   * their depth at that node.
   * L edges at nodes which do not have A edges can be checked by doing a
   * point-in-polygon test with the previously computed result areas.
   */
  private void findCoveredLineEdges() {
    // first set covered for all L edges at nodes which have A edges too
    for (final Object element : this.op.getGraph().getNodes()) {
      final Node node = (Node)element;
      // node.print(System.out);
      ((DirectedEdgeStar)node.getEdges()).findCoveredLineEdges();
    }

    /**
     * For all L edges which weren't handled by the above,
     * use a point-in-poly test to determine whether they are covered
     */
    for (final Object element : this.op.getGraph().getEdgeEnds()) {
      final DirectedEdge de = (DirectedEdge)element;
      final Edge e = de.getEdge();
      if (de.isLineEdge() && !e.isCoveredSet()) {
        final boolean isCovered = this.op.isCoveredByA(de.getX1(), de.getY1());
        e.setCovered(isCovered);
      }
    }
  }

}
