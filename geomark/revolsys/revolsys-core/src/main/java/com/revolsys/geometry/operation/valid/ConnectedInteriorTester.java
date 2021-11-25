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
package com.revolsys.geometry.operation.valid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.revolsys.geometry.geomgraph.DirectedEdge;
import com.revolsys.geometry.geomgraph.Edge;
import com.revolsys.geometry.geomgraph.EdgeRing;
import com.revolsys.geometry.geomgraph.GeometryGraph;
import com.revolsys.geometry.geomgraph.Label;
import com.revolsys.geometry.geomgraph.PlanarGraph;
import com.revolsys.geometry.geomgraph.Position;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.operation.overlay.MaximalEdgeRing;
import com.revolsys.geometry.operation.overlay.OverlayNodeFactory;
import com.revolsys.geometry.util.Assert;

/**
 * This class tests that the interior of an area {@link Geometry}
 * ( {@link Polygonal} )
 * is connected.
 * This can happen if:
 * <ul>
 * <li>a shell self-intersects
 * <li>one or more holes form a connected chain touching a shell at two different points
 * <li>one or more holes form a ring around a subset of the interior
 * </ul>
 * If a disconnected situation is found the location of the problem is recorded.
 *
 * @version 1.7
 */
public class ConnectedInteriorTester {

