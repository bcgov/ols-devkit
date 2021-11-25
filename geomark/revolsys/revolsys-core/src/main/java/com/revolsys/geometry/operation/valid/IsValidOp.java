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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.geometry.algorithm.LineIntersector;
import com.revolsys.geometry.algorithm.MCPointInRing;
import com.revolsys.geometry.algorithm.PointInRing;
import com.revolsys.geometry.algorithm.RobustLineIntersector;
import com.revolsys.geometry.geomgraph.Edge;
import com.revolsys.geometry.geomgraph.EdgeIntersection;
import com.revolsys.geometry.geomgraph.EdgeIntersectionList;
import com.revolsys.geometry.geomgraph.GeometryGraph;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.util.Assert;
import com.revolsys.util.Strings;

/**
 * Implements the algorithms required to compute the <code>isValid()</code> method
 * for {@link Geometry}s.
 * See the documentation for the various geometry types for a specification of validity.
 *
 * @version 1.7
 */
public class IsValidOp {
  /**
   * Find a point from the list of testCoords
   * that is NOT a node in the edge for the list of searchCoords
   *
   * @return the point found, or <code>null</code> if none found
   */
  public static Point findPtNotNode(final LineString testLine, final LinearRing searchRing,
    final GeometryGraph graph) {
    // find edge corresponding to searchRing.
    final Edge searchEdge = graph.findEdge(searchRing);
    // find a point in the testCoords which is not a node of the searchRing
    final EdgeIntersectionList eiList = searchEdge.getEdgeIntersectionList();
    // somewhat inefficient - is there a better way? (Use a node map, for
    // instance?)
    final int vertexCount = testLine.getVertexCount();
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      final double x = testLine.getX(vertexIndex);
      final double y = testLine.getY(vertexIndex);
      if (!eiList.isIntersection(x, y)) {
        return new PointDoubleXY(x, y);
      }
    }
    return null;
  }

  /**
   * Tests whether a {@link Geometry} is valid.
   * @param geom the Geometry to test
   * @return true if the geometry is valid
   */
  public static boolean isValid(final Geometry geom) {
    final IsValidOp isValidOp = new IsValidOp(geom);
    return isValidOp.isValid();
  }

  private final List<GeometryValidationError> errors = new ArrayList<>();

  private final Geometry geometry; // the base Geometry to be validated

  /**
   * If the following condition is TRUE JTS will validate inverted shells and exverted holes
   * (the ESRI SDE model)
   */
  private boolean isSelfTouchingRingFormingHoleValid = false;

  private boolean shortCircuit = true;

  public IsValidOp(final Geometry geometry) {
    this.geometry = geometry;
  }

  public IsValidOp(final Geometry geometry, final boolean shortCircuit) {
    this.geometry = geometry;
    this.shortCircuit = shortCircuit;
  }

  private void addError(final GeometryValidationError error) {
    this.errors.add(error);
  }

  private boolean checkClosedRing(final LinearRing ring) {
    if (ring == null) {
      return false;
    } else if (ring.isClosed()) {
      return true;
    } else {
      Point point = null;
      if (ring.getVertexCount() >= 1) {
        point = ring.getPoint(0);
      }
      addError(new TopologyValidationError(TopologyValidationError.RING_NOT_CLOSED, point));
      return false;
    }
  }

  private boolean checkClosedRings(final Polygon poly) {
    boolean valid = checkClosedRing(poly.getShell());
    if (isErrorReturn()) {
      return false;
    }
    for (int i = 0; i < poly.getHoleCount(); i++) {
      valid &= checkClosedRing(poly.getHole(i));
      if (isErrorReturn()) {
        return false;
      }
    }
    return valid;
  }

  private boolean checkConnectedInteriors(final GeometryGraph graph) {
    final ConnectedInteriorTester cit = new ConnectedInteriorTester(graph);
    if (cit.isInteriorsConnected()) {
      return true;
    } else {
      addError(new TopologyValidationError(TopologyValidationError.DISCONNECTED_INTERIOR,
        cit.getCoordinate()));
      return false;
    }
  }

  /**
   * Checks that the arrangement of edges in a polygonal geometry graph
   * forms a consistent area.
   *
   * @param graph
   *
   * @see ConsistentAreaTester
   */
  private boolean checkConsistentArea(final GeometryGraph graph) {
    final ConsistentAreaTester cat = new ConsistentAreaTester(graph);
    final boolean isValidArea = cat.isNodeConsistentArea();
    if (!isValidArea) {
      addError(new TopologyValidationError(TopologyValidationError.SELF_INTERSECTION,
        cat.getInvalidPoint()));
      return false;
    } else if (cat.hasDuplicateRings()) {
      addError(new TopologyValidationError(TopologyValidationError.DUPLICATE_RINGS,
        cat.getInvalidPoint()));
      return false;
    } else {
      return true;
    }
  }

  /**
   * Tests that each hole is inside the polygon shell.
   * This routine assumes that the holes have previously been tested
   * to ensure that all vertices lie on the shell oon the same side of it
   * (i.e that the hole rings do not cross the shell ring).
   * In other words, this test is only correct if the ConsistentArea test is passed first.
   * Given this, a simple point-in-polygon test of a single point in the hole can be used,
   * provided the point is chosen such that it does not lie on the shell.
   *
   * @param polygon the polygon to be tested for hole inclusion
   * @param graph a GeometryGraph incorporating the polygon
   */
  private boolean checkHolesInShell(final Polygon polygon, final GeometryGraph graph) {
    boolean valid = true;
    final LinearRing shell = polygon.getShell();

    final PointInRing pir = new MCPointInRing(shell);
    for (final LinearRing hole : polygon.holes()) {
      final Point holePt = findPtNotNode(hole, shell, graph);
      /**
       * If no non-node hole vertex can be found, the hole must
       * split the polygon into disconnected interiors.
       * This will be caught by a subsequent check.
       */
      if (holePt != null) {
        final boolean outside = !pir.isInside(holePt);
        if (outside) {
          valid = false;
          addError(new TopologyValidationError(TopologyValidationError.HOLE_OUTSIDE_SHELL, holePt));
          if (isErrorReturn()) {
            return false;
          }
        }
      }
    }
    return valid;
  }

  /**
   * Tests that no hole is nested inside another hole.
   * This routine assumes that the holes are disjoint.
   * To ensure this, holes have previously been tested
   * to ensure that:
   * <ul>
   * <li>they do not partially overlap
   *      (checked by <code>checkRelateConsistency</code>)
   * <li>they are not identical
   *      (checked by <code>checkRelateConsistency</code>)
   * </ul>
   */
  private boolean checkHolesNotNested(final Polygon p, final GeometryGraph graph) {
    final IndexedNestedRingTester nestedTester = new IndexedNestedRingTester(graph);

    for (int i = 0; i < p.getHoleCount(); i++) {
      final LinearRing innerHole = p.getHole(i);
      nestedTester.add(innerHole);
    }
    final boolean isNonNested = nestedTester.isNonNested();
    if (isNonNested) {
      return true;
    } else {
      addError(new TopologyValidationError(TopologyValidationError.NESTED_HOLES,
        nestedTester.getNestedPoint()));
      return false;
    }
  }

  private boolean checkInvalidCoordinates(final Geometry geometry) {
    boolean valid = true;
    for (final Vertex vertex : geometry.vertices()) {
      for (int axisIndex = 0; axisIndex < 2; axisIndex++) {
        final double value = vertex.getCoordinate(axisIndex);
        if (Double.isNaN(value)) {
          addError(new CoordinateNaNError(vertex, axisIndex));
          if (isErrorReturn()) {
            return false;
          } else {
            valid = false;
          }
        } else if (Double.isInfinite(value)) {
          addError(new CoordinateInfiniteError(vertex, axisIndex));
          if (isErrorReturn()) {
            return false;
          } else {
            valid = false;
          }
        }
      }
    }
    return valid;
  }

  /**
   * Check that a ring does not self-intersect, except at its endpoints.
   * Algorithm is to count the number of times each node along edge occurs.
   * If any occur more than once, that must be a self-intersection.
   */
  private boolean checkNoSelfIntersectingRing(final EdgeIntersectionList eiList) {
    boolean valid = true;
    final Set<Point> nodeSet = new TreeSet<>();
    boolean isFirst = true;
    for (final EdgeIntersection ei : eiList) {
      if (isFirst) {
        isFirst = false;
      } else if (nodeSet.contains(ei)) {
        valid = false;
        addError(new TopologyValidationError(TopologyValidationError.RING_SELF_INTERSECTION,
          ei.newPoint2D()));
        if (isErrorReturn()) {
          return false;
        }
      } else {
        nodeSet.add(ei.newPoint2D());
      }
    }
    return valid;
  }

  /**
   * Check that there is no ring which self-intersects (except of course at its endpoints).
   * This is required by OGC topology rules (but not by other models
   * such as ESRI SDE, which allow inverted shells and exverted holes).
   *
   * @param graph the topology graph of the geometry
   */
  private boolean checkNoSelfIntersectingRings(final GeometryGraph graph) {
    boolean valid = true;
    for (final Edge edge : graph.edges()) {
      final EdgeIntersectionList edgeIntersectionList = edge.getEdgeIntersectionList();
      valid &= checkNoSelfIntersectingRing(edgeIntersectionList);
      if (isErrorReturn()) {
        return false;
      }
    }
    return valid;
  }

  /**
   * This routine checks to see if a shell is properly contained in a hole.
   * It assumes that the edges of the shell and hole do not
   * properly intersect.
   *
   * @return <code>null</code> if the shell is properly contained, or
   *   a Point which is not inside the hole if it is not
   *
   */
  private Point checkShellInsideHole(final LinearRing shell, final LinearRing hole,
    final GeometryGraph graph) {
    // TODO: improve performance of this - by sorting LineStrings for instance?
    final Point shellPt = findPtNotNode(shell, hole, graph);
    // if point is on shell but not hole, check that the shell is inside the
    // hole
    if (shellPt != null) {
      final boolean insideHole = hole.isPointInRing(shellPt);
      if (!insideHole) {
        return shellPt;
      }
    }
    final Point holePt = findPtNotNode(hole, shell, graph);
    // if point is on hole but not shell, check that the hole is outside the
    // shell
    if (holePt != null) {
      final boolean insideShell = shell.isPointInRing(holePt);
      if (insideShell) {
        return holePt;
      }
      return null;
    }
    Assert.shouldNeverReachHere("points in shell and hole appear to be equal");
    return null;
  }

  /**
   * Check if a shell is incorrectly nested within a polygon.  This is the case
   * if the shell is inside the polygon shell, but not inside a polygon hole.
   * (If the shell is inside a polygon hole, the nesting is valid.)
   * <p>
   * The algorithm used relies on the fact that the rings must be properly contained.
   * E.g. they cannot partially overlap (this has been previously checked by
   * <code>checkRelateConsistency</code> )
   */
  private boolean checkShellNotNested(final LinearRing shell, final Polygon polygon,
    final GeometryGraph graph) {
    // test if shell is inside polygon shell
    final LinearRing polyShell = polygon.getShell();
    final Point shellPt = findPtNotNode(shell, polyShell, graph);
    // if no point could be found, we can assume that the shell is outside the
    // polygon
    if (shellPt == null) {
      return true;
    } else {
      final boolean insidePolyShell = polyShell.isPointInRing(shellPt);
      if (!insidePolyShell) {
        return true;
      }

      // if no holes, this is an error!
      if (polygon.getHoleCount() <= 0) {
        addError(new TopologyValidationError(TopologyValidationError.NESTED_SHELLS, shellPt));
        return false;
      }

      /**
       * Check if the shell is inside one of the holes.
       * This is the case if one of the calls to checkShellInsideHole
       * returns a null coordinate.
       * Otherwise, the shell is not properly contained in a hole, which is an error.
       */
      Point badNestedPt = null;
      for (int i = 0; i < polygon.getHoleCount(); i++) {
        final LinearRing hole = polygon.getHole(i);
        badNestedPt = checkShellInsideHole(shell, hole, graph);
        if (badNestedPt == null) {
          return true;
        }
      }
      addError(new TopologyValidationError(TopologyValidationError.NESTED_SHELLS, badNestedPt));
      return false;
    }
  }

  /**
   * Tests that no element polygon is wholly in the interior of another element polygon.
   * <p>
   * Preconditions:
   * <ul>
   * <li>shells do not partially overlap
   * <li>shells do not touch along an edge
   * <li>no duplicate rings exist
   * </ul>
   * This routine relies on the fact that while polygon shells may touch at one or
   * more vertices, they cannot touch at ALL vertices.
   */
  private boolean checkShellsNotNested(final Polygonal polygonal, final GeometryGraph graph) {
    boolean valid = true;
    final List<Polygon> polygons = polygonal.getPolygons();
    final int polygonCount = polygons.size();
    for (int i = 0; i < polygonCount; i++) {
      final Polygon polygon1 = polygons.get(i);
      final LinearRing shell = polygon1.getShell();
      for (int j = 0; j < polygonCount; j++) {
        if (i != j) {
          final Polygon polygon2 = polygons.get(j);
          valid &= checkShellNotNested(shell, polygon2, graph);
          if (isErrorReturn()) {
            return false;
          }
        }
      }
    }
    return valid;
  }

  private boolean checkTooFewPoints(final GeometryGraph graph) {
    if (graph.hasTooFewPoints()) {
      addError(new TopologyValidationError(TopologyValidationError.TOO_FEW_POINTS,
        graph.getInvalidPoint()));
      return false;
    } else {
      return true;
    }
  }

  private boolean checkTooFewVertices(final LineString line, final int minVertexCount) {
    int edgeCount = 0;

    final int vertexCount = line.getVertexCount();
    if (vertexCount > 0) {
      double x1 = line.getX(0);
      double y1 = line.getY(0);
      for (int vertexIndex = 1; vertexIndex < vertexCount; vertexIndex++) {
        final double x2 = line.getX(vertexIndex);
        final double y2 = line.getY(vertexIndex);
        if (x1 != x2 || y1 != y2) {
          edgeCount++;
        }
        x1 = x2;
        y1 = y2;
      }
    }

    if (edgeCount < minVertexCount - 1) {
      addError(
        new TopologyValidationError(TopologyValidationError.TOO_FEW_POINTS, line.getPoint(0)));
      return false;
    } else {
      return true;
    }
  }

  /**
   * Check geometries to see if they are valid.
   *
   * <ul>
   *   <li>Empty geometries are valid</li>
   *   <li>Geometries with x,y coordinates that are NaN or Infinity are invalid. Any other validation will not be performed for those geometries.</li>
   *   <li>Other validation checks are performed based on the type of geometry.</li>
   * </ul>
   * @param geometry
   * @return If the geometry is valid.
   */
  private boolean checkValidGeometry(final Geometry geometry) {
    if (geometry.isEmpty()) {
      return true;
    } else if (!checkInvalidCoordinates(geometry)) {
      return false;
    } else if (geometry instanceof Point) {
      return true;
    } else if (geometry instanceof Punctual) {
      return true;
    } else if (geometry instanceof LinearRing) {
      return checkValidLinearRing((LinearRing)geometry);
    } else if (geometry instanceof LineString) {
      return checkValidLineString((LineString)geometry);
    } else if (geometry instanceof Lineal) {
      return checkValidMultiLineString((Lineal)geometry);
    } else if (geometry instanceof Polygon) {
      return checkValidPolygon((Polygon)geometry);
    } else if (geometry instanceof Polygonal) {
      return checkValidMultiPolygon((Polygonal)geometry);
    } else if (geometry.isGeometryCollection()) {
      return checkValidGeometryCollection(geometry);
    } else {
      throw new UnsupportedOperationException(geometry.getClass().getName());
    }
  }

  private boolean checkValidGeometryCollection(final Geometry geometryCollection) {
    boolean valid = true;
    for (final Geometry geometry : geometryCollection.geometries()) {
      valid &= checkValidGeometry(geometry);
      if (isErrorReturn()) {
        return false;
      }
    }
    return valid;
  }

  /**
   * Checks validity of a LinearRing.
   */
  private boolean checkValidLinearRing(final LinearRing ring) {
    boolean valid = true;
    if (checkTooFewVertices(ring, 4)) {
      valid &= checkClosedRing(ring);
      if (isErrorReturn()) {
        return false;
      }

      final GeometryGraph graph = new GeometryGraph(0, ring);
      final LineIntersector li = new RobustLineIntersector();
      graph.computeSelfNodes(li, true);
      valid &= checkNoSelfIntersectingRings(graph);
      return valid;
    }
    return false;
  }

  /**
   * {@link LineString} geometries require a minimum of 2 vertices.
   */
  private boolean checkValidLineString(final LineString line) {
    return checkTooFewVertices(line, 2);
  }

  private boolean checkValidMultiLineString(final Lineal lineal) {
    boolean valid = true;
    for (final LineString lineString : lineal.lineStrings()) {
      valid &= checkValidLineString(lineString);
      if (isErrorReturn()) {
        return false;
      }
    }
    return valid;
  }

  private boolean checkValidMultiPolygon(final Polygonal polygonal) {
    boolean valid = true;
    for (final Polygon polygon : polygonal.polygons()) {
      valid &= checkClosedRings(polygon);
      if (isErrorReturn()) {
        return false;
      }
    }

    final GeometryGraph graph = new GeometryGraph(0, polygonal);

    valid &= checkTooFewPoints(graph);
    if (isErrorReturn()) {
      return false;
    }
    valid &= checkConsistentArea(graph);
    if (isErrorReturn()) {
      return false;
    }
    if (!this.isSelfTouchingRingFormingHoleValid) {
      valid &= checkNoSelfIntersectingRings(graph);
      if (isErrorReturn()) {
        return false;
      }
    }
    for (final Polygon polygon : polygonal.getPolygons()) {
      valid &= checkHolesInShell(polygon, graph);
      if (isErrorReturn()) {
        return false;
      }
    }
    for (final Polygon polygon : polygonal.getPolygons()) {
      valid &= checkHolesNotNested(polygon, graph);
      if (isErrorReturn()) {
        return false;
      }
    }
    valid &= checkShellsNotNested(polygonal, graph);
    if (isErrorReturn()) {
      return false;
    }
    valid &= checkConnectedInteriors(graph);
    return valid;
  }

  /**
   * Checks the validity of a polygon.
   * Sets the validErr flag.
   */
  private boolean checkValidPolygon(final Polygon g) {
    boolean valid = true;
    valid &= checkClosedRings(g);
    if (isErrorReturn()) {
      return false;
    }

    try {
      final GeometryGraph graph = new GeometryGraph(0, g);

      valid &= checkTooFewPoints(graph);
      if (isErrorReturn()) {
        return false;
      }
      valid &= checkConsistentArea(graph);
      if (isErrorReturn()) {
        return false;
      }

      if (!this.isSelfTouchingRingFormingHoleValid) {
        valid &= checkNoSelfIntersectingRings(graph);
        if (isErrorReturn()) {
          return false;
        }
      }
      valid &= checkHolesInShell(g, graph);
      if (isErrorReturn()) {
        return false;
      }
      // SLOWcheckHolesNotNested(g);
      valid &= checkHolesNotNested(g, graph);
      if (isErrorReturn()) {
        return false;
      }
      valid &= checkConnectedInteriors(graph);
      return valid;
    } catch (final IllegalArgumentException e) {
      return false;
    }
  }

  public List<GeometryValidationError> getErrors() {
    return this.errors;
  }

  /**
   * Computes the validity of the geometry,
   * and if not valid returns the validation error for the geometry,
   * or null if the geometry is valid.
   *
   * @return the validation error, if the geometry is invalid
   * or null if the geometry is valid
   */
  public GeometryValidationError getValidationError() {
    if (isValid()) {
      return null;
    } else {
      return this.errors.get(0);
    }
  }

  public boolean hasError() {
    if (this.errors.isEmpty()) {
      return false;
    } else {
      return true;
    }
  }

  private boolean isErrorReturn() {
    return this.shortCircuit && hasError();
  }

  /**
   * Computes the validity of the geometry,
   * and returns <tt>true</tt> if it is valid.
   *
   * @return true if the geometry is valid
   */
  public boolean isValid() {
    this.errors.clear();
    return checkValidGeometry(this.geometry);
  }

  /**
   * Sets whether polygons using <b>Self-Touching Rings</b> to form
   * holes are reported as valid.
   * If this flag is set, the following Self-Touching conditions
   * are treated as being valid:
   * <ul>
   * <li>the shell ring self-touches to Construct a new hole touching the shell
   * <li>a hole ring self-touches to create two holes touching at a point
   * </ul>
   * <p>
   * The default (following the OGC SFS standard)
   * is that this condition is <b>not</b> valid (<code>false</code>).
   * <p>
   * This does not affect whether Self-Touching Rings
   * disconnecting the polygon interior are considered valid
   * (these are considered to be <b>invalid</b> under the SFS, and many other
   * spatial models as well).
   * This includes "bow-tie" shells,
   * which self-touch at a single point causing the interior to
   * be disconnected,
   * and "C-shaped" holes which self-touch at a single point causing an island to be formed.
   *
   * @param isValid states whether geometry with this condition is valid
   */
  public void setSelfTouchingRingFormingHoleValid(final boolean isValid) {
    this.isSelfTouchingRingFormingHoleValid = isValid;
  }

  @Override
  public String toString() {
    if (isErrorReturn()) {
      return Strings.toString("\n", this.errors);
    } else {
      return "Valid";
    }
  }

}
