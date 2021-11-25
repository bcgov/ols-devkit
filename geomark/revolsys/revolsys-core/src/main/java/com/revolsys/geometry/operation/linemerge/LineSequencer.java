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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.planargraph.DirectedEdge;
import com.revolsys.geometry.planargraph.GraphComponent;
import com.revolsys.geometry.planargraph.Node;
import com.revolsys.geometry.planargraph.Subgraph;
import com.revolsys.geometry.planargraph.algorithm.ConnectedSubgraphFinder;
import com.revolsys.geometry.util.Assert;

/**
 * Builds a sequence from a set of LineStrings so that
 * they are ordered end to end.
 * A sequence is a complete non-repeating list of the linear
 * components of the input.  Each linestring is oriented
 * so that identical endpoints are adjacent in the list.
 * <p>
 * A typical use case is to convert a set of
 * unoriented geometric links
 * from a linear network
 * (e.g. such as block faces on a bus route)
 * into a continuous oriented path through the network.
 * <p>
 * The input linestrings may form one or more connected sets.
 * The input linestrings should be correctly noded, or the results may
 * not be what is expected.
 * The computed output is a single {@link Lineal} containing the ordered
 * linestrings in the sequence.
 * <p>
 * The sequencing employs the classic <b>Eulerian path</b> graph algorithm.
 * Since Eulerian paths are not uniquely determined,
 * further rules are used to
 * make the computed sequence preserve as much as possible of the input
 * ordering.
 * Within a connected subset of lines, the ordering rules are:
 * <ul>
 * <li>If there is degree-1 node which is the start
 * node of an linestring, use that node as the start of the sequence
 * <li>If there is a degree-1 node which is the end
 * node of an linestring, use that node as the end of the sequence
 * <li>If the sequence has no degree-1 nodes, use any node as the start
 * </ul>
 *
 * Note that not all arrangements of lines can be sequenced.
 * For a connected set of edges in a graph,
 * <i>Euler's Theorem</i> states that there is a sequence containing each edge once
 * <b>if and only if</b> there are no more than 2 nodes of odd degree.
 * If it is not possible to find a sequence, the {@link #isSequenceable()} method
 * will return <code>false</code>.
 *
 * @version 1.7
 */
public class LineSequencer {
  private static Node findLowestDegreeNode(final Subgraph graph) {
    int minDegree = Integer.MAX_VALUE;
    Node minDegreeNode = null;
    for (final Iterator i = graph.nodeIterator(); i.hasNext();) {
      final Node node = (Node)i.next();
      if (minDegreeNode == null || node.getDegree() < minDegree) {
        minDegree = node.getDegree();
        minDegreeNode = node;
      }
    }
    return minDegreeNode;
  }

  /**
   * Finds an {@link DirectedEdge} for an unvisited edge (if any),
   * choosing the dirEdge which preserves orientation, if possible.
   *
   * @param node the node to examine
   * @return the dirEdge found, or <code>null</code> if none were unvisited
   */
  private static DirectedEdge findUnvisitedBestOrientedDE(final Node node) {
    DirectedEdge wellOrientedDE = null;
    DirectedEdge unvisitedDE = null;
    for (final Object element : node.getOutEdges()) {
      final DirectedEdge de = (DirectedEdge)element;
      if (!de.getEdge().isVisited()) {
        unvisitedDE = de;
        if (de.getEdgeDirection()) {
          wellOrientedDE = de;
        }
      }
    }
    if (wellOrientedDE != null) {
      return wellOrientedDE;
    }
    return unvisitedDE;
  }

  /**
   * Tests whether a {@link Geometry} is sequenced correctly.
   * {@link LineString}s are trivially sequenced.
   * {@link Lineal}s are checked for correct sequencing.
   * Otherwise, <code>isSequenced</code> is defined
   * to be <code>true</code> for geometries that are not lineal.
   *
   * @param geom the geometry to test
   * @return <code>true</code> if the geometry is sequenced or is not lineal
   */
  public static boolean isSequenced(final Geometry geom) {
    if (!(geom instanceof Lineal) || geom instanceof LineString) {
      return true;
    }

    final Lineal lineal = (Lineal)geom;
    // the nodes in all subgraphs which have been completely scanned
    final Set prevSubgraphNodes = new TreeSet();

    Point lastNode = null;
    final List currNodes = new ArrayList();
    for (int i = 0; i < lineal.getGeometryCount(); i++) {
      final LineString line = (LineString)lineal.getGeometry(i);
      final Point startNode = line.getPoint(0);
      final Point endNode = line.getPoint(line.getVertexCount() - 1);

      /**
       * If this linestring is connected to a previous subgraph, geom is not sequenced
       */
      if (prevSubgraphNodes.contains(startNode)) {
        return false;
      }
      if (prevSubgraphNodes.contains(endNode)) {
        return false;
      }

      if (lastNode != null) {
        if (!startNode.equals(lastNode)) {
          // start new connected sequence
          prevSubgraphNodes.addAll(currNodes);
          currNodes.clear();
        }
      }
      currNodes.add(startNode);
      currNodes.add(endNode);
      lastNode = endNode;
    }
    return true;
  }

