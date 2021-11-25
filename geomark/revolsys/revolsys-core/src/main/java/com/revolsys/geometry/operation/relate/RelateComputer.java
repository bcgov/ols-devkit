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
package com.revolsys.geometry.operation.relate;

/**
 * @version 1.7
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.revolsys.geometry.algorithm.LineIntersector;
import com.revolsys.geometry.algorithm.PointLocator;
import com.revolsys.geometry.algorithm.RobustLineIntersector;
import com.revolsys.geometry.geomgraph.Edge;
import com.revolsys.geometry.geomgraph.EdgeEnd;
import com.revolsys.geometry.geomgraph.EdgeIntersection;
import com.revolsys.geometry.geomgraph.GeometryGraph;
import com.revolsys.geometry.geomgraph.Label;
import com.revolsys.geometry.geomgraph.Node;
import com.revolsys.geometry.geomgraph.NodeMap;
import com.revolsys.geometry.geomgraph.index.SegmentIntersector;
import com.revolsys.geometry.model.Dimension;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.IntersectionMatrix;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.util.Assert;

/**
 * Computes the topological relationship between two Geometries.
 * <p>
 * RelateComputer does not need to build a complete graph structure to compute
 * the IntersectionMatrix.  The relationship between the geometries can
 * be computed by simply examining the labelling of edges incident on each node.
 * <p>
 * RelateComputer does not currently support arbitrary GeometryCollections.
 * This is because GeometryCollections can contain overlapping Polygons.
 * In order to correct compute relate on overlapping Polygons, they
 * would first need to be noded and merged (if not explicitly, at least
 * implicitly).
 *
 * @version 1.7
 */
public class RelateComputer {
  private final GeometryGraph[] arg; // the arg(s) of the operation

  // this intersection matrix will hold the results compute for the relate
  private final ArrayList isolatedEdges = new ArrayList();

  private final LineIntersector li = new RobustLineIntersector();

  private final NodeMap nodes = new NodeMap(new RelateNodeFactory());

  private final PointLocator ptLocator = new PointLocator();

  public RelateComputer(final GeometryGraph[] arg) {
    this.arg = arg;
  }

  /**
   * If the Geometries are disjoint, we need to enter their dimension and
   * boundary dimension in the Ext rows in the IM
   */
  private void computeDisjointIM(final IntersectionMatrix im) {
    final Geometry ga = this.arg[0].getGeometry();
    if (!ga.isEmpty()) {
      im.set(Location.INTERIOR, Location.EXTERIOR, ga.getDimension());
      im.set(Location.BOUNDARY, Location.EXTERIOR, ga.getBoundaryDimension());
    }
    final Geometry gb = this.arg[1].getGeometry();
    if (!gb.isEmpty()) {
      im.set(Location.EXTERIOR, Location.INTERIOR, gb.getDimension());
      im.set(Location.EXTERIOR, Location.BOUNDARY, gb.getBoundaryDimension());
    }
  }

  public IntersectionMatrix computeIM() {
    final Dimension dimension1 = this.arg[0].getGeometryDimension();
    final Dimension dimension2 = this.arg[1].getGeometryDimension();
    final IntersectionMatrix im = new IntersectionMatrix(dimension1, dimension2);
    // since Geometries are finite and embedded in a 2-D space, the EE element
    // must always be 2
    im.set(Location.EXTERIOR, Location.EXTERIOR, Dimension.A);

    // if the Geometries don't overlap there is nothing to do
    if (!this.arg[0].getGeometry().bboxIntersects(this.arg[1].getGeometry())) {
      computeDisjointIM(im);
      return im;
    }
    this.arg[0].computeSelfNodes(this.li, false);
    this.arg[1].computeSelfNodes(this.li, false);

    // compute intersections between edges of the two input geometries
    final SegmentIntersector intersector = this.arg[0].computeEdgeIntersections(this.arg[1],
      this.li, false);
    // System.out.println("computeIM: # segment intersection tests: " +
    // intersector.numTests);
    computeIntersectionNodes(0);
    computeIntersectionNodes(1);
    /**
     * Copy the labelling for the nodes in the parent Geometries.  These override
     * any labels determined by intersections between the geometries.
     */
    copyNodesAndLabels(0);
    copyNodesAndLabels(1);

    // complete the labelling for any nodes which only have a label for a single
    // geometry
    // Debug.addWatch(nodes.find(new PointDouble((double)110, 200)));
    // Debug.printWatch();
    labelIsolatedNodes();
    // Debug.printWatch();

    // If a proper intersection was found, we can set a lower bound on the IM.
    computeProperIntersectionIM(intersector, im);

    /**
     * Now process improper intersections
     * (eg where one or other of the geometries has a vertex at the intersection point)
     * We need to compute the edge graph at all nodes to determine the IM.
     */

    // build EdgeEnds for all intersections
    final EdgeEndBuilder eeBuilder = new EdgeEndBuilder();
    final List<EdgeEnd> ee0 = eeBuilder.computeEdgeEnds(this.arg[0].edges());
    insertEdgeEnds(ee0);
    final List<EdgeEnd> ee1 = eeBuilder.computeEdgeEnds(this.arg[1].edges());
    insertEdgeEnds(ee1);

    // Debug.println("==== NodeList ===");
    // Debug.print(nodes);

    labelNodeEdges();

    /**
     * Compute the labeling for isolated components
     * <br>
     * Isolated components are components that do not touch any other components in the graph.
     * They can be identified by the fact that they will
     * contain labels containing ONLY a single element, the one for their parent geometry.
     * We only need to check components contained in the input graphs, since
     * isolated components will not have been replaced by new components formed by intersections.
     */
    // debugPrintln("Graph A isolated edges - ");
    labelIsolatedEdges(0, 1);
    // debugPrintln("Graph B isolated edges - ");
    labelIsolatedEdges(1, 0);

    // update the IM from all components
    updateIM(im);
    return im;
  }

