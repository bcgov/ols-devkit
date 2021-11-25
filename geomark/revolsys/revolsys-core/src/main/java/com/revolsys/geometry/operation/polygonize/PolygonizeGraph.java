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

package com.revolsys.geometry.operation.polygonize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.planargraph.DirectedEdge;
import com.revolsys.geometry.planargraph.DirectedEdgeStar;
import com.revolsys.geometry.planargraph.Edge;
import com.revolsys.geometry.planargraph.Node;
import com.revolsys.geometry.planargraph.PlanarGraph;
import com.revolsys.geometry.util.Assert;

/**
 * Represents a planar graph of edges that can be used to compute a
 * polygonization, and implements the algorithms to compute the
 * {@link EdgeRings} formed by the graph.
 * <p>
 * The marked flag on {@link DirectedEdge}s is used to indicate that a directed edge
 * has be logically deleted from the graph.
 *
 * @version 1.7
 */
class PolygonizeGraph extends PlanarGraph {

  /**
   * Computes the next edge pointers going CCW around the given node, for the
   * given edgering label.
   * This algorithm has the effect of converting maximal edgerings into minimal edgerings
   */
  private static void computeNextCCWEdges(final Node node, final long label) {
    final DirectedEdgeStar deStar = node.getOutEdges();
    // PolyDirectedEdge lastInDE = null;
    PolygonizeDirectedEdge firstOutDE = null;
    PolygonizeDirectedEdge prevInDE = null;

    // the edges are stored in CCW order around the star
    final List edges = deStar.getEdges();
    // for (Iterator i = deStar.getEdges().iterator(); i.hasNext(); ) {
    for (int i = edges.size() - 1; i >= 0; i--) {
      final PolygonizeDirectedEdge de = (PolygonizeDirectedEdge)edges.get(i);
      final PolygonizeDirectedEdge sym = (PolygonizeDirectedEdge)de.getSym();

      PolygonizeDirectedEdge outDE = null;
      if (de.getLabel() == label) {
        outDE = de;
      }
      PolygonizeDirectedEdge inDE = null;
      if (sym.getLabel() == label) {
        inDE = sym;
      }

      if (outDE == null && inDE == null) {
        continue; // this edge is not in edgering
      }

      if (inDE != null) {
        prevInDE = inDE;
      }

      if (outDE != null) {
        if (prevInDE != null) {
          prevInDE.setNext(outDE);
          prevInDE = null;
        }
        if (firstOutDE == null) {
          firstOutDE = outDE;
        }
      }
    }
    if (prevInDE != null) {
      Assert.isTrue(firstOutDE != null);
      prevInDE.setNext(firstOutDE);
    }
  }

  private static void computeNextCWEdges(final Node node) {
    final DirectedEdgeStar deStar = node.getOutEdges();
    PolygonizeDirectedEdge startDE = null;
    PolygonizeDirectedEdge prevDE = null;

    // the edges are stored in CCW order around the star
    for (final Object element : deStar.getEdges()) {
      final PolygonizeDirectedEdge outDE = (PolygonizeDirectedEdge)element;
      if (outDE.isMarked()) {
        continue;
      }

      if (startDE == null) {
        startDE = outDE;
      }
      if (prevDE != null) {
        final PolygonizeDirectedEdge sym = (PolygonizeDirectedEdge)prevDE.getSym();
        sym.setNext(outDE);
      }
      prevDE = outDE;
    }
    if (prevDE != null) {
      final PolygonizeDirectedEdge sym = (PolygonizeDirectedEdge)prevDE.getSym();
      sym.setNext(startDE);
    }
  }

  /**
   * Deletes all edges at a node
   */
  public static void deleteAllEdges(final Node node) {
    final List edges = node.getOutEdges().getEdges();
    for (final Object edge : edges) {
      final PolygonizeDirectedEdge de = (PolygonizeDirectedEdge)edge;
      de.setMarked(true);
      final PolygonizeDirectedEdge sym = (PolygonizeDirectedEdge)de.getSym();
      if (sym != null) {
        sym.setMarked(true);
      }
    }
  }

  /**
   * Traverses a ring of DirectedEdges, accumulating them into a list.
   * This assumes that all dangling directed edges have been removed
   * from the graph, so that there is always a next dirEdge.
   *
   * @param startDE the DirectedEdge to start traversing at
   * @return a List of DirectedEdges that form a ring
   */
  private static List findDirEdgesInRing(final PolygonizeDirectedEdge startDE) {
    PolygonizeDirectedEdge de = startDE;
    final List edges = new ArrayList();
    do {
      edges.add(de);
      de = de.getNext();
      Assert.isTrue(de != null, "found null DE in ring");
      Assert.isTrue(de == startDE || !de.isInRing(), "found DE already in ring");
    } while (de != startDE);

    return edges;
  }

  // private List labelledRings;

