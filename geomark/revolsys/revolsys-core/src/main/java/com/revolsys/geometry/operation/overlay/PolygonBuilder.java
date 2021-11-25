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
package com.revolsys.geometry.operation.overlay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.revolsys.geometry.geomgraph.DirectedEdge;
import com.revolsys.geometry.geomgraph.EdgeRing;
import com.revolsys.geometry.geomgraph.Node;
import com.revolsys.geometry.geomgraph.PlanarGraph;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.TopologyException;
import com.revolsys.geometry.util.Assert;

/**
 * Forms {@link Polygon}s out of a graph of {@link DirectedEdge}s.
 * The edges to use are marked as being in the result Area.
 * <p>
 *
 * @version 1.7
 */
public class PolygonBuilder {

  private final GeometryFactory geometryFactory;

  // private List dirEdgeList;
  // private NodeMap nodes;
  private final List<EdgeRing> shellList = new ArrayList<>();

  public PolygonBuilder(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  /**
   * Add a set of edges and nodes, which form a graph.
   * The graph is assumed to contain one or more polygons,
   * possibly with holes.
   */
  public void add(final Collection<DirectedEdge> dirEdges, final Collection<Node> nodes) {
    PlanarGraph.linkResultDirectedEdges(nodes);
    final List<MaximalEdgeRing> maxEdgeRings = buildMaximalEdgeRings(dirEdges);
    final List<EdgeRing> freeHoleList = new ArrayList<>();
    final List<MaximalEdgeRing> edgeRings = buildMinimalEdgeRings(maxEdgeRings, this.shellList,
      freeHoleList);
    sortShellsAndHoles(edgeRings, this.shellList, freeHoleList);
    placeFreeHoles(this.shellList, freeHoleList);
  }

  /**
   * Add a complete graph.
   * The graph is assumed to contain one or more polygons,
   * possibly with holes.
   */
  public void add(final PlanarGraph graph) {
    add(graph.getEdgeEnds(), graph.getNodes());
  }

  /**
   * for all DirectedEdges in result, form them into MaximalEdgeRings
   */
  private List<MaximalEdgeRing> buildMaximalEdgeRings(final Collection<DirectedEdge> dirEdges) {
    final List<MaximalEdgeRing> maxEdgeRings = new ArrayList<>();
    for (final DirectedEdge de : dirEdges) {
      if (de.isInResult() && de.getLabel().isArea()) {
        // if this edge has not yet been processed
        if (de.getEdgeRing() == null) {
          final MaximalEdgeRing er = new MaximalEdgeRing(de, this.geometryFactory);
          maxEdgeRings.add(er);
          er.setInResult();
        }
      }
    }
    return maxEdgeRings;
  }

  private List<MaximalEdgeRing> buildMinimalEdgeRings(final List<MaximalEdgeRing> maxEdgeRings,
    final List<EdgeRing> shellList, final List<EdgeRing> freeHoleList) {
    final List<MaximalEdgeRing> edgeRings = new ArrayList<>();
    for (final MaximalEdgeRing er : maxEdgeRings) {
      if (er.getMaxNodeDegree() > 2) {
        er.linkDirectedEdgesForMinimalEdgeRings();
        final List<MinimalEdgeRing> minEdgeRings = er.buildMinimalRings();
        // at this point we can go ahead and attempt to place holes, if this
        // EdgeRing is a polygon
        final EdgeRing shell = findShell(minEdgeRings);
        if (shell != null) {
          placePolygonHoles(shell, minEdgeRings);
          shellList.add(shell);
        } else {
          freeHoleList.addAll(minEdgeRings);
        }
      } else {
        edgeRings.add(er);
      }
    }
    return edgeRings;
  }

  /**
   * Checks the current set of shells (with their associated holes) to
   * see if any of them contain the point.
   */
  public boolean containsPoint(final Point point) {
    for (final EdgeRing shell : this.shellList) {
      if (shell.containsPoint(point)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Find the innermost enclosing shell EdgeRing containing the argument EdgeRing, if any.
   * The innermost enclosing ring is the <i>smallest</i> enclosing ring.
   * The algorithm used depends on the fact that:
   * <br>
   *  ring A contains ring B iff envelope(ring A) contains envelope(ring B)
   * <br>
   * This routine is only safe to use if the chosen point of the hole
   * is known to be properly contained in a shell
   * (which is guaranteed to be the case if the hole does not touch its shell)
   *
   * @return containing EdgeRing, if there is one
   * or null if no containing EdgeRing is found
   */
  private EdgeRing findEdgeRingContaining(final EdgeRing testEr, final List<EdgeRing> shellList) {
    final LinearRing testRing = testEr.getLinearRing();
    final double testX = testRing.getX(0);
    final double testY = testRing.getY(0);

    EdgeRing minShell = null;
    for (final EdgeRing tryShell : shellList) {
      final LinearRing tryRing = tryShell.getLinearRing();
      boolean isContained = false;
      if (tryRing.bboxCovers(testRing) && tryRing.isPointInRing(testX, testY)) {
        isContained = true;
      }
      // check if this new containing ring is smaller than the current minimum
      // ring
      if (isContained) {
        if (minShell == null || minShell.bboxCovers(tryRing)) {
          minShell = tryShell;
        }
      }
    }
    return minShell;
  }

  /**
   * This method takes a list of MinimalEdgeRings derived from a MaximalEdgeRing,
   * and tests whether they form a Polygon.  This is the case if there is a single shell
   * in the list.  In this case the shell is returned.
   * The other possibility is that they are a series of connected holes, in which case
   * no shell is returned.
   *
   * @return the shell EdgeRing, if there is one
   * or null, if all the rings are holes
   */
  private EdgeRing findShell(final List<MinimalEdgeRing> minEdgeRings) {
    int shellCount = 0;
    EdgeRing shell = null;
    for (final MinimalEdgeRing er : minEdgeRings) {
      if (!er.isHole()) {
        shell = er;
        shellCount++;
      }
    }
    Assert.isTrue(shellCount <= 1, "found two shells in MinimalEdgeRing list");
    return shell;
  }

  public List<Polygon> getPolygons() {
    final List<Polygon> polygons = new ArrayList<>();
    for (final EdgeRing edgeRing : this.shellList) {
      final Polygon polygon = edgeRing.toPolygon(this.geometryFactory);
      polygons.add(polygon);
    }
    return polygons;
  }

  /**
   * This method determines finds a containing shell for all holes
   * which have not yet been assigned to a shell.
   * These "free" holes should
   * all be <b>properly</b> contained in their parent shells, so it is safe to use the
   * <code>findEdgeRingContaining</code> method.
   * (This is the case because any holes which are NOT
   * properly contained (i.e. are connected to their
   * parent shell) would have formed part of a MaximalEdgeRing
   * and been handled in a previous step).
   *
   * @throws TopologyException if a hole cannot be assigned to a shell
   */
  private void placeFreeHoles(final List<EdgeRing> shellList, final List<EdgeRing> freeHoleList) {
    for (final EdgeRing hole : freeHoleList) {
      // only place this hole if it doesn't yet have a shell
      if (hole.getShell() == null) {
        final EdgeRing shell = findEdgeRingContaining(hole, shellList);
        if (shell == null) {
          throw new TopologyException("unable to assign hole to a shell", hole.getFirstPoint());
        } else {
          hole.setShell(shell);
        }
      }
    }
  }

  /**
   * This method assigns the holes for a Polygon (formed from a list of
   * MinimalEdgeRings) to its shell.
   * Determining the holes for a MinimalEdgeRing polygon serves two purposes:
   * <ul>
   * <li>it is faster than using a point-in-polygon check later on.
   * <li>it ensures correctness, since if the PIP test was used the point
   * chosen might lie on the shell, which might return an incorrect result from the
   * PIP test
   * </ul>
   */
  private void placePolygonHoles(final EdgeRing shell, final List<MinimalEdgeRing> minEdgeRings) {
    for (final MinimalEdgeRing er : minEdgeRings) {
      if (er.isHole()) {
        er.setShell(shell);
      }
    }
  }

  /**
   * For all rings in the input list,
   * determine whether the ring is a shell or a hole
   * and add it to the appropriate list.
   * Due to the way the DirectedEdges were linked,
   * a ring is a shell if it is oriented CW, a hole otherwise.
   */
  private void sortShellsAndHoles(final List<MaximalEdgeRing> edgeRings,
    final List<EdgeRing> shellList, final List<EdgeRing> freeHoleList) {
    for (final MaximalEdgeRing edgeRing : edgeRings) {
      if (edgeRing.isHole()) {
        freeHoleList.add(edgeRing);
      } else {
        shellList.add(edgeRing);
      }
    }
  }

}