  /**
   * Insert nodes for all intersections on the edges of a Geometry.
   * Label the created nodes the same as the edge label if they do not already have a label.
   * This allows nodes created by either self-intersections or
   * mutual intersections to be labelled.
   * Endpoint nodes will already be labelled from when they were inserted.
   */
  private void computeIntersectionNodes(final int argIndex) {
    for (final Iterator i = this.arg[argIndex].getEdgeIterator(); i.hasNext();) {
      final Edge e = (Edge)i.next();
      final Location eLoc = e.getLabel().getLocation(argIndex);
      for (final Object element : e.getEdgeIntersectionList()) {
        final EdgeIntersection ei = (EdgeIntersection)element;
        final RelateNode n = (RelateNode)this.nodes.addNode(ei.newPoint2D());
        if (eLoc == Location.BOUNDARY) {
          n.setLabelBoundary(argIndex);
        } else {
          if (n.getLabel().isNull(argIndex)) {
            n.setLabel(argIndex, Location.INTERIOR);
          }
        }
        // Debug.println(n);
      }
    }
  }

  private void computeProperIntersectionIM(final SegmentIntersector intersector,
    final IntersectionMatrix im) {
    // If a proper intersection is found, we can set a lower bound on the IM.
    final Dimension dimA = this.arg[0].getGeometry().getDimension();
    final Dimension dimB = this.arg[1].getGeometry().getDimension();
    final boolean hasProper = intersector.hasProperIntersection();
    final boolean hasProperInterior = intersector.hasProperInteriorIntersection();

    // For Geometry's of dim 0 there can never be proper intersections.

    /**
     * If edge segments of Areas properly intersect, the areas must properly overlap.
     */
    if (dimA.isArea() && dimB.isArea()) {
      if (hasProper) {
        im.setAtLeast("212101212");
      }
    }
    /**
     * If an Line segment properly intersects an edge segment of an Area,
     * it follows that the Interior of the Line intersects the Boundary of the Area.
     * If the intersection is a proper <i>interior</i> intersection, then
     * there is an Interior-Interior intersection too.
     * Note that it does not follow that the Interior of the Line intersects the Exterior
     * of the Area, since there may be another Area component which contains the rest of the Line.
     */
    else if (dimA.isArea() && dimB.isLine()) {
      if (hasProper) {
        im.setAtLeast("FFF0FFFF2");
      }
      if (hasProperInterior) {
        im.setAtLeast("1FFFFF1FF");
      }
    } else if (dimA.isLine() && dimB.isArea()) {
      if (hasProper) {
        im.setAtLeast("F0FFFFFF2");
      }
      if (hasProperInterior) {
        im.setAtLeast("1F1FFFFFF");
      }
    }
    /*
     * If edges of LineStrings properly intersect *in an interior point*, all we
     * can deduce is that the interiors intersect. (We can NOT deduce that the
     * exteriors intersect, since some other segments in the geometries might
     * cover the points in the neighbourhood of the intersection.) It is
     * important that the point be known to be an interior point of both
     * Geometries, since it is possible in a self-intersecting geometry to have
     * a proper intersection on one segment that is also a boundary point of
     * another segment.
     */
    else if (dimA.isLine() && dimB.isLine()) {
      if (hasProperInterior) {
        im.setAtLeast("0FFFFFFFF");
      }
    }
  }

