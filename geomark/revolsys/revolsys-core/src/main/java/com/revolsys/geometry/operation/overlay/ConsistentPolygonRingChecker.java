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
import java.util.Iterator;
import java.util.List;

import com.revolsys.geometry.geomgraph.DirectedEdge;
import com.revolsys.geometry.geomgraph.DirectedEdgeStar;
import com.revolsys.geometry.geomgraph.GeometryGraph;
import com.revolsys.geometry.geomgraph.Label;
import com.revolsys.geometry.geomgraph.Node;
import com.revolsys.geometry.geomgraph.PlanarGraph;
import com.revolsys.geometry.geomgraph.Position;
import com.revolsys.geometry.model.TopologyException;

/**
 * Tests whether the polygon rings in a {@link GeometryGraph}
 * are consistent.
 * Used for checking if Topology errors are present after noding.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class ConsistentPolygonRingChecker {
  private final PlanarGraph graph;

  private final int LINKING_TO_OUTGOING = 2;

  private final int SCANNING_FOR_INCOMING = 1;

  public ConsistentPolygonRingChecker(final PlanarGraph graph) {
    this.graph = graph;
  }

  /**
   * Tests whether the result geometry is consistent
   *
   * @throws TopologyException if inconsistent topology is found
   */
  public void check(final int opCode) {
    for (final Iterator nodeit = this.graph.getNodeIterator(); nodeit.hasNext();) {
      final Node node = (Node)nodeit.next();
      testLinkResultDirectedEdges((DirectedEdgeStar)node.getEdges(), opCode);
    }
  }

  public void checkAll() {
    check(OverlayOp.INTERSECTION);
    check(OverlayOp.DIFFERENCE);
    check(OverlayOp.UNION);
    check(OverlayOp.SYMDIFFERENCE);
  }

  private List getPotentialResultAreaEdges(final DirectedEdgeStar deStar, final int opCode) {
    // print(System.out);
    final List resultAreaEdgeList = new ArrayList();
    for (final Object element : deStar) {
      final DirectedEdge de = (DirectedEdge)element;
      if (isPotentialResultAreaEdge(de, opCode) || isPotentialResultAreaEdge(de.getSym(), opCode)) {
        resultAreaEdgeList.add(de);
      }
    }
    return resultAreaEdgeList;
  }

  private boolean isPotentialResultAreaEdge(final DirectedEdge de, final int opCode) {
    // mark all dirEdges with the appropriate label
    final Label label = de.getLabel();
    if (label.isArea() && !de.isInteriorAreaEdge() && OverlayOp.isResultOfOp(
      label.getLocation(0, Position.RIGHT), label.getLocation(1, Position.RIGHT), opCode)) {
      return true;
      // Debug.print("in result "); Debug.println(de);
    }
    return false;
  }

  private void testLinkResultDirectedEdges(final DirectedEdgeStar deStar, final int opCode) {
    // make sure edges are copied to resultAreaEdges list
    final List ringEdges = getPotentialResultAreaEdges(deStar, opCode);
    // find first area edge (if any) to start linking at
    DirectedEdge firstOut = null;
    DirectedEdge incoming = null;
    int state = this.SCANNING_FOR_INCOMING;
    // link edges in CCW order
    for (final Object ringEdge : ringEdges) {
      final DirectedEdge nextOut = (DirectedEdge)ringEdge;
      final DirectedEdge nextIn = nextOut.getSym();

      // skip de's that we're not interested in
      if (!nextOut.getLabel().isArea()) {
        continue;
      }

      // record first outgoing edge, in order to link the last incoming edge
      if (firstOut == null && isPotentialResultAreaEdge(nextOut, opCode)) {
        firstOut = nextOut;
        // assert: sym.isInResult() == false, since pairs of dirEdges should
        // have been removed already
      }

      switch (state) {
        case SCANNING_FOR_INCOMING:
          if (!isPotentialResultAreaEdge(nextIn, opCode)) {
            continue;
          }
          incoming = nextIn;
          state = this.LINKING_TO_OUTGOING;
        break;
        case LINKING_TO_OUTGOING:
          if (!isPotentialResultAreaEdge(nextOut, opCode)) {
            continue;
          }
          // incoming.setNext(nextOut);
          state = this.SCANNING_FOR_INCOMING;
        break;
      }
    }
    // Debug.print(this);
    if (state == this.LINKING_TO_OUTGOING) {
      // Debug.print(firstOut == null, this);
      if (firstOut == null) {
        throw new TopologyException("no outgoing dirEdge found", deStar.getCoordinate());
      }
    }

  }

}
