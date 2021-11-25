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
package com.revolsys.geometry.noding;

import java.util.Collection;

/**
 * Nodes a set of {@link SegmentString}s by
 * performing a brute-force comparison of every segment to every other one.
 * This has n^2 performance, so is too slow for use on large numbers
 * of segments.
 *
 * @version 1.7
 */
public class SimpleNoder extends SinglePassNoder {

  private Collection<? extends SegmentString> nodedSegStrings;

  public SimpleNoder() {
  }

  private void computeIntersects(final SegmentString e0, final SegmentString e1) {
    for (int i0 = 0; i0 < e0.size() - 1; i0++) {
      for (int i1 = 0; i1 < e1.size() - 1; i1++) {
        this.segInt.processIntersections(e0, i0, e1, i1);
      }
    }
  }

  @Override
  public void computeNodes(final Collection<NodedSegmentString> inputSegStrings) {
    this.nodedSegStrings = inputSegStrings;
    for (final Object element : inputSegStrings) {
      final SegmentString edge0 = (SegmentString)element;
      for (final Object element2 : inputSegStrings) {
        final SegmentString edge1 = (SegmentString)element2;
        computeIntersects(edge0, edge1);
      }
    }
  }

  @Override
  public Collection<NodedSegmentString> getNodedSubstrings() {
    return NodedSegmentString.getNodedSubstrings((Collection)this.nodedSegStrings);
  }

}
