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

package com.revolsys.geometry.simplify;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.revolsys.geometry.index.quadtree.QuadTree;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDouble;
import com.revolsys.geometry.util.RectangleUtil;

/**
 * An spatial index on a set of {@link LineSegmentDouble}s.
 * Supports adding and removing items.
 *
 * @author Martin Davis
 *
 */
@Deprecated
class LineSegmentIndex {
  private final QuadTree<LineSegment> index = new QuadTree<>(GeometryFactory.DEFAULT_3D);

  public LineSegmentIndex() {
  }

  public void add(final LineSegment seg) {
    this.index.insertItem(seg.getBoundingBox(), seg);
  }

  public void add(final TaggedLineString line) {
    final TaggedLineSegment[] segs = line.getSegments();
    for (final TaggedLineSegment seg : segs) {
      add(seg);
    }
  }

  public List query(final LineSegment querySeg) {
    final BoundingBox env = querySeg.getBoundingBox();

    final LineSegmentVisitor visitor = new LineSegmentVisitor(querySeg);
    this.index.forEach(env, visitor);
    final List itemsFound = visitor.getItems();

    // List listQueryItems = index.query(env);
    // System.out.println("visitor size = " + itemsFound.size()
    // + " query size = " + listQueryItems.size());
    // List itemsFound = index.query(env);

    return itemsFound;
  }

  public void remove(final LineSegment seg) {
    this.index.removeItem(seg.getBoundingBox(), seg);
  }
}

/**
 * ItemVisitor subclass to reduce volume of query results.
 */
class LineSegmentVisitor implements Consumer<LineSegment> {
  // MD - only seems to make about a 10% difference in overall time.

  private final ArrayList<LineSegment> items = new ArrayList<>();

  private final LineSegment querySeg;

  public LineSegmentVisitor(final LineSegment querySeg) {
    this.querySeg = querySeg;
  }

  @Override
  public void accept(final LineSegment seg) {
    if (RectangleUtil.intersects(seg.getP0(), seg.getP1(), this.querySeg.getP0(),
      this.querySeg.getP1())) {
      this.items.add(seg);
    }
  }

  public List<LineSegment> getItems() {
    return this.items;
  }
}
