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

import com.revolsys.geometry.geomgraph.GeometryGraph;
import com.revolsys.geometry.index.SpatialIndex;
import com.revolsys.geometry.index.strtree.StrTree;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;

/**
 * Tests whether any of a set of {@link LinearRing}s are
 * nested inside another ring in the set, using a spatial
 * index to speed up the comparisons.
 *
 * @version 1.7
 */
public class IndexedNestedRingTester {
  private final GeometryGraph graph; // used to find non-node vertices

  private SpatialIndex<LinearRing> index;

  private Point nestedPt;

  private final List<LinearRing> rings = new ArrayList<>();

  public IndexedNestedRingTester(final GeometryGraph graph) {
    this.graph = graph;
  }

  public void add(final LinearRing ring) {
    this.rings.add(ring);
  }

  private void buildIndex() {
    this.index = new StrTree<>();

    for (final LinearRing ring : this.rings) {
      final BoundingBox env = ring.getBoundingBox();
      this.index.insertItem(env, ring);
    }
  }

  public Point getNestedPoint() {
    return this.nestedPt;
  }

  public boolean isNonNested() {
    buildIndex();

    for (final LinearRing innerRing : this.rings) {

      final List<LinearRing> results = this.index.getItems(innerRing.getBoundingBox());

      for (final LinearRing searchRing : results) {
        if (innerRing == searchRing) {
        } else if (innerRing.getBoundingBox().bboxIntersects(searchRing.getBoundingBox())) {
          final Point innerRingPt = IsValidOp.findPtNotNode(innerRing, searchRing, this.graph);

          /**
           * If no non-node pts can be found, this means
           * that the searchRing touches ALL of the innerRing vertices.
           * This indicates an invalid polygon, since either
           * the two holes Construct a new disconnected interior,
           * or they touch in an infinite number of points
           * (i.e. along a line segment).
           * Both of these cases are caught by other tests,
           * so it is safe to simply skip this situation here.
           */
          if (innerRingPt != null) {
            final boolean isInside = searchRing.isPointInRing(innerRingPt);
            if (isInside) {
              this.nestedPt = innerRingPt;
              return false;
            }
          }
        }
      }
    }
    return true;
  }
}