  public static Point findDifferentPoint(final LineString line, final Point point) {
    final int vertexCount = line.getVertexCount();
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      final double x = line.getX(vertexIndex);
      final double y = line.getY(vertexIndex);
      if (!point.equalsVertex(x, y)) {
        return new PointDoubleXY(x, y);
      }
    }
    return null;
  }

  // save a coordinate for any disconnected interior found
  // the coordinate will be somewhere on the ring surrounding the disconnected
  // interior
  private Point disconnectedRingcoord;

  private final GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;

  private final GeometryGraph geomGraph;

  public ConnectedInteriorTester(final GeometryGraph geomGraph) {
    this.geomGraph = geomGraph;
  }

  /**
   * Form DirectedEdges in graph into Minimal EdgeRings.
   * (Minimal Edgerings must be used, because only they are guaranteed to provide
   * a correct isHole computation)
   */
  private List buildEdgeRings(final Collection dirEdges) {
    final List edgeRings = new ArrayList();
    for (final Object dirEdge : dirEdges) {
      final DirectedEdge de = (DirectedEdge)dirEdge;
      // if this edge has not yet been processed
      if (de.isInResult() && de.getEdgeRing() == null) {
        final MaximalEdgeRing er = new MaximalEdgeRing(de, this.geometryFactory);

        er.linkDirectedEdgesForMinimalEdgeRings();
        final List minEdgeRings = er.buildMinimalRings();
        edgeRings.addAll(minEdgeRings);
      }
    }
    return edgeRings;
  }

  public Point getCoordinate() {
    return this.disconnectedRingcoord;
  }

  /**
   * Check if any shell ring has an unvisited edge.
   * A shell ring is a ring which is not a hole and which has the interior
   * of the parent area on the RHS.
   * (Note that there may be non-hole rings with the interior on the LHS,
   * since the interior of holes will also be polygonized into CW rings
   * by the linkAllDirectedEdges() step)
   *
   * @return true if there is an unvisited edge in a non-hole ring
   */
  private boolean hasUnvisitedShellEdge(final List edgeRings) {
    for (final Object edgeRing : edgeRings) {
      final EdgeRing er = (EdgeRing)edgeRing;
      // don't check hole rings
      if (er.isHole()) {
        continue;
      }
      final List edges = er.getEdges();
      DirectedEdge de = (DirectedEdge)edges.get(0);
      // don't check CW rings which are holes
      // (MD - this check may now be irrelevant)
      if (de.getLabel().getLocation(0, Position.RIGHT) != Location.INTERIOR) {
        continue;
      }

      /**
       * the edgeRing is CW ring which surrounds the INT of the area, so check all
       * edges have been visited.  If any are unvisited, this is a disconnected part of the interior
       */
      for (final Object edge : edges) {
        de = (DirectedEdge)edge;
        // Debug.print("visted? "); Debug.println(de);
        if (!de.isVisited()) {
          // Debug.print("not visited "); Debug.println(de);
          this.disconnectedRingcoord = de.getCoordinate();
          return true;
        }
      }
    }
    return false;
  }

  public boolean isInteriorsConnected() {
    // node the edges, in case holes touch the shell
    final List splitEdges = new ArrayList();
    this.geomGraph.computeSplitEdges(splitEdges);

    // form the edges into rings
    final PlanarGraph graph = new PlanarGraph(new OverlayNodeFactory());
    graph.addEdges(splitEdges);
    setInteriorEdgesInResult(graph);
    graph.linkResultDirectedEdges();
    final List edgeRings = buildEdgeRings(graph.getEdgeEnds());

    /**
     * Mark all the edges for the edgeRings corresponding to the shells
     * of the input polygons.  Note only ONE ring gets marked for each shell.
     */
    visitShellInteriors(this.geomGraph.getGeometry(), graph);

    /**
     * If there are any unvisited shell edges
     * (i.e. a ring which is not a hole and which has the interior
     * of the parent area on the RHS)
     * this means that one or more holes must have split the interior of the
     * polygon into at least two pieces.  The polygon is thus invalid.
     */
    return !hasUnvisitedShellEdge(edgeRings);
  }

  private void setInteriorEdgesInResult(final PlanarGraph graph) {
    for (final Object element : graph.getEdgeEnds()) {
      final DirectedEdge de = (DirectedEdge)element;
      if (de.getLabel().getLocation(0, Position.RIGHT) == Location.INTERIOR) {
        de.setInResult(true);
      }
    }
  }

  private void visitInteriorRing(final LineString ring, final PlanarGraph graph) {
    final Point pt0 = ring.getVertex(0).newPoint2D();
    /**
     * Find first point in coord list different to initial point.
     * Need special check since the first point may be repeated.
     */
    final Point pt1 = findDifferentPoint(ring, pt0);
    final Edge e = graph.findEdgeInSameDirection(pt0, pt1);
    final DirectedEdge de = (DirectedEdge)graph.findEdgeEnd(e);
    if (de != null) {
      DirectedEdge intDe = null;
      final Label label = de.getLabel();
      if (label.getLocation(0, Position.RIGHT) == Location.INTERIOR) {
        intDe = de;
      } else if (de.getSym().getLabel().getLocation(0, Position.RIGHT) == Location.INTERIOR) {
        intDe = de.getSym();
      }
      Assert.isTrue(intDe != null, "unable to find dirEdge with Interior on RHS");

      visitLinkedDirectedEdges(intDe);
    }
  }

  protected void visitLinkedDirectedEdges(final DirectedEdge start) {
    final DirectedEdge startDe = start;
    DirectedEdge de = start;
    do {
      Assert.isTrue(de != null, "found null Directed Edge");
      de.setVisited(true);
      de = de.getNext();
    } while (de != startDe);
  }

  /**
   * Mark all the edges for the edgeRings corresponding to the shells
   * of the input polygons.
   * Only ONE ring gets marked for each shell - if there are others which remain unmarked
   * this indicates a disconnected interior.
   */
  private void visitShellInteriors(final Geometry g, final PlanarGraph graph) {
    if (g instanceof Polygon) {
      final Polygon p = (Polygon)g;
      visitInteriorRing(p.getShell(), graph);
    } else if (g instanceof Polygonal) {
      final Polygonal polygonal = (Polygonal)g;
      for (final Polygon polygon : polygonal.polygons()) {
        visitInteriorRing(polygon.getShell(), graph);
      }
    }
  }
}
