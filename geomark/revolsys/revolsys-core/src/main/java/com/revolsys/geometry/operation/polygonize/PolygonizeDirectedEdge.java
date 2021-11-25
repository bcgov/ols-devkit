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
package com.revolsys.geometry.operation.polygonize;

import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.planargraph.DirectedEdge;
import com.revolsys.geometry.planargraph.Node;

/**
 * A {@link DirectedEdge} of a {@link PolygonizeGraph}, which represents
 * an edge of a polygon formed by the graph.
 * May be logically deleted from the graph by setting the <code>marked</code> flag.
 *
 * @version 1.7
 */
class PolygonizeDirectedEdge extends DirectedEdge {

  private EdgeRing edgeRing = null;

  private long label = -1;

  private PolygonizeDirectedEdge next = null;

  /**
   * Constructs a directed edge connecting the <code>from</code> node to the
   * <code>to</code> node.
   *
   * @param directionPt
   *                  specifies this DirectedEdge's direction (given by an imaginary
   *                  line from the <code>from</code> node to <code>directionPt</code>)
   * @param edgeDirection
   *                  whether this DirectedEdge's direction is the same as or
   *                  opposite to that of the parent Edge (if any)
   */
  public PolygonizeDirectedEdge(final Node from, final Node to, final Point directionPt,
    final boolean edgeDirection) {
    super(from, to, directionPt, edgeDirection);
  }

  /**
   * Returns the identifier attached to this directed edge.
   */
  public long getLabel() {
    return this.label;
  }

  /**
   * Returns the next directed edge in the EdgeRing that this directed edge is a member
   * of.
   */
  public PolygonizeDirectedEdge getNext() {
    return this.next;
  }

  /**
   * Returns the ring of directed edges that this directed edge is
   * a member of, or null if the ring has not been set.
   * @see #setRing(EdgeRing)
   */
  public boolean isInRing() {
    return this.edgeRing != null;
  }

  /**
   * Attaches an identifier to this directed edge.
   */
  public void setLabel(final long label) {
    this.label = label;
  }

  /**
   * Sets the next directed edge in the EdgeRing that this directed edge is a member
   * of.
   */
  public void setNext(final PolygonizeDirectedEdge next) {
    this.next = next;
  }

  /**
   * Sets the ring of directed edges that this directed edge is
   * a member of.
   */
  public void setRing(final EdgeRing edgeRing) {
    this.edgeRing = edgeRing;
  }

}