  public static Geometry sequence(final Geometry geom) {
    final LineSequencer sequencer = new LineSequencer();
    sequencer.add(geom);
    return sequencer.getSequencedLineStrings();
  }

  // initialize with default, in case no lines are input
  private GeometryFactory factory = GeometryFactory.DEFAULT_3D;

  private final LineMergeGraph graph = new LineMergeGraph();

  private boolean isRun = false;

  private boolean isSequenceable = false;

  private int lineCount = 0;

  private Geometry sequencedGeometry = null;

  /**
   * Adds a {@link Collection} of {@link Geometry}s to be sequenced.
   * May be called multiple times.
   * Any dimension of Geometry may be added; the constituent linework will be
   * extracted.
   *
   * @param geometries a Collection of geometries to add
   */
  public void add(final Collection geometries) {
    for (final Object element : geometries) {
      final Geometry geometry = (Geometry)element;
      add(geometry);
    }
  }

  /**
   * Adds a {@link Geometry} to be sequenced.
   * May be called multiple times.
   * Any dimension of Geometry may be added; the constituent linework will be
   * extracted.
   *
   * @param geometry the geometry to add
   */
  public void add(final Geometry geometry) {
    for (final LineString line : geometry.getGeometryComponents(LineString.class)) {
      addLine(line);
    }
  }

  private void addLine(final LineString lineString) {
    if (this.factory == null) {
      this.factory = lineString.getGeometryFactory();
    }
    this.graph.addEdge(lineString);
    this.lineCount++;
  }

  private void addReverseSubpath(DirectedEdge de, final ListIterator lit,
    final boolean expectedClosed) {
    // trace an unvisited path *backwards* from this de
    final Node endNode = de.getToNode();

    Node fromNode = null;
    while (true) {
      lit.add(de.getSym());
      de.getEdge().setVisited(true);
      fromNode = de.getFromNode();
      final DirectedEdge unvisitedOutDE = findUnvisitedBestOrientedDE(fromNode);
      // this must terminate, since we are continually marking edges as visited
      if (unvisitedOutDE == null) {
        break;
      }
      de = unvisitedOutDE.getSym();
    }
    if (expectedClosed) {
      // the path should end at the toNode of this de, otherwise we have an
      // error
      Assert.isTrue(fromNode == endNode, "path not contiguous");
    }
  }

  /**
   * Builds a geometry ({@link Lineal} )
   * representing the sequence.
   *
   * @param sequences a List of Lists of DirectedEdges with
   *   LineMergeEdges as their parent edges.
   * @return the sequenced geometry, or <code>null</code> if no sequence exists
   */
  private Geometry buildSequencedGeometry(final List sequences) {
    final List lines = new ArrayList();

    for (final Object element : sequences) {
      final List seq = (List)element;
      for (final Object element2 : seq) {
        final DirectedEdge de = (DirectedEdge)element2;
        final LineMergeEdge e = (LineMergeEdge)de.getEdge();
        final LineString line = e.getLine();

        LineString lineToAdd = line;
        if (!de.getEdgeDirection() && !line.isClosed()) {
          lineToAdd = line.reverse();
        }

        lines.add(lineToAdd);
      }
    }
    if (lines.size() == 0) {
      return this.factory.lineString();
    } else {
      return this.factory.buildGeometry(lines);
    }
  }

  private void computeSequence() {
    if (this.isRun) {
      return;
    }
    this.isRun = true;

    final List sequences = findSequences();
    if (sequences == null) {
      return;
    }

    this.sequencedGeometry = buildSequencedGeometry(sequences);
    this.isSequenceable = true;

    final int finalLineCount = this.sequencedGeometry.getGeometryCount();
    Assert.isTrue(this.lineCount == finalLineCount, "Lines were missing from result");
    Assert.isTrue(this.sequencedGeometry instanceof Lineal, "Result is not lineal");
  }

  private List findSequence(final Subgraph graph) {
    GraphComponent.setVisited(graph.edgeIterator(), false);

    final Node startNode = findLowestDegreeNode(graph);
    final DirectedEdge startDE = startNode.getOutEdges().iterator().next();
    final DirectedEdge startDESym = startDE.getSym();

    final List seq = new LinkedList();
    final ListIterator lit = seq.listIterator();
    addReverseSubpath(startDESym, lit, false);
    while (lit.hasPrevious()) {
      final DirectedEdge prev = (DirectedEdge)lit.previous();
      final DirectedEdge unvisitedOutDE = findUnvisitedBestOrientedDE(prev.getFromNode());
      if (unvisitedOutDE != null) {
        addReverseSubpath(unvisitedOutDE.getSym(), lit, true);
      }
    }

    /**
     * At this point, we have a valid sequence of graph DirectedEdges, but it
     * is not necessarily appropriately oriented relative to the underlying
     * geometry.
     */
    final List orientedSeq = orient(seq);
    return orientedSeq;
  }

