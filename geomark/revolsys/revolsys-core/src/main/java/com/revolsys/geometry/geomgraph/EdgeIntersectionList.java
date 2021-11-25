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
package com.revolsys.geometry.geomgraph;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.impl.LineStringDouble;
import com.revolsys.util.Strings;

/**
 * A list of edge intersections along an {@link Edge}.
 * Implements splitting an edge with intersections
 * into multiple resultant edges.
 *
 * @version 1.7
 */
public class EdgeIntersectionList implements Iterable<EdgeIntersection> {
  Edge edge; // the parent edge

  // a Map <EdgeIntersection, EdgeIntersection>
  private final Map<EdgeIntersection, EdgeIntersection> nodeMap = new TreeMap<>();

  public EdgeIntersectionList(final Edge edge) {
    this.edge = edge;
  }

  /**
   * Adds an intersection into the list, if it isn't already there.
   * The input segmentIndex and dist are expected to be normalized.
   * @return the EdgeIntersection found or added
   */
  public EdgeIntersection add(final double x, final double y, final int segmentIndex,
    final double dist) {
    final EdgeIntersection eiNew = new EdgeIntersection(x, y, segmentIndex, dist);
    final EdgeIntersection edgeIntersection = this.nodeMap.get(eiNew);
    if (edgeIntersection != null) {
      return edgeIntersection;
    }
    this.nodeMap.put(eiNew, eiNew);
    return eiNew;
  }

  /**
   * Adds entries for the first and last points of the edge to the list
   */
  public void addEndpoints() {
    final Edge edge = this.edge;
    final int maxSegIndex = edge.getVertexCount() - 1;
    final double x1 = edge.getX(0);
    final double y1 = edge.getY(0);
    add(x1, y1, 0, 0.0);
    final double x2 = edge.getX(maxSegIndex);
    final double y2 = edge.getY(maxSegIndex);
    add(x2, y2, maxSegIndex, 0.0);
  }

  /**
   * Creates new edges for all the edges that the intersections in this
   * list split the parent edge into.
   * Adds the edges to the input list (this is so a single list
   * can be used to accumulate all split edges for a Geometry).
   *
   * @param edgeList a list of EdgeIntersections
   */
  public void addSplitEdges(final List<Edge> edgeList) {
    // ensure that the list has entries for the first and last point of the edge
    addEndpoints();

    final Iterator<EdgeIntersection> it = iterator();
    // there should always be at least two entries in the list
    EdgeIntersection eiPrev = it.next();
    while (it.hasNext()) {
      final EdgeIntersection ei = it.next();
      final Edge newEdge = newSplitEdge(eiPrev, ei);
      edgeList.add(newEdge);

      eiPrev = ei;
    }
  }

  /**
   * Tests if the given point is an edge intersection
   *
   * @return true if the point is an intersection
   */
  public boolean isIntersection(final double x, final double y) {
    for (final EdgeIntersection edgeIntersection : this) {
      if (edgeIntersection.equalsVertex(x, y)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns an iterator of {@link EdgeIntersection}s
   *
   * @return an Iterator of EdgeIntersections
   */
  @Override
  public Iterator<EdgeIntersection> iterator() {
    return this.nodeMap.values().iterator();
  }

  /**
   * Construct a new new "split edge" with the section of points between
   * (and including) the two intersections.
   * The label for the new edge is the same as the label for the parent edge.
   */
  Edge newSplitEdge(final EdgeIntersection ei0, final EdgeIntersection ei1) {
    final int fromIndex = ei0.segmentIndex;
    final int toIndex = ei1.segmentIndex;
    int pointCount = toIndex - fromIndex + 2;

    // if the last intersection point is not equal to the its segment start pt,
    // add it to the points list as well.
    // (This check is needed because the distance metric is not totally
    // reliable!)
    // The check for point equality is 2D only - Z values are ignored
    final double xEnd = ei1.getX();
    final double yEnd = ei1.getY();
    final Edge edge = this.edge;
    final boolean useIntPt1 = ei1.dist > 0.0 || !edge.equalsVertex(toIndex, xEnd, yEnd);
    if (!useIntPt1) {
      pointCount--;
    }

    final double[] coordinates = new double[pointCount * 2];
    int coordinateIndex = 0;
    coordinates[coordinateIndex++] = ei0.getX();
    coordinates[coordinateIndex++] = ei0.getY();
    for (int i = fromIndex + 1; i <= toIndex; i++) {
      final double x = edge.getX(i);
      final double y = edge.getY(i);
      coordinates[coordinateIndex++] = x;
      coordinates[coordinateIndex++] = y;
    }
    if (useIntPt1) {
      coordinates[coordinateIndex++] = xEnd;
      coordinates[coordinateIndex++] = yEnd;
    }
    final LineString line = new LineStringDouble(2, coordinates);
    return new Edge(line, new Label(edge.label));
  }

  @Override
  public String toString() {
    return Strings.toString(this.nodeMap.values());
  }
}