  /**
   * Copy all nodes from an arg geometry into this graph.
   * The node label in the arg geometry overrides any previously computed
   * label for that argIndex.
   * (E.g. a node may be an intersection node with
   * a computed label of BOUNDARY,
   * but in the original arg Geometry it is actually
   * in the interior due to the Boundary Determination Rule)
   */
  private void copyNodesAndLabels(final int argIndex) {
    for (final Iterator i = this.arg[argIndex].getNodeIterator(); i.hasNext();) {
      final Node graphNode = (Node)i.next();
      final Node newNode = this.nodes.addNode(graphNode);
      newNode.setLabel(argIndex, graphNode.getLabel().getLocation(argIndex));
      // node.print(System.out);
    }
  }

  private void insertEdgeEnds(final List ee) {
    for (final Object element : ee) {
      final EdgeEnd e = (EdgeEnd)element;
      this.nodes.add(e);
    }
  }

  /**
   * Label an isolated edge of a graph with its relationship to the target geometry.
   * If the target has dim 2 or 1, the edge can either be in the interior or the exterior.
   * If the target has dim 0, the edge must be in the exterior
   */
  private void labelIsolatedEdge(final Edge edge, final int targetIndex, final Geometry target) {
    // this won't work for GeometryCollections with both dim 2 and 1 geoms
    if (target.getDimension().isGreaterThan(Dimension.P)) {
      // since edge is not in boundary, may not need the full generality of
      // PointLocator?
      // Possibly should use ptInArea locator instead? We probably know here
      // that the edge does not touch the bdy of the target Geometry
      final double x = edge.getX(0);
      final double y = edge.getY(0);
      final Location loc = this.ptLocator.locate(target, x, y);
      edge.getLabel().setAllLocations(targetIndex, loc);
    } else {
      edge.getLabel().setAllLocations(targetIndex, Location.EXTERIOR);
    }
    // System.out.println(e.getLabel());
  }

  /**
   * Processes isolated edges by computing their labelling and adding them
   * to the isolated edges list.
   * Isolated edges are guaranteed not to touch the boundary of the target (since if they
   * did, they would have caused an intersection to be computed and hence would
   * not be isolated)
   */
  private void labelIsolatedEdges(final int thisIndex, final int targetIndex) {
    for (final Iterator ei = this.arg[thisIndex].getEdgeIterator(); ei.hasNext();) {
      final Edge e = (Edge)ei.next();
      if (e.isIsolated()) {
        labelIsolatedEdge(e, targetIndex, this.arg[targetIndex].getGeometry());
        this.isolatedEdges.add(e);
      }
    }
  }

  /**
   * Label an isolated node with its relationship to the target geometry.
   */
  private void labelIsolatedNode(final Node n, final int targetIndex) {
    final Geometry geometry = this.arg[targetIndex].getGeometry();
    final double x = n.getX();
    final double y = n.getY();
    final Location loc = this.ptLocator.locate(geometry, x, y);
    n.getLabel().setAllLocations(targetIndex, loc);
    // debugPrintln(n.getLabel());
  }

  /**
   * Isolated nodes are nodes whose labels are incomplete
   * (e.g. the location for one Geometry is null).
   * This is the case because nodes in one graph which don't intersect
   * nodes in the other are not completely labelled by the initial process
   * of adding nodes to the nodeList.
   * To complete the labelling we need to check for nodes that lie in the
   * interior of edges, and in the interior of areas.
   */
  private void labelIsolatedNodes() {
    for (final Object element : this.nodes) {
      final Node n = (Node)element;
      final Label label = n.getLabel();
      // isolated nodes should always have at least one geometry in their label
      Assert.isTrue(label.getGeometryCount() > 0, "node with empty label found");
      if (n.isIsolated()) {
        if (label.isNull(0)) {
          labelIsolatedNode(n, 0);
        } else {
          labelIsolatedNode(n, 1);
        }
      }
    }
  }

  private void labelNodeEdges() {
    for (final Object element : this.nodes) {
      final RelateNode node = (RelateNode)element;
      node.getEdges().computeLabelling(this.arg);
      // Debug.print(node.getEdges());
      // node.print(System.out);
    }
  }

  /**
   * update the IM with the sum of the IMs for each component
   */
  private void updateIM(final IntersectionMatrix im) {
    // Debug.println(im);
    for (final Object element : this.isolatedEdges) {
      final Edge e = (Edge)element;
      e.updateIM(im);
      // Debug.println(im);
    }
    for (final Object element : this.nodes) {
      final RelateNode node = (RelateNode)element;
      node.updateIM(im);
      // Debug.println(im);
      node.updateIMFromEdges(im);
      // Debug.println(im);
      // node.print(System.out);
    }
  }
}
