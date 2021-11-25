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

import com.revolsys.geometry.geomgraph.EdgeEnd;
import com.revolsys.geometry.geomgraph.EdgeEndStar;
import com.revolsys.geometry.model.IntersectionMatrix;

/**
 * An ordered list of {@link EdgeEndBundle}s around a {@link RelateNode}.
 * They are maintained in CCW order (starting with the positive x-axis) around the node
 * for efficient lookup and topology building.
 *
 * @version 1.7
 */
public class EdgeEndBundleStar extends EdgeEndStar<EdgeEndBundle> {
  /**
   * Creates a new empty EdgeEndBundleStar
   */
  public EdgeEndBundleStar() {
  }

  /**
   * Insert a EdgeEnd in order in the list.
   * If there is an existing EdgeStubBundle which is parallel, the EdgeEnd is
   * added to the bundle.  Otherwise, a new EdgeEndBundle is created
   * to contain the EdgeEnd.
   * <br>
   */
  @Override
  public void insert(final EdgeEnd e) {
    EdgeEndBundle eb = this.edgeMap.get(e);
    if (eb == null) {
      eb = new EdgeEndBundle(e);
      insertEdgeEnd(e, eb);
    } else {
      eb.insert(e);
    }
  }

  /**
   * Update the IM with the contribution for the EdgeStubs around the node.
   */
  void updateIM(final IntersectionMatrix im) {
    for (final EdgeEndBundle esb : this) {
      esb.updateIM(im);
    }
  }

}
