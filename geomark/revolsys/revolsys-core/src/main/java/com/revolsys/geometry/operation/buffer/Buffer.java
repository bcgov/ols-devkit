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
package com.revolsys.geometry.operation.buffer;

/**
 * @version 1.7
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.revolsys.geometry.algorithm.CGAlgorithms;
import com.revolsys.geometry.algorithm.CGAlgorithmsDD;
import com.revolsys.geometry.algorithm.LineIntersector;
import com.revolsys.geometry.algorithm.RobustLineIntersector;
import com.revolsys.geometry.geomgraph.DirectedEdge;
import com.revolsys.geometry.geomgraph.Edge;
import com.revolsys.geometry.geomgraph.EdgeList;
import com.revolsys.geometry.geomgraph.Label;
import com.revolsys.geometry.geomgraph.Node;
import com.revolsys.geometry.geomgraph.PlanarGraph;
import com.revolsys.geometry.geomgraph.Position;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.TopologyException;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDouble;
import com.revolsys.geometry.noding.IntersectionAdder;
import com.revolsys.geometry.noding.MCIndexNoder;
import com.revolsys.geometry.noding.NodedSegmentString;
import com.revolsys.geometry.noding.Noder;
import com.revolsys.geometry.noding.ScaledNoder;
import com.revolsys.geometry.noding.SegmentString;
import com.revolsys.geometry.noding.snapround.MCIndexSnapRounder;
import com.revolsys.geometry.operation.overlay.OverlayNodeFactory;
import com.revolsys.geometry.operation.overlay.PolygonBuilder;
import com.revolsys.geometry.operation.union.CascadedPolygonUnion;

//import debug.*;

/**
 * Computes the buffer of a geometry, for both positive and negative buffer distances.
 * <p>
 * In GIS, the positive (or negative) buffer of a geometry is defined as
 * the Minkowski sum (or difference) of the geometry
 * with a circle of radius equal to the absolute value of the buffer distance.
 * In the CAD/CAM world buffers are known as </i>offset curves</i>.
 * In morphological analysis the
 * operation of postive and negative buffering
 * is referred to as <i>erosion</i> and <i>dilation</i>
 * <p>
 * The buffer operation always returns a polygonal result.
 * The negative or zero-distance buffer of lines and points is always an empty {@link Polygon}.
 * <p>
 * Since true buffer curves may contain circular arcs,
 * computed buffer polygons can only be approximations to the true geometry.
 * The user can control the accuracy of the curve approximation by specifying
 * the number of linear segments used to approximate curves.
 * <p>
 * The <b>end cap style</b> of a linear buffer may be specified. The
 * following end cap styles are supported:
 * <ul
 * <li>{@link BufferParameters#CAP_ROUND} - the usual round end caps
 * <li>{@link BufferParameters#CAP_FLAT} - end caps are truncated flat at the line ends
 * <li>{@link BufferParameters#CAP_SQUARE} - end caps are squared off at the buffer distance beyond the line ends
 * </ul>
 * <p>
 *
 * @version 1.7
 */
public class Buffer {

  /**
   * A number of digits of precision which leaves some computational "headroom"
   * for floating point operations.
   *
   * This value should be less than the decimal precision of double-precision values (16).
   */
  private static int MAX_PRECISION_DIGITS = 12;

  /**
   * Comutes the buffer for a geometry for a given buffer distance
   * and accuracy of approximation.
   *
   * @param g the geometry to buffer
   * @param distance the buffer distance
   * @param parameters the buffer parameters to use
   * @return the buffer of the input geometry
   *
   */
  @SuppressWarnings("unchecked")
  public static <G extends Geometry> G buffer(final Geometry geometry, final double distance,
    final BufferParameters parameters) {
    final GeometryFactory geometryFactory = geometry.getGeometryFactory();
    try {
      final MCIndexNoder noder = new MCIndexNoder();
      final LineIntersector li = new RobustLineIntersector();
      noder.setSegmentIntersector(new IntersectionAdder(li));
      return (G)buffer(noder, geometryFactory, geometry, distance, parameters);
    } catch (final RuntimeException e) {
      if (geometryFactory.isFloating()) {
        return (G)bufferReducedPrecision(geometry, distance, parameters);
      } else {
        return (G)bufferFixedPrecision(geometryFactory, geometry, distance, parameters);
      }
    }
  }