  /**
   * Finds all nodes in a maximal edgering which are self-intersection nodes
   * @param startDE
   * @param label
   * @return the list of intersection nodes found,
   * or <code>null</code> if no intersection nodes were found
   */
  private static List findIntersectionNodes(final PolygonizeDirectedEdge startDE,
    final long label) {
    PolygonizeDirectedEdge de = startDE;
    List intNodes = null;
    do {
      final Node node = de.getFromNode();
      if (getDegree(node, label) > 1) {
        if (intNodes == null) {
          intNodes = new ArrayList();
        }
        intNodes.add(node);
      }

      de = de.getNext();
      Assert.isTrue(de != null, "found null DE in ring");
      Assert.isTrue(de == startDE || !de.isInRing(), "found DE already in ring");
    } while (de != startDE);

    return intNodes;
  }

  /**
   * Finds and labels all edgerings in the graph.
   * The edge rings are labelling with unique integers.
   * The labelling allows detecting cut edges.
   *
   * @param dirEdges a List of the DirectedEdges in the graph
   * @return a List of DirectedEdges, one for each edge ring found
   */
  private static List findLabeledEdgeRings(final Collection dirEdges) {
    final List edgeRingStarts = new ArrayList();
    // label the edge rings formed
    long currLabel = 1;
    for (final Object dirEdge : dirEdges) {
      final PolygonizeDirectedEdge de = (PolygonizeDirectedEdge)dirEdge;
      if (de.isMarked()) {
        continue;
      }
      if (de.getLabel() >= 0) {
        continue;
      }

      edgeRingStarts.add(de);
      final List edges = findDirEdgesInRing(de);

      label(edges, currLabel);
      currLabel++;
    }
    return edgeRingStarts;
  }

  private static int getDegree(final Node node, final long label) {
    final List edges = node.getOutEdges().getEdges();
    int degree = 0;
    for (final Object edge : edges) {
      final PolygonizeDirectedEdge de = (PolygonizeDirectedEdge)edge;
      if (de.getLabel() == label) {
        degree++;
      }
    }
    return degree;
  }

  private static int getDegreeNonDeleted(final Node node) {
    final List edges = node.getOutEdges().getEdges();
    int degree = 0;
    for (final Object edge : edges) {
      final PolygonizeDirectedEdge de = (PolygonizeDirectedEdge)edge;
      if (!de.isMarked()) {
        degree++;
      }
    }
    return degree;
  }

  private static void label(final Collection dirEdges, final long label) {
    for (final Object dirEdge : dirEdges) {
      final PolygonizeDirectedEdge de = (PolygonizeDirectedEdge)dirEdge;
      de.setLabel(label);
    }
  }

  private final GeometryFactory factory;

  /**
   * Construct a new new polygonization graph.
   */
  public PolygonizeGraph(final GeometryFactory factory) {
    this.factory = factory;
  }

  /**
   * Add a {@link LineString} forming an edge of the polygon graph.
   * @param line the line to add
   */
  public void addEdge(final LineString line) {
    final LineString cleanLine = line.removeDuplicatePoints();
    if (cleanLine.isEmpty()) {
      return;
    }

    if (cleanLine.getVertexCount() < 2) {
      return;
    }

    final Point startPt = cleanLine.getFromPoint().newPoint2D();
    final Point endPt = cleanLine.getToPoint().newPoint2D();

    final Node nStart = getNode(startPt);
    final Node nEnd = getNode(endPt);

    final DirectedEdge de0 = new PolygonizeDirectedEdge(nStart, nEnd,
      cleanLine.getVertex(1).newPoint2D(), true);
    final DirectedEdge de1 = new PolygonizeDirectedEdge(nEnd, nStart,
      cleanLine.getToVertex(1).newPoint2D(), false);
    final Edge edge = new PolygonizeEdge(line);
    edge.setDirectedEdges(de0, de1);
    add(edge);
  }

  /**
   * Traverses the polygonized edge rings in the graph
   * and computes the depth parity (odd or even)
   * relative to the exterior of the graph.
   * If the client has requested that the output
   * be polygonally valid, only odd polygons will be constructed.
   *
   */
  public void computeDepthParity() {
    while (true) {
      final PolygonizeDirectedEdge de = null; // findLowestDirEdge();
      if (de == null) {
        return;
      }
      computeDepthParity(de);
    }
  }

  /**
   * Traverses all connected edges, computing the depth parity
   * of the associated polygons.
   *
   * @param de
   */
  private void computeDepthParity(final PolygonizeDirectedEdge de) {

  }

  private void computeNextCWEdges() {
    // set the next pointers for the edges around each node
    for (final Iterator iNode = nodeIterator(); iNode.hasNext();) {
      final Node node = (Node)iNode.next();
      computeNextCWEdges(node);
    }
  }

