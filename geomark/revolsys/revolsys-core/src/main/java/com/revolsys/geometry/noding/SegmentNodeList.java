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
package com.revolsys.geometry.noding;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.model.impl.LineStringDouble;
import com.revolsys.geometry.util.Assert;

// INCOMPLETE!
class NodeVertexIterator implements Iterator<SegmentNode> {
  private SegmentNode currNode = null;

  private SegmentNode nextNode = null;

  private final Iterator<SegmentNode> nodeIt;

  NodeVertexIterator(final SegmentNodeList nodeList) {
    this.nodeIt = nodeList.iterator();
    readNextNode();
  }

  @Override
  public boolean hasNext() {
    if (this.nextNode == null) {
      return false;
    }
    return true;
  }

  @Override
  public SegmentNode next() {
    if (this.currNode == null) {
      this.currNode = this.nextNode;
      readNextNode();
      return this.currNode;
    }
    // check for trying to read too far
    if (this.nextNode == null) {
      return null;
    }

    if (this.nextNode.getSegmentIndex() == this.currNode.getSegmentIndex()) {
      this.currNode = this.nextNode;
      readNextNode();
      return this.currNode;
    }

    if (this.nextNode.getSegmentIndex() > this.currNode.getSegmentIndex()) {

    }
    return null;
  }

  private void readNextNode() {
    if (this.nodeIt.hasNext()) {
      this.nextNode = this.nodeIt.next();
    } else {
      this.nextNode = null;
    }
  }

  /**
   *  Not implemented.
   *
   *@throws  UnsupportedOperationException  This method is not implemented.
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException(getClass().getName());
  }

}

/**
 * A list of the {@link SegmentNode}s present along a noded {@link SegmentString}.
 *
 * @version 1.7
 */
public class SegmentNodeList implements Iterable<SegmentNode> {
  private final NodedSegmentString edge;

  private final Map<SegmentNode, SegmentNode> nodeMap = new TreeMap<>();

  public SegmentNodeList(final NodedSegmentString edge) {
    this.edge = edge;
  }

  /**
   * Adds an intersection into the list, if it isn't already there.
   * The input segmentIndex and dist are expected to be normalized.
   *
   * @return the SegmentIntersection found or added
   */
  public SegmentNode add(final double x, final double y, final int segmentIndex) {
    if (!Double.isFinite(x) || !Double.isFinite(y)) {
      throw new IllegalArgumentException("Cannot add an empty point to a SegmentNodeList");
    } else {
      final int segmentOctant = this.edge.getSegmentOctant(segmentIndex);
      final SegmentNode newNode = new SegmentNode(this.edge, x, y, segmentIndex, segmentOctant);
      SegmentNode node = this.nodeMap.get(newNode);
      if (node == null) {
        node = newNode;
        this.nodeMap.put(node, node);
      } else {
        // debugging sanity check
        final boolean equals = node.equalsVertex(x, y);
        if (!equals) {
          Assert.isTrue(equals, "Found equal nodes with different coordinates");
        }
      }
      return node;
    }
  }

  /**
   * Adds nodes for any collapsed edge pairs.
   * Collapsed edge pairs can be caused by inserted nodes, or they can be
   * pre-existing in the edge vertex list.
   * In order to provide the correct fully noded semantics,
   * the vertex at the base of a collapsed pair must also be added as a node.
   */
  private void addCollapsedNodes() {
    final List<Integer> collapsedVertexIndexes = new ArrayList<>();

    findCollapsesFromInsertedNodes(collapsedVertexIndexes);
    findCollapsesFromExistingVertices(collapsedVertexIndexes);

    // node the collapses
    for (final int vertexIndex : collapsedVertexIndexes) {
      final double x = this.edge.getX(vertexIndex);
      final double y = this.edge.getY(vertexIndex);
      add(x, y, vertexIndex);
    }
  }

  /**
   * Adds nodes for the first and last points of the edge
   */
  private void addEndpoints() {
    final int maxSegIndex = this.edge.size() - 1;
    final double fromPointX = this.edge.getX(0);
    final double fromPointY = this.edge.getY(0);
    add(fromPointX, fromPointY, 0);

    final double toPointX = this.edge.getX(maxSegIndex);
    final double toPointY = this.edge.getY(maxSegIndex);
    add(toPointX, toPointY, maxSegIndex);
  }

