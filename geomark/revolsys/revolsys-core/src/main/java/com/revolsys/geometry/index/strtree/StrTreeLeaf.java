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
package com.revolsys.geometry.index.strtree;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.Consumer;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;

/**
 * Boundable wrapper for a non-Boundable spatial object. Used internally by
 * AbstractSTRtree.
 *
 * @version 1.7
 */
public class StrTreeLeaf<I> extends BoundingBoxDoubleXY implements Boundable<I>, Serializable {
  private static final long serialVersionUID = 1L;

  private final I item;

  public StrTreeLeaf(final BoundingBox bounds, final I item) {
    super(bounds);
    this.item = item;
  }

  @Override
  public void boundablesAtLevel(final int level, final Collection<Boundable<I>> boundables) {
    if (level == -1) {
      boundables.add(this);
    }
  }

  @Override
  public I getItem() {
    return this.item;
  }

  @Override
  public void query(final double minX, final double minY, final double maxX, final double maxY,
    final Consumer<? super I> action) {
    if (bboxIntersects(minX, minY, maxX, maxY)) {
      action.accept(this.item);
    }
  }
}