  private static Geometry buffer(final Noder noder, final GeometryFactory geometryFactory,
    final Geometry geometry, final double distance, final BufferParameters parameters) {

    final OffsetCurveSetBuilder curveSetBuilder = new OffsetCurveSetBuilder(geometry, distance,
      geometryFactory, parameters);

    final List<NodedSegmentString> curves = curveSetBuilder.getCurves();
    if (curves.size() == 0) {
      return geometryFactory.polygon();
    } else {
      final EdgeList edgeList = new EdgeList();
      computeNodedEdges(noder, edgeList, curves);
      final PlanarGraph graph = new PlanarGraph(new OverlayNodeFactory());
      final List<Edge> edges = edgeList.getEdges();
      graph.addEdges(edges);

      final List<BufferSubgraph> subgraphList = newSubgraphs(graph);
      final PolygonBuilder polyBuilder = new PolygonBuilder(geometryFactory);
      buildSubgraphs(subgraphList, polyBuilder);
      final List<Polygon> polygons = polyBuilder.getPolygons();

      if (polygons.size() == 0) {
        return geometryFactory.polygon().union();
      } else {
        return CascadedPolygonUnion.union(polygons);
      }
    }
  }

  private static Geometry bufferFixedPrecision(final GeometryFactory geometryFactory,
    final Geometry geometry, final double distance, final BufferParameters parameters) {
    final MCIndexSnapRounder rounder = new MCIndexSnapRounder(1.0);
    final double scale = geometryFactory.getScaleXY();
    final Noder noder = new ScaledNoder(rounder, scale);
    return buffer(noder, geometryFactory, geometry, distance, parameters);
  }

  private static Geometry bufferReducedPrecision(final Geometry geometry, final double distance,
    final BufferParameters parameters) {
    TopologyException saveException = null;
    // try and compute with decreasing precision
    for (int precDigits = MAX_PRECISION_DIGITS; precDigits >= 0; precDigits--) {
      try {
        final double sizeBasedScaleFactor = precisionScaleFactor(geometry, distance, precDigits);
        final GeometryFactory precisionModel = geometry.getGeometryFactory()
          .convertScales(sizeBasedScaleFactor, sizeBasedScaleFactor);
        return bufferFixedPrecision(precisionModel, geometry, distance, parameters);
      } catch (final TopologyException e) {

        saveException = e;
        // TODO remove
        // throw e;
      }
    }
    throw saveException;
  }

  /**
   * Completes the building of the input subgraphs by depth-labelling them,
   * and adds them to the PolygonBuilder.
   * The subgraph list must be sorted in rightmost-coordinate order.
   *
   * @param subgraphList the subgraphs to build
   * @param polyBuilder the PolygonBuilder which will build the final polygons
   */
  private static void buildSubgraphs(final List<BufferSubgraph> subgraphList,
    final PolygonBuilder polyBuilder) {
    final List<BufferSubgraph> processedGraphs = new ArrayList<>();
    for (final BufferSubgraph subgraph : subgraphList) {
      final Point p = subgraph.getRightmostCoordinate();
      final int outsideDepth = getDepth(processedGraphs, p);
      subgraph.computeDepth(outsideDepth);
      subgraph.findResultEdges();
      processedGraphs.add(subgraph);
      final List<DirectedEdge> edges = subgraph.getDirectedEdges();
      final List<Node> nodes = subgraph.getNodes();
      polyBuilder.add(edges, nodes);
    }
  }

  private static void computeNodedEdges(final Noder noder, final EdgeList edges,
    final List<NodedSegmentString> segments) {
    noder.computeNodes(segments);
    final Collection<NodedSegmentString> nodedSegments = noder.getNodedSubstrings();
    for (final SegmentString segment : nodedSegments) {
      final int vertexCount = segment.size();
      if (vertexCount > 2 || vertexCount == 2 && !segment.equalsVertex2d(0, 1)) {
        final Label oldLabel = (Label)segment.getData();
        final Label label = new Label(oldLabel);
        final LineString points = segment.getLineString();
        final Edge edge = new Edge(points, label);
        insertUniqueEdge(edges, edge);
      }
    }
  }

