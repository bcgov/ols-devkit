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
import java.util.List;

import com.revolsys.geometry.geomgraph.DirectedEdge;
import com.revolsys.geometry.geomgraph.DirectedEdgeStar;
import com.revolsys.geometry.geomgraph.EdgeRing;
import com.revolsys.geometry.geomgraph.Node;
import com.revolsys.geometry.model.GeometryFactory;

/**
 * A ring of {@link DirectedEdge}s which may contain nodes of degree > 2.
 * A <tt>MaximalEdgeRing</tt> may represent two different spatial entities:
 * <ul>
 * <li>a single polygon possibly containing inversions (if the ring is oriented CW)
 * <li>a single hole possibly containing exversions (if the ring is oriented CCW)
 * </ul>
 * If the MaximalEdgeRing represents a polygon,
 * the interior of the polygon is strongly connected.
 * <p>
 * These are the form of rings used to define polygons under some spatial data models.
 * However, under the OGC SFS model, {@link MinimalEdgeRing}s are required.
 * A MaximalEdgeRing can be converted to a list of MinimalEdgeRings using the
 * {@link #buildMinimalRings() } method.
 *
 * @version 1.7
 * @see com.revolsys.geometry.operation.overlay.MinimalEdgeRing
 */
public class MaximalEdgeRing extends EdgeRing {

  public MaximalEdgeRing(final DirectedEdge start, final GeometryFactory geometryFactory) {
    super(start, geometryFactory);
  }

  public List<MinimalEdgeRing> buildMinimalRings() {
    final List<MinimalEdgeRing> minEdgeRings = new ArrayList<>();
    DirectedEdge de = this.startDe;
    do {
      if (de.getMinEdgeRing() == null) {
        final MinimalEdgeRing minEr = new MinimalEdgeRing(de, this.geometryFactory);
        minEdgeRings.add(minEr);
      }
      de = de.getNext();
    } while (de != this.startDe);
    return minEdgeRings;
  }

  @Override
  public DirectedEdge getNext(final DirectedEdge de) {
    return de.getNext();
  }

  /**
   * For all nodes in this EdgeRing,
   * link the DirectedEdges at the node to form minimalEdgeRings
   */
  public void linkDirectedEdgesForMinimalEdgeRings() {
    DirectedEdge de = this.startDe;
    do {
      final Node node = de.getNode();
      ((DirectedEdgeStar)node.getEdges()).linkMinimalDirectedEdges(this);
      de = de.getNext();
    } while (de != this.startDe);
  }

  @Override
  public void setEdgeRing(final DirectedEdge de, final EdgeRing er) {
    de.setEdgeRing(er);
  }

}
