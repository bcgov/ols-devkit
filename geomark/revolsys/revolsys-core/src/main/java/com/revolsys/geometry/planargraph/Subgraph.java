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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A subgraph of a {@link PlanarGraph}.
 * A subgraph may contain any subset of {@link Edge}s
 * from the parent graph.
 * It will also automatically contain all {@link DirectedEdge}s
 * and {@link Node}s associated with those edges.
 * No new objects are created when edges are added -
 * all associated components must already exist in the parent graph.
 */
public class Subgraph {
  protected List dirEdges = new ArrayList();

  protected Set edges = new HashSet();

  protected NodeMap nodeMap = new NodeMap();

  protected PlanarGraph parentGraph;

  /**
   * Creates a new subgraph of the given {@link PlanarGraph}
   *
   * @param parentGraph the parent graph
   */
  public Subgraph(final PlanarGraph parentGraph) {
    this.parentGraph = parentGraph;
  }

  /**
   * Adds an {@link Edge} to the subgraph.
   * The associated {@link DirectedEdge}s and {@link Node}s
   * are also added.
   *
   * @param e the edge to add
   */
  public void add(final Edge e) {
    if (this.edges.contains(e)) {
      return;
    }

    this.edges.add(e);
    this.dirEdges.add(e.getDirEdge(0));
    this.dirEdges.add(e.getDirEdge(1));
    this.nodeMap.add(e.getDirEdge(0).getFromNode());
    this.nodeMap.add(e.getDirEdge(1).getFromNode());
  }

  /**
   * Tests whether an {@link Edge} is contained in this subgraph
   * @param e the edge to test
   * @return <code>true</code> if the edge is contained in this subgraph
   */
  public boolean contains(final Edge e) {
    return this.edges.contains(e);
  }

  /**
   * Returns an {@link Iterator} over the {@link DirectedEdge}s in this graph,
   * in the order in which they were added.
   *
   * @return an iterator over the directed edges
   *
   * @see #add(Edge)
   */
  public Iterator dirEdgeIterator() {
    return this.dirEdges.iterator();
  }

  /**
   * Returns an {@link Iterator} over the {@link Edge}s in this graph,
   * in the order in which they were added.
   *
   * @return an iterator over the edges
   *
   * @see #add(Edge)
   */
  public Iterator edgeIterator() {
    return this.edges.iterator();
  }

  /**
   * Gets the {@link PlanarGraph} which this subgraph
   * is part of.
   *
   * @return the parent PlanarGraph
   */
  public PlanarGraph getParent() {
    return this.parentGraph;
  }

  /**
   * Returns an {@link Iterator} over the {@link Node}s in this graph.
   * @return an iterator over the nodes
   */
  public Iterator nodeIterator() {
    return this.nodeMap.iterator();
  }

}
