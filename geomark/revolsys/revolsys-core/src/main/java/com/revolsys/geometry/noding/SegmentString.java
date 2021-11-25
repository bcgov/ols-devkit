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

import com.revolsys.geometry.model.LineString;

/**
 * An interface for classes which represent a sequence of contiguous line segments.
 * SegmentStrings can carry a context object, which is useful
 * for preserving topological or parentage information.
 *
 * @version 1.7
 */
public interface SegmentString {
  default boolean equalsVertex2d(final int vertexIndex1, final int vertexIndex2) {
    final LineString line = getLineString();
    final double x1 = line.getX(vertexIndex1);
    final double x2 = line.getX(vertexIndex2);
    if (x1 == x2) {
      final double y1 = line.getY(vertexIndex1);
      final double y2 = line.getY(vertexIndex2);
      if (y1 == y2) {
        return true;
      }
    }
    return false;
  }

  /**
  * Gets the user-defined data for this segment string.
  *
  * @return the user-defined data
  */
  Object getData();

  LineString getLineString();

  boolean isClosed();

  /**
   * Sets the user-defined data for this segment string.
   *
   * @param data an Object containing user-defined data
   */
  void setData(Object data);

  int size();
}
