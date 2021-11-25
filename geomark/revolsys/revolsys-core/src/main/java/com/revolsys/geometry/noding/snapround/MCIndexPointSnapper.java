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

package com.revolsys.geometry.noding.snapround;

import com.revolsys.geometry.index.SpatialIndex;
import com.revolsys.geometry.index.chain.MonotoneChain;
import com.revolsys.geometry.index.chain.MonotoneChainSelectAction;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.noding.MonotoneChainIndex;
import com.revolsys.geometry.noding.NodedSegmentString;
import com.revolsys.geometry.noding.SegmentString;

/**
 * "Snaps" all {@link SegmentString}s in a {@link SpatialIndex} containing
 * {@link MonotoneChain}s to a given {@link HotPixel}.
 *
 * @version 1.7
 */
public class MCIndexPointSnapper {
  // public static final int nSnaps = 0;

  public class HotPixelSnapAction implements MonotoneChainSelectAction {
    private final HotPixel hotPixel;

    // is -1 if hotPixel is not a vertex
    private final int hotPixelVertexIndex;

    private boolean isNodeAdded = false;

    private final SegmentString parentEdge;

    public HotPixelSnapAction(final HotPixel hotPixel, final SegmentString parentEdge,
      final int hotPixelVertexIndex) {
      this.hotPixel = hotPixel;
      this.parentEdge = parentEdge;
      this.hotPixelVertexIndex = hotPixelVertexIndex;
    }

    public boolean isNodeAdded() {
      return this.isNodeAdded;
    }

    @Override
    public void select(final MonotoneChain mc, final int startIndex) {
      final NodedSegmentString ss = (NodedSegmentString)mc.getContext();
      /**
       * Check to avoid snapping a hotPixel vertex to the same vertex.
       * This method is called for segments which intersects the
       * hot pixel,
       * so need to check if either end of the segment is equal to the hot pixel
       * and if so, do not snap.
       *
       * Sep 22 2012 - MD - currently do need to snap to every vertex,
       * since otherwise the testCollapse1 test in SnapRoundingTest fails.
       */
      if (this.parentEdge != null) {
        if (ss == this.parentEdge && startIndex == this.hotPixelVertexIndex) {
          return;
        }
      }
      this.isNodeAdded = this.hotPixel.addSnappedNode(ss, startIndex);
    }

  }

  private final MonotoneChainIndex index;

  public MCIndexPointSnapper(final MonotoneChainIndex index) {
    this.index = index;
  }

  public boolean snap(final HotPixel hotPixel) {
    return snap(hotPixel, null, -1);
  }

  /**
   * Snaps (nodes) all interacting segments to this hot pixel.
   * The hot pixel may represent a vertex of an edge,
   * in which case this routine uses the optimization
   * of not noding the vertex itself
   *
   * @param hotPixel the hot pixel to snap to
   * @param parentEdge the edge containing the vertex, if applicable, or <code>null</code>
   * @param hotPixelVertexIndex the index of the hotPixel vertex, if applicable, or -1
   * @return <code>true</code> if a node was added for this pixel
   */
  public boolean snap(final HotPixel hotPixel, final SegmentString parentEdge,
    final int hotPixelVertexIndex) {
    final BoundingBox pixelEnv = hotPixel.getSafeEnvelope();
    final HotPixelSnapAction hotPixelSnapAction = new HotPixelSnapAction(hotPixel, parentEdge,
      hotPixelVertexIndex);

    this.index.query(pixelEnv, (item) -> {
      final MonotoneChain testChain = item;
      testChain.select(pixelEnv, hotPixelSnapAction);
    });
    return hotPixelSnapAction.isNodeAdded();
  }

}
