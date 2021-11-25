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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.geometry.algorithm.BoundaryNodeRule;
import com.revolsys.geometry.algorithm.LineIntersector;
import com.revolsys.geometry.geomgraph.index.EdgeSetIntersector;
import com.revolsys.geometry.geomgraph.index.SegmentIntersector;
import com.revolsys.geometry.geomgraph.index.SimpleMCSweepLineIntersector;
import com.revolsys.geometry.model.Dimension;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;

/**
 * A GeometryGraph is a graph that models a given Geometry
 * @version 1.7
 */
public class GeometryGraph extends PlanarGraph {
  /**
   * This method implements the Boundary Determination Rule
   * for determining whether
   * a component (node or edge) that appears multiple times in elements
   * of a MultiGeometry is in the boundary or the interior of the Geometry
   * <br>
   * The SFS uses the "Mod-2 Rule", which this function implements
   * <br>
   * An alternative (and possibly more intuitive) rule would be
   * the "At Most One Rule":
   *    isInBoundary = (componentCount == 1)
   */
  public static Location determineBoundary(final BoundaryNodeRule boundaryNodeRule,
    final int boundaryCount) {
    return boundaryNodeRule.isInBoundary(boundaryCount) ? Location.BOUNDARY : Location.INTERIOR;
  }

  private final int argIndex;

  private BoundaryNodeRule boundaryNodeRule = null;

  private Collection<Node> boundaryNodes;

  private final Geometry geometry;

  private boolean hasTooFewPoints = false;

  private Point invalidPoint = null;

  /**
   * The lineEdgeMap is a map of the linestring components of the
   * parentGeometry to the edges which are derived from them.
   * This is used to efficiently perform findEdge queries
   */
  private final Map<LineString, Edge> lineEdgeMap = new HashMap<>();

  /**
   * If this flag is true, the Boundary Determination Rule will used when deciding
   * whether nodes are in the boundary or not
   */
  private boolean useBoundaryDeterminationRule = true;

  public GeometryGraph(final int argIndex, final Geometry geometry) {
    this(argIndex, geometry, BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE);
  }

  public GeometryGraph(final int argIndex, final Geometry geometry,
    final BoundaryNodeRule boundaryNodeRule) {
    this.argIndex = argIndex;
    this.geometry = geometry;
    this.boundaryNodeRule = boundaryNodeRule;
    if (geometry != null) {
      add(geometry);
    }
  }

  private void add(final Geometry geometry) {
    if (!geometry.isEmpty()) {
      if (geometry instanceof Polygon) {
        addPolygon((Polygon)geometry);
      } else if (geometry instanceof LineString) {
        addLineString((LineString)geometry);
      } else if (geometry instanceof Point) {
        addPoint((Point)geometry);
      } else if (geometry instanceof Polygonal) {
        this.useBoundaryDeterminationRule = false;
        addCollection(geometry);
      } else if (geometry.isGeometryCollection()) {
        addCollection(geometry);
      } else {
        throw new UnsupportedOperationException(geometry.getClass().getName());
      }
    }
  }

  private void addCollection(final Geometry geometry) {
    for (int i = 0; i < geometry.getGeometryCount(); i++) {
      final Geometry part = geometry.getGeometry(i);
      add(part);
    }
  }

  /**
   * Add an Edge computed externally.  The label on the Edge is assumed
   * to be correct.
   */
  public void addEdge(final Edge edge) {
    insertEdge(edge);
    // insert the endpoint as a node, to mark that it is on the boundary
    insertPoint(this.argIndex, edge.getPoint(0), Location.BOUNDARY);
    insertPoint(this.argIndex, edge.getPoint(edge.getVertexCount() - 1), Location.BOUNDARY);
  }

