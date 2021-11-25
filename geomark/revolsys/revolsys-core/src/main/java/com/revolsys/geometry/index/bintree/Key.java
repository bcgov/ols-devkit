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
package com.revolsys.geometry.index.bintree;

import com.revolsys.geometry.index.DoubleBits;

/**
 * A Key is a unique identifier for a node in a tree.
 * It contains a lower-left point and a level number. The level number
 * is the power of two for the size of the node envelope
 *
 * @version 1.7
 */
public class Key {

  public static int computeLevel(final Interval interval) {
    final double dx = interval.getWidth();
    // int level = BinaryPower.exponent(dx) + 1;
    final int level = DoubleBits.exponent(dx) + 1;
    return level;
  }

  // auxiliary data which is derived from the key for use in computation
  private Interval interval;

  private int level = 0;

  // the fields which make up the key
  private double pt = 0.0;

  public Key(final Interval interval) {
    computeKey(interval);
  }

  private void computeInterval(final int level, final Interval itemInterval) {
    final double size = DoubleBits.powerOf2(level);
    // double size = pow2.power(level);
    this.pt = Math.floor(itemInterval.getMin() / size) * size;
    this.interval.init(this.pt, this.pt + size);
  }

  /**
   * return a square envelope containing the argument envelope,
   * whose extent is a power of two and which is based at a power of 2
   */
  public void computeKey(final Interval itemInterval) {
    this.level = computeLevel(itemInterval);
    this.interval = new Interval();
    computeInterval(this.level, itemInterval);
    // MD - would be nice to have a non-iterative form of this algorithm
    while (!this.interval.contains(itemInterval)) {
      this.level += 1;
      computeInterval(this.level, itemInterval);
    }
  }

  public Interval getInterval() {
    return this.interval;
  }

  public int getLevel() {
    return this.level;
  }

  public double getPoint() {
    return this.pt;
  }
}