  /**
   * Creates new edges for all the edges that the intersections in this
   * list split the parent edge into.
   * Adds the edges to the provided argument list
   * (this is so a single list can be used to accumulate all split edges
   * for a set of {@link SegmentString}s).
   */
  public void addSplitEdges(final Collection edgeList) {
    // ensure that the list has entries for the first and last point of the edge
    addEndpoints();
    addCollapsedNodes();

    final Iterator<SegmentNode> it = iterator();
    // there should always be at least two entries in the list, since the
    // endpoints are nodes
    SegmentNode eiPrev = it.next();
    while (it.hasNext()) {
      final SegmentNode ei = it.next();
      final SegmentString newEdge = newSplitEdge(eiPrev, ei);
      /*
       * if (newEdge.size() < 2) throw new RuntimeException(
       * "created single point edge: " + newEdge.toString());
       */
      edgeList.add(newEdge);
      eiPrev = ei;
    }
    // checkSplitEdgesCorrectness(testingSplitEdges);
  }

  private boolean findCollapseIndex(final SegmentNode ei0, final SegmentNode ei1,
    final int[] collapsedVertexIndex) {
    // only looking for equal nodes
    if (!ei0.equals(2, ei1)) {
      return false;
    }

    int numVerticesBetween = ei1.getSegmentIndex() - ei0.getSegmentIndex();
    if (!ei1.isInterior()) {
      numVerticesBetween--;
    }

    // if there is a single vertex between the two equal nodes, this is a
    // collapse
    if (numVerticesBetween == 1) {
      collapsedVertexIndex[0] = ei0.getSegmentIndex() + 1;
      return true;
    }
    return false;
  }

  /**
   * Adds nodes for any collapsed edge pairs
   * which are pre-existing in the vertex list.
   */
  private void findCollapsesFromExistingVertices(final List<Integer> collapsedVertexIndexes) {
    for (int i = 0; i < this.edge.size() - 2; i++) {
      if (this.edge.equalsVertex2d(i, i + 2)) {
        // add base of collapse as node
        collapsedVertexIndexes.add(i + 1);
      }
    }
  }

  /**
   * Adds nodes for any collapsed edge pairs caused by inserted nodes
   * Collapsed edge pairs occur when the same coordinate is inserted as a node
   * both before and after an existing edge vertex.
   * To provide the correct fully noded semantics,
   * the vertex must be added as a node as well.
   */
  private void findCollapsesFromInsertedNodes(final List<Integer> collapsedVertexIndexes) {
    final int[] collapsedVertexIndex = new int[1];
    final Iterator<SegmentNode> it = iterator();
    // there should always be at least two entries in the list, since the
    // endpoints are nodes
    SegmentNode eiPrev = it.next();
    while (it.hasNext()) {
      final SegmentNode ei = it.next();
      final boolean isCollapsed = findCollapseIndex(eiPrev, ei, collapsedVertexIndex);
      if (isCollapsed) {
        collapsedVertexIndexes.add(collapsedVertexIndex[0]);
      }

      eiPrev = ei;
    }
  }

  public NodedSegmentString getEdge() {
    return this.edge;
  }

  /**
   * returns an iterator of SegmentNodes
   */
  @Override
  public Iterator<SegmentNode> iterator() {
    return this.nodeMap.values().iterator();
  }

  /**
   * Construct a new new "split edge" with the section of points between
   * (and including) the two intersections.
   * The label for the new edge is the same as the label for the parent edge.
   */
  SegmentString newSplitEdge(final SegmentNode ei0, final SegmentNode ei1) {
    // Debug.println("\ncreateSplitEdge"); Debug.print(ei0); Debug.print(ei1);
    int npts = ei1.getSegmentIndex() - ei0.getSegmentIndex() + 2;

    // if the last intersection point is not equal to the its segment start pt,
    // add it to the points list as well.
    // (This check is needed because the distance metric is not totally
    // reliable!)
    // The check for point equality is 2D only - Z values are ignored
    final boolean useIntPt1 = ei1.isInterior()
      || !this.edge.equalsVertex(ei1.getSegmentIndex(), ei1.getX(), ei1.getY());
    if (!useIntPt1) {
      npts--;
    }

    final int axisCount = this.edge.getLineString().getAxisCount();
    final double[] coordinates = new double[npts * axisCount];

    int ipt = 0;
    CoordinatesListUtil.setCoordinates(coordinates, axisCount, ipt++, ei0);
    for (int i = ei0.getSegmentIndex() + 1; i <= ei1.getSegmentIndex(); i++) {
      CoordinatesListUtil.setCoordinates(coordinates, axisCount, ipt++, this.edge, i);
    }
    if (useIntPt1) {
      CoordinatesListUtil.setCoordinates(coordinates, axisCount, ipt++, ei1);
    }

    final LineStringDouble points = new LineStringDouble(axisCount, coordinates);
    return new NodedSegmentString(points, this.edge.getData());
  }

  public void print(final PrintStream out) {
    out.println("Intersections:");
    for (final SegmentNode node : this) {
      node.print(out);
    }
  }
}