  private void addLineString(final LineString line) {
    final LineString cleanLine = line.removeDuplicatePoints();

    final int cleanVertexCount = cleanLine.getVertexCount();
    if (cleanVertexCount < 2 || cleanLine.isEmpty()) {
      this.hasTooFewPoints = true;
      this.invalidPoint = cleanLine.getPoint(0);
      return;
    } else {
      // add the edge for the LineString
      // line edges do not have locations for their left and right sides
      final Edge e = new Edge(cleanLine, new Label(this.argIndex, Location.INTERIOR));
      this.lineEdgeMap.put(line, e);
      insertEdge(e);
      /**
       * Add the boundary points of the LineString, if any.
       * Even if the LineString is closed, add both points as if they were endpoints.
       * This allows for the case that the node already exists and is a boundary point.
       */
      insertBoundaryPoint(this.argIndex, cleanLine.getPoint(0));
      insertBoundaryPoint(this.argIndex, cleanLine.getPoint(cleanVertexCount - 1));
    }
  }

  /**
   * Add a point computed externally.  The point is assumed to be a
   * Point Geometry part, which has a location of INTERIOR.
   */
  public void addPoint(final Point pt) {
    insertPoint(this.argIndex, pt, Location.INTERIOR);
  }

  private void addPolygon(final Polygon p) {
    addPolygonRing(p.getShell(), Location.EXTERIOR, Location.INTERIOR);

    for (int i = 0; i < p.getHoleCount(); i++) {
      final LinearRing hole = p.getHole(i);

      // Holes are topologically labelled opposite to the shell, since
      // the interior of the polygon lies on their opposite side
      // (on the left, if the hole is oriented CW)
      addPolygonRing(hole, Location.INTERIOR, Location.EXTERIOR);
    }
  }

  /**
   * Adds a polygon ring to the graph.
   * Empty rings are ignored.
   *
   * The left and right topological location arguments assume that the ring is oriented CW.
   * If the ring is in the opposite orientation,
   * the left and right locations must be interchanged.
   */
  private void addPolygonRing(final LinearRing ring, final Location cwLeft,
    final Location cwRight) {
    if (!ring.isEmpty()) {
      final LineString simplifiedRing;
      try {
        simplifiedRing = ring.removeDuplicatePoints();
        final int vertexCount = simplifiedRing.getVertexCount();
        if (vertexCount > 3) {
          Location left;
          Location right;
          if (ring.isCounterClockwise()) {
            left = cwRight;
            right = cwLeft;
          } else {
            left = cwLeft;
            right = cwRight;
          }
          final Label label = new Label(this.argIndex, Location.BOUNDARY, left, right);
          final Edge e = new Edge(simplifiedRing, label);
          this.lineEdgeMap.put(ring, e);

          insertEdge(e);
          // insert the endpoint as a node, to mark that it is on the boundary
          insertPoint(this.argIndex, simplifiedRing.getPoint2D(0), Location.BOUNDARY);

        } else if (vertexCount == 0) {
          this.hasTooFewPoints = true;
          this.invalidPoint = GeometryFactory.DEFAULT_2D.point();
        } else {
          this.hasTooFewPoints = true;
          this.invalidPoint = simplifiedRing.getPoint2D(0);
        }
      } catch (final IllegalArgumentException e) {
        this.hasTooFewPoints = true;
        this.invalidPoint = ring.getPoint2D(0);
      }
    }
  }

  /**
   * Add a node for a self-intersection.
   * If the node is a potential boundary node (e.g. came from an edge which
   * is a boundary) then insert it as a potential boundary node.
   * Otherwise, just add it as a regular node.
   */
  private void addSelfIntersectionNode(final int argIndex, final Point point, final Location loc) {
    // if this node is already a boundary node, don't change it
    if (isBoundaryNode(argIndex, point)) {
      return;
    }
    if (loc == Location.BOUNDARY && this.useBoundaryDeterminationRule) {
      insertBoundaryPoint(argIndex, point);
    } else {
      insertPoint(argIndex, point, loc);
    }
  }

  private void addSelfIntersectionNodes(final int argIndex) {
    for (final Edge e : this.edges) {
      final Location eLoc = e.getLabel().getLocation(argIndex);
      for (final EdgeIntersection ei : e.getEdgeIntersectionList()) {
        addSelfIntersectionNode(argIndex, ei.newPoint2D(), eLoc);
      }
    }
  }

