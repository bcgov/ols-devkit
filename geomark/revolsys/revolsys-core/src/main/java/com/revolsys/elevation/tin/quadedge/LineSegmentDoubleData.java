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

package com.revolsys.elevation.tin.quadedge;

import com.revolsys.geometry.model.segment.LineSegmentDouble;

/**
 * Models a constraint segment in a triangulation.
 * A constraint segment is an oriented straight line segment between a start point
 * and an end point.
 *
 * @author David Skea
 * @author Martin Davis
 */
public class LineSegmentDoubleData extends LineSegmentDouble {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private Object data = null;

  /**
   * Creates a new instance for the given ordinates.
   */
  public LineSegmentDoubleData(final double x1, final double y1, final double z1, final double x2,
    final double y2, final double z2) {
    super(3, x1, y1, z1, x2, y2, z2);
  }

  /**
   * Creates a new instance for the given ordinates,  with associated external data.
   */
  public LineSegmentDoubleData(final double x1, final double y1, final double z1, final double x2,
    final double y2, final double z2, final Object data) {
    super(3, x1, y1, z1, x2, y2, z2);
    this.data = data;
  }

  /**
   * Gets the external data associated with this segment
   *
   * @return a data object
   */
  public Object getData() {
    return this.data;
  }

  /**
   * Sets the external data to be associated with this segment
   *
   * @param data a data object
   */
  public void setData(final Object data) {
    this.data = data;
  }
}
