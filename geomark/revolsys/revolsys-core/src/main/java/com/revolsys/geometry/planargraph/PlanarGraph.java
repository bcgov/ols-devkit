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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.revolsys.geometry.model.Point;

/**
 * Represents a directed graph which is embeddable in a planar surface.
 * <p>
 * This class and the other classes in this package serve as a framework for
 * building planar graphs for specific algorithms. This class must be
 * subclassed to expose appropriate methods to construct the graph. This allows
 * controlling the types of graph components ({@link DirectedEdge}s,
 * {@link Edge}s and {@link Node}s) which can be added to the graph. An
 * application which uses the graph framework will almost always provide
 * subclasses for one or more graph components, which hold application-specific
 * data and graph algorithms.
 *
 * @version 1.7
 */
public abstract class PlanarGraph {
  protected Set<DirectedEdge> dirEdges = new HashSet<>();

  protected Set<Edge> edges = new HashSet<>();

  protected NodeMap nodeMap = new NodeMap();

  /**
   * Constructs a empty graph.
   */
  public PlanarGraph() {
  }

  /**
   * Adds the Edge to this PlanarGraph; only subclasses can add DirectedEdges,
   * to ensure the edges added are of the right class.
   */
  protected void add(final DirectedEdge dirEdge) {
    this.dirEdges.add(dirEdge);
  }

  /**
   * Adds the Edge and its DirectedEdges with this PlanarGraph.
   * Assumes that the Edge has already been created with its associated DirectEdges.
   * Only subclasses can add Edges, to ensure the edges added are of the right class.
   */
  protected void add(final Edge edge) {
    this.edges.add(edge);
    add(edge.getDirEdge(0));
    add(edge.getDirEdge(1));
  }

  /**
   * Adds a node to the map, replacing any that is already at that location.
   * Only subclasses can add Nodes, to ensure Nodes are of the right type.
   *
   * @param node the node to add
   */
  protected void add(final Node node) {
    this.nodeMap.add(node);
  }

  /**
   * Tests whether this graph contains the given {@link DirectedEdge}
   *
   * @param de the directed edge to query
   * @return <code>true</code> if the graph contains the directed edge
   */
  public boolean contains(final DirectedEdge de) {
    return this.dirEdges.contains(de);
  }

  /**
   * Tests whether this graph contains the given {@link Edge}
   *
   * @param e the edge to query
   * @return <code>true</code> if the graph contains the edge
   */
  public boolean contains(final Edge e) {
    return this.edges.contains(e);
  }

  /**
   * Returns an Iterator over the DirectedEdges in this PlanarGraph, in the order in which they
   * were added.
   *
   * @see #add(Edge)
   * @see #add(DirectedEdge)
   */
  public Iterator<DirectedEdge> dirEdgeIterator() {
    return this.dirEdges.iterator();
  }

  /**
   * Returns an Iterator over the Edges in this PlanarGraph, in the order in which they
   * were added.
   *
   * @see #add(Edge)
   */
  public Iterator<Edge> edgeIterator() {
    return this.edges.iterator();
  }

  /**
   * Returns the {@link Node} at the given location,
   * or null if no {@link Node} was there.
   *
   * @param pt the location to query
   * @return the node found
   * or <code>null</code> if this graph contains no node at the location
   */
  public Node findNode(final Point pt) {
    return this.nodeMap.find(pt);
  }

  /**
   * Returns all Nodes with the given number of Edges around it.
   */
  public List<Node> findNodesOfDegree(final int degree) {
    final List<Node> nodesFound = new ArrayList<>();
    for (final Node node : this.nodeMap.nodes()) {
      if (node.getDegree() == degree) {
        nodesFound.add(node);
      }
    }
    return nodesFound;
  }

  /**
   * Returns the Edges that have been added to this PlanarGraph
   * @see #add(Edge)
   */
  public Collection<Edge> getEdges() {
    return this.edges;
  }

  public Collection<Node> getNodes() {
    return this.nodeMap.nodes();
  }

  /**
   * Returns an Iterator over the Nodes in this PlanarGraph.
   */
  public Iterator<Node> nodeIterator() {
    return this.nodeMap.iterator();
  }

  /**
   * Returns the Nodes in this PlanarGraph.
   */

  /**
   * Removes a {@link DirectedEdge} from its from-{@link Node} and from this graph.
   * This method does not remove the {@link Node}s associated with the DirectedEdge,
   * even if the removal of the DirectedEdge reduces the degree of a Node to zero.
   */
  public void remove(final DirectedEdge de) {
    final DirectedEdge sym = de.getSym();
    if (sym != null) {
      sym.setSym(null);
    }

    de.getFromNode().remove(de);
    de.remove();
    this.dirEdges.remove(de);
  }

  /**
   * Removes an {@link Edge} and its associated {@link DirectedEdge}s
   * from their from-Nodes and from the graph.
   * Note: This method does not remove the {@link Node}s associated
   * with the {@link Edge}, even if the removal of the {@link Edge}
   * reduces the degree of a {@link Node} to zero.
   */
  public void remove(final Edge edge) {
    remove(edge.getDirEdge(0));
    remove(edge.getDirEdge(1));
    this.edges.remove(edge);
    edge.remove();
  }

  /**
   * Removes a node from the graph, along with any associated DirectedEdges and
   * Edges.
   */
  public void remove(final Node node) {
    // unhook all directed edges
    final List<DirectedEdge> outEdges = node.getOutEdges().getEdges();
    for (final DirectedEdge de : outEdges) {
      final DirectedEdge sym = de.getSym();
      // remove the diredge that points to this node
      if (sym != null) {
        remove(sym);
      }
      // remove this diredge from the graph collection
      this.dirEdges.remove(de);

      final Edge edge = de.getEdge();
      if (edge != null) {
        this.edges.remove(edge);
      }

    }
    // remove the node from the graph
    this.nodeMap.remove(node);
    node.remove();
  }

}
