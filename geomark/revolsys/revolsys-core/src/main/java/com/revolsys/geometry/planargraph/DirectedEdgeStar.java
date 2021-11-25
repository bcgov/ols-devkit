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

package com.revolsys.geometry.planargraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A sorted collection of {@link DirectedEdge}s which leave a {@link Node}
 * in a {@link PlanarGraph}.
 *
 * @version 1.7
 */
public class DirectedEdgeStar implements Iterable<DirectedEdge> {

  /**
   * The underlying list of outgoing DirectedEdges
   */
  private final List<DirectedEdge> outEdges = new ArrayList<>();

  private boolean sorted = false;

  /**
   * Constructs a DirectedEdgeStar with no edges.
   */
  public DirectedEdgeStar() {
  }

  /**
   * Adds a new member to this DirectedEdgeStar.
   */
  public void add(final DirectedEdge de) {
    this.outEdges.add(de);
    this.sorted = false;
  }

  /**
   * Returns the number of edges around the Node associated with this DirectedEdgeStar.
   */
  public int getDegree() {
    return this.outEdges.size();
  }

  /**
   * Returns the DirectedEdges, in ascending order by angle with the positive x-axis.
   */
  public List<DirectedEdge> getEdges() {
    sortEdges();
    return this.outEdges;
  }

  /**
   * Returns the zero-based index of the given DirectedEdge, after sorting in ascending order
   * by angle with the positive x-axis.
   */
  public int getIndex(final DirectedEdge dirEdge) {
    sortEdges();
    for (int i = 0; i < this.outEdges.size(); i++) {
      final DirectedEdge de = this.outEdges.get(i);
      if (de == dirEdge) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the zero-based index of the given Edge, after sorting in ascending order
   * by angle with the positive x-axis.
   */
  public int getIndex(final Edge edge) {
    sortEdges();
    for (int i = 0; i < this.outEdges.size(); i++) {
      final DirectedEdge de = this.outEdges.get(i);
      if (de.getEdge() == edge) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns value of i modulo the number of edges in this DirectedEdgeStar
   * (i.e. the remainder when i is divided by the number of edges)
   *
   * @param i an integer (positive, negative or zero)
   */
  public int getIndex(final int i) {
    int modi = i % this.outEdges.size();
    // I don't think modi can be 0 (assuming i is positive) [Jon Aquino
    // 10/28/2003]
    if (modi < 0) {
      modi += this.outEdges.size();
    }
    return modi;
  }

  /**
   * Returns the {@link DirectedEdge} on the right-hand (CW)
   * side of the given {@link DirectedEdge}
   * (which must be a member of this DirectedEdgeStar).
   */
  public DirectedEdge getNextCWEdge(final DirectedEdge dirEdge) {
    final int i = getIndex(dirEdge);
    return this.outEdges.get(getIndex(i - 1));
  }

  /**
   * Returns the {@link DirectedEdge} on the left-hand (CCW)
   * side of the given {@link DirectedEdge}
   * (which must be a member of this DirectedEdgeStar).
   */
  public DirectedEdge getNextEdge(final DirectedEdge dirEdge) {
    final int i = getIndex(dirEdge);
    return this.outEdges.get(getIndex(i + 1));
  }

  /**
   * Returns an Iterator over the DirectedEdges, in ascending order by angle with the positive x-axis.
   */
  @Override
  public Iterator<DirectedEdge> iterator() {
    sortEdges();
    return this.outEdges.iterator();
  }

  /**
   * Drops a member of this DirectedEdgeStar.
   */
  public void remove(final DirectedEdge de) {
    this.outEdges.remove(de);
  }

  private void sortEdges() {
    if (!this.sorted) {
      Collections.sort(this.outEdges);
      this.sorted = true;
    }
  }
}
