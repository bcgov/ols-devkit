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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.jeometry.common.function.BiConsumerDouble;

import com.revolsys.geometry.geomgraph.DirectedEdge;
import com.revolsys.geometry.geomgraph.DirectedEdgeStar;
import com.revolsys.geometry.geomgraph.Edge;
import com.revolsys.geometry.geomgraph.Node;
import com.revolsys.geometry.geomgraph.Position;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.TopologyException;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;

//import debug.*;

/**
 * A connected subset of the graph of
 * {@link DirectedEdge}s and {@link Node}s.
 * Its edges will generate either
 * <ul>
 * <li> a single polygon in the complete buffer, with zero or more holes, or
 * <li> one or more connected holes
 * </ul>
 *
 *
 * @version 1.7
 */
class BufferSubgraph extends BoundingBoxDoubleXY implements Comparable {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final List<DirectedEdge> dirEdgeList = new ArrayList<>();

  private final RightmostEdgeFinder finder;

  private final List<Node> nodes = new ArrayList<>();

  private Point rightMostCoord = null;

  public BufferSubgraph() {
    this.finder = new RightmostEdgeFinder();
  }

  /**
   * Adds the argument node and all its out edges to the subgraph
   * @param node the node to add
   * @param nodeStack the current set of nodes being traversed
   */
  private void add(final Node node, final Stack<Node> nodeStack) {
    node.setVisited(true);
    this.nodes.add(node);
    for (final Object element : (DirectedEdgeStar)node.getEdges()) {
      final DirectedEdge de = (DirectedEdge)element;
      this.dirEdgeList.add(de);
      final DirectedEdge sym = de.getSym();
      final Node symNode = sym.getNode();
      /**
       * NOTE: this is a depth-first traversal of the graph.
       * This will cause a large depth of recursion.
       * It might be better to do a breadth-first traversal.
       */
      if (!symNode.isVisited()) {
        nodeStack.push(symNode);
      }
    }
  }

  /**
   * Adds all nodes and edges reachable from this node to the subgraph.
   * Uses an explicit stack to avoid a large depth of recursion.
   *
   * @param node a node known to be in the subgraph
   */
  private void addReachable(final Node startNode) {
    final Stack<Node> nodeStack = new Stack<>();
    nodeStack.add(startNode);
    while (!nodeStack.empty()) {
      final Node node = nodeStack.pop();
      add(node, nodeStack);
    }
  }

  private void clearVisitedEdges() {
    for (final DirectedEdge de : this.dirEdgeList) {
      de.setVisited(false);
    }
  }

  /**
   * BufferSubgraphs are compared on the x-value of their rightmost Coordinate.
   * This defines a partial ordering on the graphs such that:
   * <p>
   * g1 >= g2 <==> Ring(g2) does not contain Ring(g1)
   * <p>
   * where Polygon(g) is the buffer polygon that is built from g.
   * <p>
   * This relationship is used to sort the BufferSubgraphs so that shells are guaranteed to
   * be built before holes.
   */
  @Override
  public int compareTo(final Object o) {
    final BufferSubgraph graph = (BufferSubgraph)o;
    if (this.rightMostCoord.getX() < graph.rightMostCoord.getX()) {
      return -1;
    }
    if (this.rightMostCoord.getX() > graph.rightMostCoord.getX()) {
      return 1;
    }
    return 0;
  }

  public void computeDepth(final int outsideDepth) {
    clearVisitedEdges();
    // find an outside edge to assign depth to
    final DirectedEdge de = this.finder.getEdge();
    // right side of line returned by finder is on the outside
    de.setEdgeDepths(Position.RIGHT, outsideDepth);
    copySymDepths(de);

    // computeNodeDepth(n, de);
    computeDepths(de);
  }