  private List findSequences() {
    final List sequences = new ArrayList();
    final ConnectedSubgraphFinder csFinder = new ConnectedSubgraphFinder(this.graph);
    final List subgraphs = csFinder.getConnectedSubgraphs();
    for (final Object subgraph2 : subgraphs) {
      final Subgraph subgraph = (Subgraph)subgraph2;
      if (hasSequence(subgraph)) {
        final List seq = findSequence(subgraph);
        sequences.add(seq);
      } else {
        // if any subgraph cannot be sequenced, abort
        return null;
      }
    }
    return sequences;
  }

  /**
   * Returns the {@link Lineal}
   * built by the sequencing process, if one exists.
   *
   * @return the sequenced linestrings,
   * or <code>null</code> if a valid sequence does not exist
   */
  public Geometry getSequencedLineStrings() {
    computeSequence();
    return this.sequencedGeometry;
  }

  /**
   * Tests whether a complete unique path exists in a graph
   * using Euler's Theorem.
   *
   * @param graph the subgraph containing the edges
   * @return <code>true</code> if a sequence exists
   */
  private boolean hasSequence(final Subgraph graph) {
    int oddDegreeCount = 0;
    for (final Iterator i = graph.nodeIterator(); i.hasNext();) {
      final Node node = (Node)i.next();
      if (node.getDegree() % 2 == 1) {
        oddDegreeCount++;
      }
    }
    return oddDegreeCount <= 2;
  }

  /**
   * Tests whether the arrangement of linestrings has a valid
   * sequence.
   *
   * @return <code>true</code> if a valid sequence exists.
   */
  public boolean isSequenceable() {
    computeSequence();
    return this.isSequenceable;
  }

  /**
   * Computes a version of the sequence which is optimally
   * oriented relative to the underlying geometry.
   * <p>
   * Heuristics used are:
   * <ul>
   * <li>If the path has a degree-1 node which is the start
   * node of an linestring, use that node as the start of the sequence
   * <li>If the path has a degree-1 node which is the end
   * node of an linestring, use that node as the end of the sequence
   * <li>If the sequence has no degree-1 nodes, use any node as the start
   * (NOTE: in this case could orient the sequence according to the majority of the
   * linestring orientations)
   * </ul>
   *
   * @param seq a List of DirectedEdges
   * @return a List of DirectedEdges oriented appropriately
   */
  private List orient(final List seq) {
    final DirectedEdge startEdge = (DirectedEdge)seq.get(0);
    final DirectedEdge endEdge = (DirectedEdge)seq.get(seq.size() - 1);
    final Node startNode = startEdge.getFromNode();
    final Node endNode = endEdge.getToNode();

    boolean flipSeq = false;
    final boolean hasDegree1Node = startNode.getDegree() == 1 || endNode.getDegree() == 1;

    if (hasDegree1Node) {
      boolean hasObviousStartNode = false;

      // test end edge before start edge, to make result stable
      // (ie. if both are good starts, pick the actual start
      if (endEdge.getToNode().getDegree() == 1 && endEdge.getEdgeDirection() == false) {
        hasObviousStartNode = true;
        flipSeq = true;
      }
      if (startEdge.getFromNode().getDegree() == 1 && startEdge.getEdgeDirection() == true) {
        hasObviousStartNode = true;
        flipSeq = false;
      }

      // since there is no obvious start node, use any node of degree 1
      if (!hasObviousStartNode) {
        // check if the start node should actually be the end node
        if (startEdge.getFromNode().getDegree() == 1) {
          flipSeq = true;
          // if the end node is of degree 1, it is properly the end node
        }
      }

    }

    // if there is no degree 1 node, just use the sequence as is
    // (Could insert heuristic of taking direction of majority of lines as
    // overall direction)

    if (flipSeq) {
      return reverse(seq);
    }
    return seq;
  }

  /**
   * Reverse the sequence.
   * This requires reversing the order of the dirEdges, and flipping
   * each dirEdge as well
   *
   * @param seq a List of DirectedEdges, in sequential order
   * @return the reversed sequence
   */
  private List reverse(final List seq) {
    final LinkedList newSeq = new LinkedList();
    for (final Object element : seq) {
      final DirectedEdge de = (DirectedEdge)element;
      newSeq.addFirst(de.getSym());
    }
    return newSeq;
  }

}