  /**
   * Compute the change in depth as an edge is crossed from R to L
   */
  private static int depthDelta(final Label label) {
    final Location lLoc = label.getLocation(0, Position.LEFT);
    final Location rLoc = label.getLocation(0, Position.RIGHT);
    if (lLoc == Location.INTERIOR && rLoc == Location.EXTERIOR) {
      return 1;
    } else if (lLoc == Location.EXTERIOR && rLoc == Location.INTERIOR) {
      return -1;
    }
    return 0;
  }

  /**
   * Finds all non-horizontal segments intersecting the stabbing line.
   * The stabbing line is the ray to the right of stabbingRayLeftPt.
   *
   * @param stabbingRayLeftPt the left-hand origin of the stabbing line
   * @return a List of {@link DepthSegments} intersecting the stabbing line
   */
  private static List<DepthSegment> findStabbedSegments(final Collection<BufferSubgraph> graphs,
    final Point stabbingRayLeftPt) {
    final List<DepthSegment> segments = new ArrayList<>();
    for (final BufferSubgraph graph : graphs) {
      final BoundingBox env = graph.getBoundingBox();
      if (stabbingRayLeftPt.getY() >= env.getMinY() && stabbingRayLeftPt.getY() <= env.getMaxY()) {
        final List<DirectedEdge> edges = graph.getDirectedEdges();
        for (final DirectedEdge edge : edges) {
          if (edge.isForward()) {
            findStabbedSegments(graphs, stabbingRayLeftPt, edge, segments);
          }
        }
      }
    }
    return segments;
  }

  /**
   * Finds all non-horizontal segments intersecting the stabbing line
   * in the input dirEdge.
   * The stabbing line is the ray to the right of stabbingRayLeftPt.
   *
   * @param stabbingRayLeftPt the left-hand origin of the stabbing line
   * @param stabbedSegments the current list of {@link DepthSegments} intersecting the stabbing line
   */
  private static void findStabbedSegments(final Collection<BufferSubgraph> subgraphs,
    final Point stabbingRayLeftPt, final DirectedEdge dirEdge,
    final List<DepthSegment> stabbedSegments) {
    final Edge edge = dirEdge.getEdge();
    for (int i = 0; i < edge.getVertexCount() - 1; i++) {
      final Point p1 = edge.getPoint(i);
      LineSegment seg = new LineSegmentDouble(p1, edge.getPoint(i + 1));
      double y1 = seg.getY(0);
      double y2 = seg.getY(1);
      // ensure segment always points upwards
      if (y1 > y2) {
        seg = seg.reverse();
        y1 = seg.getY(0);
        y2 = seg.getY(1);
      }
      final double x1 = seg.getX(0);
      final double x2 = seg.getX(1);
      // skip segment if it is left of the stabbing line
      final double maxx = Math.max(x1, x2);
      if (maxx < stabbingRayLeftPt.getX()) {
        continue;
      }

      // skip horizontal segments (there will be a non-horizontal one carrying
      // the same depth info
      if (seg.isHorizontal()) {
        continue;
      }

      // skip if segment is above or below stabbing line
      if (stabbingRayLeftPt.getY() < y1 || stabbingRayLeftPt.getY() > y2) {
        continue;
      }

      // skip if stabbing ray is right of the segment
      if (CGAlgorithmsDD.orientationIndex(seg.getP0(), seg.getP1(),
        stabbingRayLeftPt) == CGAlgorithms.RIGHT) {
        continue;
      }

      // stabbing line cuts this segment, so record it
      int depth = dirEdge.getDepth(Position.LEFT);
      // if segment direction was flipped, use RHS depth instead
      if (!seg.getP0().equals(p1)) {
        depth = dirEdge.getDepth(Position.RIGHT);
      }
      final DepthSegment ds = new DepthSegment(seg, depth);
      stabbedSegments.add(ds);
    }
  }