  /**
   * Compute depths for all dirEdges via breadth-first traversal of nodes in graph
   * @param startEdge edge to start processing with
   */
  // <FIX> MD - use iteration & queue rather than recursion, for speed and
  // robustness
  private void computeDepths(final DirectedEdge startEdge) {
    final Set<Node> nodesVisited = new HashSet<>();
    final LinkedList<Node> nodeQueue = new LinkedList<>();

    final Node startNode = startEdge.getNode();
    nodeQueue.addLast(startNode);
    nodesVisited.add(startNode);
    startEdge.setVisited(true);

    while (!nodeQueue.isEmpty()) {
      // System.out.println(nodes.size() + " queue: " + nodeQueue.size());
      final Node n = nodeQueue.removeFirst();
      nodesVisited.add(n);
      // compute depths around node, starting at this edge since it has depths
      // assigned
      computeNodeDepth(n);

      // add all adjacent nodes to process queue,
      // unless the node has been visited already
      for (final Object element : (DirectedEdgeStar)n.getEdges()) {
        final DirectedEdge de = (DirectedEdge)element;
        final DirectedEdge sym = de.getSym();
        if (sym.isVisited()) {
          continue;
        }
        final Node adjNode = sym.getNode();
        if (!nodesVisited.contains(adjNode)) {
          nodeQueue.addLast(adjNode);
          nodesVisited.add(adjNode);
        }
      }
    }
  }

  private void computeNodeDepth(final Node n) {
    // find a visited dirEdge to start at
    DirectedEdge startEdge = null;
    for (final Object element : (DirectedEdgeStar)n.getEdges()) {
      final DirectedEdge de = (DirectedEdge)element;
      if (de.isVisited() || de.getSym().isVisited()) {
        startEdge = de;
        break;
      }
    }
    // MD - testing Result: breaks algorithm
    // if (startEdge == null) return;

    // only compute string append if assertion would fail
    if (startEdge == null) {
      throw new TopologyException(
        "unable to find edge to compute depths at POINT(" + n.getX() + " " + n.getY() + ")");
    }

    ((DirectedEdgeStar)n.getEdges()).computeDepths(startEdge);

    // copy depths to sym edges
    for (final Object element : (DirectedEdgeStar)n.getEdges()) {
      final DirectedEdge de = (DirectedEdge)element;
      de.setVisited(true);
      copySymDepths(de);
    }
  }

  private void copySymDepths(final DirectedEdge de) {
    final DirectedEdge sym = de.getSym();
    sym.setDepth(Position.LEFT, de.getDepth(Position.RIGHT));
    sym.setDepth(Position.RIGHT, de.getDepth(Position.LEFT));
  }

  /**
   * Find all edges whose depths indicates that they are in the result area(s).
   * Since we want polygon shells to be
   * oriented CW, choose dirEdges with the interior of the result on the RHS.
   * Mark them as being in the result.
   * Interior Area edges are the result of dimensional collapses.
   * They do not form part of the result area boundary.
   */
  public void findResultEdges() {
    for (final DirectedEdge de : this.dirEdgeList) {
      /**
       * Select edges which have an interior depth on the RHS
       * and an exterior depth on the LHS.
       * Note that because of weird rounding effects there may be
       * edges which have negative depths!  Negative depths
       * count as "outside".
       */
      // <FIX> - handle negative depths
      final int depthRight = de.getDepth(Position.RIGHT);
      if (depthRight >= 1) {
        final int depthLeft = de.getDepth(Position.LEFT);
        if (depthLeft <= 0) {
          final boolean interiorAreaEdge = de.isInteriorAreaEdge();
          if (!interiorAreaEdge) {
            de.setInResult(true);
          }
        }
      }
    }
  }

  /**
   * Computes the envelope of the edges in the subgraph.
   * The envelope is cached after being computed.
   *
   * @return the envelope of the graph.
   */
  @Override
  public BoundingBox getBoundingBox() {
    if (isEmpty()) {
      for (final DirectedEdge dirEdge : this.dirEdgeList) {
        final Edge edge = dirEdge.getEdge();
        final LineString points = edge.getLineString();
        final BiConsumerDouble action = this::expandBbox;
        points.forEachVertex(action);
      }
    }
    return this;
  }

  public List<DirectedEdge> getDirectedEdges() {
    return this.dirEdgeList;
  }

  public List<Node> getNodes() {
    return this.nodes;
  }

  /**
   * Gets the rightmost coordinate in the edges of the subgraph
   */
  public Point getRightmostCoordinate() {
    return this.rightMostCoord;
  }

  /**
   * Creates the subgraph consisting of all edges reachable from this node.
   * Finds the edges in the graph and the rightmost coordinate.
   *
   * @param node a node to start the graph traversal from
   */
  public void newNode(final Node node) {
    addReachable(node);
    this.finder.findEdge(this.dirEdgeList);
    this.rightMostCoord = this.finder.getCoordinate();
  }
}
