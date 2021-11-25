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
package com.revolsys.geometry.algorithm;

/**
 * Specifies and implements various fundamental Computational Geometric
 * algorithms. The algorithms supplied in this class are robust for
 * double-precision floating point.
 *
 * @version 1.7
 */
public class CGAlgorithms {

  /**
   * A value that indicates an orientation of clockwise, or a right turn.
   */
  public static final int CLOCKWISE = -1;

  /**
   * A value that indicates an orientation of collinear, or no turn (straight).
   */
  public static final int COLLINEAR = 0;

  /**
   * A value that indicates an orientation of counterclockwise, or a left turn.
   */
  public static final int COUNTERCLOCKWISE = 1;

  /**
   * A value that indicates an orientation of counterclockwise, or a left turn.
   */
  public static final int LEFT = COUNTERCLOCKWISE;

  /**
   * A value that indicates an orientation of clockwise, or a right turn.
   */
  public static final int RIGHT = CLOCKWISE;

  /**
   * A value that indicates an orientation of collinear, or no turn (straight).
   */
  public static final int STRAIGHT = COLLINEAR;

  public CGAlgorithms() {
  }

}