  public SegmentIntersector computeEdgeIntersections(final GeometryGraph g,
    final LineIntersector li, final boolean includeProper) {
    final SegmentIntersector si = new SegmentIntersector(li, includeProper, true);
    si.setBoundaryNodes(getBoundaryNodes(), g.getBoundaryNodes());

    final EdgeSetIntersector esi = newEdgeSetIntersector();
    esi.computeIntersections(this.edges, g.edges, si);
    /*
     * for (Iterator i = g.edges.iterator(); i.hasNext();) { Edge e = (Edge)
     * i.next(); Debug.print(e.getEdgeIntersectionList()); }
     */
    return si;
  }

  /**
   * Compute self-nodes, taking advantage of the Geometry type to
   * minimize the number of intersection tests.  (E.g. rings are
   * not tested for self-intersection, since they are assumed to be valid).
   * @param li the LineIntersector to use
   * @param computeRingSelfNodes if <false>, intersection checks are optimized to not test rings for self-intersection
   * @return the SegmentIntersector used, containing information about the intersections found
   */
  public SegmentIntersector computeSelfNodes(final LineIntersector li,
    final boolean computeRingSelfNodes) {
    final SegmentIntersector si = new SegmentIntersector(li, true, false);
    final EdgeSetIntersector esi = newEdgeSetIntersector();
    // optimized test for Polygons and Rings
    if (!computeRingSelfNodes
      && (this.geometry instanceof LinearRing || this.geometry instanceof Polygonal)) {
      esi.computeIntersections(this.edges, si, false);
    } else {
      esi.computeIntersections(this.edges, si, true);
    }
    // System.out.println("SegmentIntersector # tests = " + si.numTests);
    addSelfIntersectionNodes(this.argIndex);
    return si;
  }

  public void computeSplitEdges(final List<Edge> edgelist) {
    for (final Edge edge : this.edges) {
      final EdgeIntersectionList edgeIntersectionList = edge.getEdgeIntersectionList();
      edgeIntersectionList.addSplitEdges(edgelist);
    }
  }

  public Edge findEdge(final LineString line) {
    return this.lineEdgeMap.get(line);
  }

  public BoundaryNodeRule getBoundaryNodeRule() {
    return this.boundaryNodeRule;
  }

  public Collection<Node> getBoundaryNodes() {
    if (this.boundaryNodes == null) {
      final NodeMap nodes = getNodeMap();
      this.boundaryNodes = nodes.getBoundaryNodes(this.argIndex);
    }
    return this.boundaryNodes;
  }

  public Geometry getGeometry() {
    return this.geometry;
  }

  public Dimension getGeometryDimension() {
    return this.geometry.getDimension();
  }

  public Point getInvalidPoint() {
    return this.invalidPoint;
  }

  public boolean hasTooFewPoints() {
    return this.hasTooFewPoints;
  }

  /**
   * Adds candidate boundary points using the current {@link BoundaryNodeRule}.
   * This is used to add the boundary
   * points of dim-1 geometries (Curves/MultiCurves).
   */
  private void insertBoundaryPoint(final int argIndex, final Point point) {
    final NodeMap nodes = getNodeMap();
    final Node n = nodes.addNode(point);
    // nodes always have labels
    final Label lbl = n.getLabel();
    // the new point to insert is on a boundary
    int boundaryCount = 1;
    // determine the current location for the point (if any)
    Location loc = Location.NONE;
    loc = lbl.getLocation(argIndex, Position.ON);
    if (loc == Location.BOUNDARY) {
      boundaryCount++;
    }

    // determine the boundary status of the point according to the Boundary
    // Determination Rule
    final Location newLoc = determineBoundary(this.boundaryNodeRule, boundaryCount);
    lbl.setLocation(argIndex, newLoc);
  }

  private void insertPoint(final int argIndex, final Point point, final Location onLocation) {
    final NodeMap nodes = getNodeMap();
    final Node n = nodes.addNode(point);
    final Label lbl = n.getLabel();
    if (lbl == null) {
      n.label = new Label(argIndex, onLocation);
    } else {
      lbl.setLocation(argIndex, onLocation);
    }
  }

  private EdgeSetIntersector newEdgeSetIntersector() {
    return new SimpleMCSweepLineIntersector();
  }
}
