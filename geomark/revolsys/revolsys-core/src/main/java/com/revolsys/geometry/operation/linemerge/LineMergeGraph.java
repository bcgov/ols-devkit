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
package com.revolsys.geometry.operation.linemerge;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.planargraph.DirectedEdge;
import com.revolsys.geometry.planargraph.Edge;
import com.revolsys.geometry.planargraph.Node;
import com.revolsys.geometry.planargraph.PlanarGraph;

/**
 * A planar graph of edges that is analyzed to sew the edges together. The
 * <code>marked</code> flag on @{link com.vividsolutions.planargraph.Edge}s
 * and @{link com.vividsolutions.planargraph.Node}s indicates whether they have been
 * logically deleted from the graph.
 *
 * @version 1.7
 */
public class LineMergeGraph extends PlanarGraph {
  /**
   * Adds an Edge, DirectedEdges, and Nodes for the given LineString representation
   * of an edge.
   * Empty lines or lines with all coordinates equal are not added.
   *
   * @param line the linestring to add to the graph
   */
  public void addEdge(final LineString line) {
    if (line.isEmpty()) {
      return;
    }

    final LineString points = line.removeDuplicatePoints();
    final int vertexCount = points.getVertexCount();
    if (vertexCount > 1) {
      final Point fromPoint = points.getPoint(0);
      final Point toPoint = points.getPoint(vertexCount - 1);
      final Node startNode = getNode(fromPoint);
      final Node endNode = getNode(toPoint);
      final DirectedEdge directedEdge0 = new LineMergeDirectedEdge(startNode, endNode,
        points.getPoint(1).newPoint(), true);
      final DirectedEdge directedEdge1 = new LineMergeDirectedEdge(endNode, startNode,
        points.getPoint(vertexCount - 2).newPoint(), false);
      final Edge edge = new LineMergeEdge(line);
      edge.setDirectedEdges(directedEdge0, directedEdge1);
      add(edge);
    }
  }

  private Node getNode(final Point point) {
    Node node = findNode(point);
    if (node == null) {
      final double x = point.getX();
      final double y = point.getY();
      node = new Node(x, y);
      add(node);
    }

    return node;
  }

  public void removeEdge(final LineString line) {
    final Point point = line.getFromPoint();
    final Node node = findNode(point);
    if (node != null) {
      for (final DirectedEdge outEdge : Lists.toArray(node.getOutEdges())) {
        final LineMergeEdge edge = (LineMergeEdge)outEdge.getEdge();
        if (line.equals(edge.getLine())) {
          remove(edge);
        }
      }
    }
  }
}
