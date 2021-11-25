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
package com.revolsys.geometry.index.intervalrtree;

import java.util.function.Consumer;

public abstract class IntervalRTreeNode<V> {

  protected final double max;

  protected final double min;

  public IntervalRTreeNode() {
    this(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
  }

  public IntervalRTreeNode(final double min, final double max) {
    this.min = min;
    this.max = max;
  }

  public double getMax() {
    return this.max;
  }

  public double getMin() {
    return this.min;
  }

  protected boolean intersects(final double queryMin, final double queryMax) {
    return !(this.min > queryMax || this.max < queryMin);
  }

  public abstract void query(double queryMin, double queryMax, Consumer<? super V> visitor);

  @Override
  public String toString() {
    final double min = getMin();
    final double max = getMax();
    return "LINESTRING(0 " + min + ",0 " + max + ")";
  }
}
