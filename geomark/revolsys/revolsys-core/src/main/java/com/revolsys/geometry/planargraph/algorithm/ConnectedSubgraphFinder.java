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

package com.revolsys.geometry.planargraph.algorithm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.revolsys.geometry.planargraph.DirectedEdge;
import com.revolsys.geometry.planargraph.Edge;
import com.revolsys.geometry.planargraph.GraphComponent;
import com.revolsys.geometry.planargraph.Node;
import com.revolsys.geometry.planargraph.PlanarGraph;
import com.revolsys.geometry.planargraph.Subgraph;

/**
 * Finds all connected {@link Subgraph}s of a {@link PlanarGraph}.
 * <p>
 * <b>Note:</b> uses the <code>isVisited</code> flag on the nodes.
 */
public class ConnectedSubgraphFinder {

  private final PlanarGraph graph;

  public ConnectedSubgraphFinder(final PlanarGraph graph) {
    this.graph = graph;
  }

  /**
   * Adds the argument node and all its out edges to the subgraph.
   * @param node the node to add
   * @param nodeStack the current set of nodes being traversed
   */
  private void addEdges(final Node node, final Stack nodeStack, final Subgraph subgraph) {
    node.setVisited(true);
    for (final Object element : node.getOutEdges()) {
      final DirectedEdge de = (DirectedEdge)element;
      subgraph.add(de.getEdge());
      final Node toNode = de.getToNode();
      if (!toNode.isVisited()) {
        nodeStack.push(toNode);
      }
    }
  }

  /**
   * Adds all nodes and edges reachable from this node to the subgraph.
   * Uses an explicit stack to avoid a large depth of recursion.
   *
   * @param node a node known to be in the subgraph
   */
  private void addReachable(final Node startNode, final Subgraph subgraph) {
    final Stack nodeStack = new Stack();
    nodeStack.add(startNode);
    while (!nodeStack.empty()) {
      final Node node = (Node)nodeStack.pop();
      addEdges(node, nodeStack, subgraph);
    }
  }

  private Subgraph findSubgraph(final Node node) {
    final Subgraph subgraph = new Subgraph(this.graph);
    addReachable(node, subgraph);
    return subgraph;
  }

  public List getConnectedSubgraphs() {
    final List subgraphs = new ArrayList();

    GraphComponent.setVisited(this.graph.nodeIterator(), false);
    for (final Iterator i = this.graph.edgeIterator(); i.hasNext();) {
      final Edge e = (Edge)i.next();
      final Node node = e.getDirEdge(0).getFromNode();
      if (!node.isVisited()) {
        subgraphs.add(findSubgraph(node));
      }
    }
    return subgraphs;
  }

}