  /**
   * Convert the maximal edge rings found by the initial graph traversal
   * into the minimal edge rings required by JTS polygon topology rules.
   *
   * @param ringEdges the list of start edges for the edgeRings to convert.
   */
  private void convertMaximalToMinimalEdgeRings(final List ringEdges) {
    for (final Object ringEdge : ringEdges) {
      final PolygonizeDirectedEdge de = (PolygonizeDirectedEdge)ringEdge;
      final long label = de.getLabel();
      final List intNodes = findIntersectionNodes(de, label);

      if (intNodes == null) {
        continue;
      }
      // flip the next pointers on the intersection nodes to create minimal edge
      // rings
      for (final Object intNode : intNodes) {
        final Node node = (Node)intNode;
        computeNextCCWEdges(node, label);
      }
    }
  }

  /**
   * Finds and removes all cut edges from the graph.
   * @return a list of the {@link LineString}s forming the removed cut edges
   */
  public List<LineString> deleteCutEdges() {
    computeNextCWEdges();
    // label the current set of edgerings
    findLabeledEdgeRings(this.dirEdges);

    /**
     * Cut Edges are edges where both dirEdges have the same label.
     * Delete them, and record them
     */
    final List<LineString> cutLines = new ArrayList<>();
    for (final Object element : this.dirEdges) {
      final PolygonizeDirectedEdge de = (PolygonizeDirectedEdge)element;
      if (de.isMarked()) {
        continue;
      }

      final PolygonizeDirectedEdge sym = (PolygonizeDirectedEdge)de.getSym();

      if (de.getLabel() == sym.getLabel()) {
        de.setMarked(true);
        sym.setMarked(true);

        // save the line as a cut edge
        final PolygonizeEdge e = (PolygonizeEdge)de.getEdge();
        cutLines.add(e.getLine());
      }
    }
    return cutLines;
  }

  /**
   * Marks all edges from the graph which are "dangles".
   * Dangles are which are incident on a node with degree 1.
   * This process is recursive, since removing a dangling edge
   * may result in another edge becoming a dangle.
   * In order to handle large recursion depths efficiently,
   * an explicit recursion stack is used
   *
   * @return a List containing the {@link LineString}s that formed dangles
   */
  public Collection<LineString> deleteDangles() {
    final List nodesToRemove = findNodesOfDegree(1);
    final Set<LineString> dangleLines = new HashSet<>();

    final Stack nodeStack = new Stack();
    for (final Object element : nodesToRemove) {
      nodeStack.push(element);
    }

    while (!nodeStack.isEmpty()) {
      final Node node = (Node)nodeStack.pop();

      deleteAllEdges(node);
      final List nodeOutEdges = node.getOutEdges().getEdges();
      for (final Object nodeOutEdge : nodeOutEdges) {
        final PolygonizeDirectedEdge de = (PolygonizeDirectedEdge)nodeOutEdge;
        // delete this edge and its sym
        de.setMarked(true);
        final PolygonizeDirectedEdge sym = (PolygonizeDirectedEdge)de.getSym();
        if (sym != null) {
          sym.setMarked(true);
        }

        // save the line as a dangle
        final PolygonizeEdge e = (PolygonizeEdge)de.getEdge();
        dangleLines.add(e.getLine());

        final Node toNode = de.getToNode();
        // add the toNode to the list to be processed, if it is now a dangle
        if (getDegreeNonDeleted(toNode) == 1) {
          nodeStack.push(toNode);
        }
      }
    }
    return dangleLines;
  }

  private EdgeRing findEdgeRing(final PolygonizeDirectedEdge startDE) {
    PolygonizeDirectedEdge de = startDE;
    final EdgeRing er = new EdgeRing(this.factory);
    do {
      er.add(de);
      de.setRing(er);
      de = de.getNext();
      Assert.isTrue(de != null, "found null DE in ring");
      Assert.isTrue(de == startDE || !de.isInRing(), "found DE already in ring");
    } while (de != startDE);

    return er;
  }

  /**
   * Computes the minimal EdgeRings formed by the edges in this graph.
   * @return a list of the {@link EdgeRing}s found by the polygonization process.
   */
  public List<EdgeRing> getEdgeRings() {
    // maybe could optimize this, since most of these pointers should be set
    // correctly already
    // by deleteCutEdges()
    computeNextCWEdges();
    // clear labels of all edges in graph
    label(this.dirEdges, -1);
    final List maximalRings = findLabeledEdgeRings(this.dirEdges);
    convertMaximalToMinimalEdgeRings(maximalRings);

    // find all edgerings (which will now be minimal ones, as required)
    final List<EdgeRing> edgeRingList = new ArrayList<>();
    for (final Object element : this.dirEdges) {
      final PolygonizeDirectedEdge de = (PolygonizeDirectedEdge)element;
      if (de.isMarked()) {
        continue;
      }
      if (de.isInRing()) {
        continue;
      }

      final EdgeRing er = findEdgeRing(de);
      edgeRingList.add(er);
    }
    return edgeRingList;
  }

  public GeometryFactory getGeometryFactory() {
    return this.factory;
  }

  private Node getNode(final Point point) {
    Node node = findNode(point);
    if (node == null) {
      final double x = point.getX();
      final double y = point.getY();
      node = new Node(x, y);
      // ensure node is only added once to graph
      add(node);
    }
    return node;
  }
}