  private static int getDepth(final Collection<BufferSubgraph> subgraphs, final Point p) {
    final List<DepthSegment> stabbedSegments = findStabbedSegments(subgraphs, p);
    // if no segments on stabbing line subgraph must be outside all others.
    if (stabbedSegments.size() == 0) {
      return 0;
    } else {
      Collections.sort(stabbedSegments);
      final DepthSegment ds = stabbedSegments.get(0);
      return ds.getLeftDepth();
    }
  }

  /**
   * Inserted edges are checked to see if an identical edge already exists.
   * If so, the edge is not inserted, but its label is merged
   * with the existing edge.
   */
  private static void insertUniqueEdge(final EdgeList edgeList, final Edge edge) {
    // <FIX> MD 8 Oct 03 speed up identical edge lookup
    // fast lookup
    final Edge existingEdge = edgeList.findEqualEdge(edge);

    // If an identical edge already exists, simply update its label
    if (existingEdge != null) {
      final Label existingLabel = existingEdge.getLabel();

      Label labelToMerge = edge.getLabel();
      // check if new edge is in reverse direction to existing edge
      // if so, must flip the label before merging it
      if (!existingEdge.isPointwiseEqual(edge)) {
        labelToMerge = new Label(edge.getLabel());
        labelToMerge.flip();
      }
      existingLabel.merge(labelToMerge);

      // compute new depth delta of sum of edges
      final int mergeDelta = depthDelta(labelToMerge);
      final int existingDelta = existingEdge.getDepthDelta();
      final int newDelta = existingDelta + mergeDelta;
      existingEdge.setDepthDelta(newDelta);
    } else { // no matching existing edge was found
      // add this new edge to the list of edges in this graph
      // e.setName(name + edges.size());
      edgeList.add(edge);
      edge.setDepthDelta(depthDelta(edge.getLabel()));
    }
  }

  private static List<BufferSubgraph> newSubgraphs(final PlanarGraph graph) {
    final List<BufferSubgraph> subgraphList = new ArrayList<>();
    for (final Node node : graph.getNodes()) {
      if (!node.isVisited()) {
        final BufferSubgraph subgraph = new BufferSubgraph();
        subgraph.newNode(node);
        subgraphList.add(subgraph);
      }
    }
    /**
     * Sort the subgraphs in descending order of their rightmost coordinate.
     * This ensures that when the Polygons for the subgraphs are built,
     * subgraphs for shells will have been built before the subgraphs for
     * any holes they contain.
     */
    Collections.sort(subgraphList, Collections.reverseOrder());
    return subgraphList;
  }

  /**
   * Compute a scale factor to limit the precision of
   * a given combination of Geometry and buffer distance.
   * The scale factor is determined by
   * the number of digits of precision in the (geometry + buffer distance),
   * limited by the supplied <code>maxPrecisionDigits</code> value.
   * <p>
   * The scale factor is based on the absolute magnitude of the (geometry + buffer distance).
   * since this determines the number of digits of precision which must be handled.
   *
   * @param geometry the Geometry being buffered
   * @param distance the buffer distance
   * @param maxPrecisionDigits the max # of digits that should be allowed by
   *          the precision determined by the computed scale factor
   *
   * @return a scale factor for the buffer computation
   */
  private static double precisionScaleFactor(final Geometry geometry, final double distance,
    final int maxPrecisionDigits) {
    final BoundingBox boundingBox = geometry.getBoundingBox();
    final double minX = Math.abs(boundingBox.getMinX());
    final double maxX = Math.abs(boundingBox.getMaxX());
    final double minY = Math.abs(boundingBox.getMinY());
    final double maxY = Math.abs(boundingBox.getMaxY());

    double envMax = minX;
    if (maxX > envMax) {
      envMax = maxX;
    }
    if (minY > envMax) {
      envMax = minY;
    }
    if (maxY > envMax) {
      envMax = maxY;
    }
    final double expandByDistance = distance > 0.0 ? distance : 0.0;
    final double bufEnvMax = envMax + 2 * expandByDistance;

    // the smallest power of 10 greater than the buffer envelope
    final int bufEnvPrecisionDigits = (int)(Math.log(bufEnvMax) / Math.log(10) + 1.0);
    final int minUnitLog10 = maxPrecisionDigits - bufEnvPrecisionDigits;

    final double scaleFactor = Math.pow(10.0, minUnitLog10);
    return scaleFactor;
  }

}
