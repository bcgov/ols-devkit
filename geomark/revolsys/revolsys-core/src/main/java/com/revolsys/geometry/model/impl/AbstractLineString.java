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
package com.revolsys.geometry.model.impl;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;

/**
 * Models an OGC-style <code>LineString</code>. A LineString consists of a
 * sequence of two or more vertices, along with all points along the
 * linearly-interpolated curves (line segments) between each pair of consecutive
 * vertices. Consecutive vertices may be equal. The line segments in the line
 * may intersect each other (in other words, the linestring may "curl back" in
 * itself and self-intersect. Linestrings with exactly two identical points are
 * invalid.
 * <p>
 * A linestring must have either 0 or 2 or more points. If these conditions are
 * not met, the constructors throw an {@link IllegalArgumentException}
 *
 * @version 1.7
 */
public abstract class AbstractLineString implements LineString {
  private static final long serialVersionUID = 3110669828065365560L;

  /**
   * Creates and returns a full copy of this {@link LineString} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public LineString clone() {
    try {
      return (LineString)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof Geometry) {
      final Geometry geometry = (Geometry)other;
      return equals(2, geometry);
    } else {
      return false;
    }
  }

  /**
   * Gets a hash code for the Geometry.
   *
   * @return an integer value suitable for use as a hashcode
   */

  @Override
  public int hashCode() {
    return getBoundingBox().hashCode();
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
