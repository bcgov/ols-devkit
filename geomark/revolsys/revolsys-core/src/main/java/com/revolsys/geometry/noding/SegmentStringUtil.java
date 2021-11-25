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

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;

/**
 * Utility methods for processing {@link SegmentString}s.
 *
 * @author Martin Davis
 *
 */
public class SegmentStringUtil {
  /**
   * Extracts all linear components from a given {@link Geometry}
   * to {@link SegmentString}s.
   * The SegmentString data item is set to be the source Geometry.
   *
   * @param geom the geometry to extract from
   * @return a List of SegmentStrings
   */
  public static List<NodedSegmentString> extractSegmentStrings(final Geometry geom) {
    final List<NodedSegmentString> segments = new ArrayList<>();
    final List<LineString> lines = geom.getGeometryComponents(LineString.class);
    for (final LineString line : lines) {
      final NodedSegmentString segment = new NodedSegmentString(line, geom);
      segments.add(segment);
    }
    return segments;
  }

  public static String toString(final List<? extends SegmentString> segments) {
    final StringBuilder buf = new StringBuilder();
    for (final SegmentString segment : segments) {
      buf.append(segment.toString());
      buf.append("\n");

    }
    return buf.toString();
  }
}
