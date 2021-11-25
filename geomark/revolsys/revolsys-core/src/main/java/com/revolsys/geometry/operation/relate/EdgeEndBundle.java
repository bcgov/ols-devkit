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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.revolsys.geometry.algorithm.BoundaryNodeRule;
import com.revolsys.geometry.geomgraph.Edge;
import com.revolsys.geometry.geomgraph.EdgeEnd;
import com.revolsys.geometry.geomgraph.GeometryGraph;
import com.revolsys.geometry.geomgraph.Label;
import com.revolsys.geometry.geomgraph.Position;
import com.revolsys.geometry.model.IntersectionMatrix;
import com.revolsys.geometry.model.Location;

/**
 * A collection of {@link EdgeEnd}s which obey the following invariant:
 * They originate at the same node and have the same direction.
 *
 * @version 1.7
 */
public class EdgeEndBundle extends EdgeEnd implements Iterable<EdgeEnd> {
  // private BoundaryNodeRule boundaryNodeRule;
  private final List<EdgeEnd> edgeEnds = new ArrayList<>();

  public EdgeEndBundle(final BoundaryNodeRule boundaryNodeRule, final EdgeEnd e) {
    super(e.getEdge(), e.getX1(), e.getY1(), e.getX2(), e.getY2(), new Label(e.getLabel()));
    insert(e);
    /*
     * if (boundaryNodeRule != null) this.boundaryNodeRule = boundaryNodeRule;
     * else boundaryNodeRule = BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE;
     */
  }

  public EdgeEndBundle(final EdgeEnd e) {
    this(null, e);
  }

  /**
   * This computes the overall edge label for the set of
   * edges in this EdgeStubBundle.  It essentially merges
   * the ON and side labels for each edge.  These labels must be compatible
   */
  @Override
  public void computeLabel(final BoundaryNodeRule boundaryNodeRule) {
    // create the label. If any of the edges belong to areas,
    // the label must be an area label
    boolean isArea = false;
    for (final EdgeEnd edgeEnd : this) {
      if (edgeEnd.getLabel().isArea()) {
        isArea = true;
      }
    }
    if (isArea) {
      setLabel(new Label(Location.NONE, Location.NONE, Location.NONE));
    } else {
      setLabel(new Label(Location.NONE));
    }

    // compute the On label, and the side labels if present
    for (int i = 0; i < 2; i++) {
      computeLabelOn(i, boundaryNodeRule);
      if (isArea) {
        computeLabelSides(i);
      }
    }
  }

  /**
   * Compute the overall ON location for the list of EdgeStubs.
   * (This is essentially equivalent to computing the self-overlay of a single Geometry)
   * edgeStubs can be either on the boundary (eg Polygon edge)
   * OR in the interior (e.g. segment of a LineString)
   * of their parent Geometry.
   * In addition, GeometryCollections use a {@link BoundaryNodeRule} to determine
   * whether a segment is on the boundary or not.
   * Finally, in GeometryCollections it can occur that an edge is both
   * on the boundary and in the interior (e.g. a LineString segment lying on
   * top of a Polygon edge.) In this case the Boundary is given precendence.
   * <br>
   * These observations result in the following rules for computing the ON location:
   * <ul>
   * <li> if there are an odd number of Bdy edges, the attribute is Bdy
   * <li> if there are an even number >= 2 of Bdy edges, the attribute is Int
   * <li> if there are any Int edges, the attribute is Int
   * <li> otherwise, the attribute is NULL.
   * </ul>
   */
  private void computeLabelOn(final int geomIndex, final BoundaryNodeRule boundaryNodeRule) {
    // compute the ON location value
    int boundaryCount = 0;
    boolean foundInterior = false;

    for (final EdgeEnd e : this) {
      final Location loc = e.getLabel().getLocation(geomIndex);
      if (loc == Location.BOUNDARY) {
        boundaryCount++;
      }
      if (loc == Location.INTERIOR) {
        foundInterior = true;
      }
    }
    Location loc = Location.NONE;
    if (foundInterior) {
      loc = Location.INTERIOR;
    }
    if (boundaryCount > 0) {
      loc = GeometryGraph.determineBoundary(boundaryNodeRule, boundaryCount);
    }
    getLabel().setLocation(geomIndex, loc);

  }

  /**
   * To compute the summary label for a side, the algorithm is:
   *   FOR all edges
   *     IF any edge's location is INTERIOR for the side, side location = INTERIOR
   *     ELSE IF there is at least one EXTERIOR attribute, side location = EXTERIOR
   *     ELSE  side location = NULL
   *  <br>
   *  Note that it is possible for two sides to have apparently contradictory information
   *  i.e. one edge side may indicate that it is in the interior of a geometry, while
   *  another edge side may indicate the exterior of the same geometry.  This is
   *  not an incompatibility - GeometryCollections may contain two Polygons that touch
   *  along an edge.  This is the reason for Interior-primacy rule above - it
   *  results in the summary label having the Geometry interior on <b>both</b> sides.
   */
  private void computeLabelSide(final int geomIndex, final int side) {
    for (final EdgeEnd e : this) {
      if (e.getLabel().isArea()) {
        final Location loc = e.getLabel().getLocation(geomIndex, side);
        if (loc == Location.INTERIOR) {
          getLabel().setLocation(geomIndex, side, Location.INTERIOR);
          return;
        } else if (loc == Location.EXTERIOR) {
          getLabel().setLocation(geomIndex, side, Location.EXTERIOR);
        }
      }
    }
  }

  /**
   * Compute the labelling for each side
   */
  private void computeLabelSides(final int geomIndex) {
    computeLabelSide(geomIndex, Position.LEFT);
    computeLabelSide(geomIndex, Position.RIGHT);
  }

  public List<EdgeEnd> getEdgeEnds() {
    return this.edgeEnds;
  }

  public void insert(final EdgeEnd e) {
    // Assert: start point is the same
    // Assert: direction is the same
    this.edgeEnds.add(e);
  }

  @Override
  public Iterator<EdgeEnd> iterator() {
    return this.edgeEnds.iterator();
  }

  @Override
  public void print(final PrintStream out) {
    out.println("EdgeEndBundle--> Label: " + getLabel());
    for (final EdgeEnd e : this) {
      e.print(out);
      out.println();
    }
  }

  /**
   * Update the IM with the contribution for the computed label for the EdgeStubs.
   */
  void updateIM(final IntersectionMatrix im) {
    Edge.updateIM(getLabel(), im);
  }
}
