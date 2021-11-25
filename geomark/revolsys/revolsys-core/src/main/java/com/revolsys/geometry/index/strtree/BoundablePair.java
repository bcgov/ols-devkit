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

import com.revolsys.geometry.util.PriorityQueue;

/**
 * A pair of {@link Boundable}s, whose leaf items
 * support a distance metric between them.
 * Used to compute the distance between the members,
 * and to expand a member relative to the other
 * in order to produce new branches of the
 * Branch-and-Bound evaluation tree.
 * Provides an ordering based on the distance between the members,
 * which allows building a priority queue by minimum distance.
 *
 * @author Martin Davis
 *
 */
class BoundablePair<I> implements Comparable<BoundablePair<I>> {
  /**
   * Computes the distance between the {@link Boundable}s in this pair.
   * The boundables are either composites or leaves.
   * If either is composite, the distance is computed as the minimum distance
   * between the bounds.
   * If both are leaves, the distance is computed by {@link #itemDistance(StrTreeLeaf, StrTreeLeaf)}.
   * @param itemDistance
   *
   * @return
   */
  public static <ITEM> double distance(final ItemDistance<ITEM> itemDistance,
    final Boundable<ITEM> boundable1, final Boundable<ITEM> boundable2) {
    if (boundable1.isNode() || boundable2.isNode()) {
      return boundable1.bboxDistance(boundable2);
    } else {
      final StrTreeLeaf<ITEM> item1 = (StrTreeLeaf<ITEM>)boundable1;
      final StrTreeLeaf<ITEM> item2 = (StrTreeLeaf<ITEM>)boundable2;
      return itemDistance.distance(item1, item2);
    }
  }

  public static boolean isLeaves(final Boundable<?> boundable1, final Boundable<?> boundable2) {
    return !(boundable1.isNode() || boundable2.isNode());
  }

  private final Boundable<I> boundable1;

  private final Boundable<I> boundable2;

  private final double distance;

  public BoundablePair(final Boundable<I> boundable1, final Boundable<I> boundable2,
    final double distance) {
    this.boundable1 = boundable1;
    this.boundable2 = boundable2;
    this.distance = distance;
  }

  public BoundablePair(final Boundable<I> boundable1, final Boundable<I> boundable2,
    final ItemDistance<I> itemDistance) {
    this.boundable1 = boundable1;
    this.boundable2 = boundable2;
    this.distance = distance(itemDistance, this.boundable1, this.boundable2);
  }

  /**
   * Compares two pairs based on their minimum distances
   */
  @Override
  public int compareTo(final BoundablePair<I> nd) {
    if (this.distance < nd.distance) {
      return -1;
    } else if (this.distance > nd.distance) {
      return 1;
    } else {
      return 0;
    }
  }

  private void expand(final Boundable<I> bndComposite, final Boundable<I> bndOther,
    final PriorityQueue<BoundablePair<I>> priQ, final ItemDistance<I> itemDistance,
    final double minDistance) {
    final int childCount = bndComposite.getChildCount();
    final Boundable<I>[] children = bndComposite.getChildren();
    for (int i = 0; i < childCount; i++) {
      final Boundable<I> child = children[i];
      final double distance = BoundablePair.distance(itemDistance, child, bndOther);
      // only add to queue if this pair might contain the closest points
      if (distance < minDistance) {
        final BoundablePair<I> bp = new BoundablePair<>(child, bndOther, distance);
        priQ.add(bp);
      }
    }
  }

  /**
   * For a pair which is not a leaf
   * (i.e. has at least one composite boundable)
   * computes a list of new pairs
   * from the expansion of the larger boundable.
   *
   */
  public void expandToQueue(final PriorityQueue<BoundablePair<I>> priQ,
    final ItemDistance<I> itemDistance, final double minDistance) {
    /*
     * HEURISTIC: If both boundable are composite, choose the one with largest
     * area to expand. Otherwise, simply expand whichever is composite.
     */
    if (this.boundable1.isNode()) {
      if (this.boundable2.isNode()) {
        if (this.boundable1.getArea() > this.boundable2.getArea()) {
          expand(this.boundable1, this.boundable2, priQ, itemDistance, minDistance);
        } else {
          expand(this.boundable2, this.boundable1, priQ, itemDistance, minDistance);
        }
      } else {
        expand(this.boundable1, this.boundable2, priQ, itemDistance, minDistance);

      }
    } else {
      if (this.boundable2.isNode()) {
        expand(this.boundable2, this.boundable1, priQ, itemDistance, minDistance);
      } else {
        throw new IllegalArgumentException("neither boundable is composite");
      }
    }
  }

  /**
   * Gets one of the member {@link Boundable}s in the pair
   * (indexed by [0, 1]).
   *
   * @param i the index of the member to return (0 or 1)
   * @return the chosen member
   */
  public Boundable<I> getBoundable(final int i) {
    if (i == 0) {
      return this.boundable1;
    }
    return this.boundable2;
  }

  /**
   * Gets the minimum possible distance between the Boundables in
   * this pair.
   * If the members are both items, this will be the
   * exact distance between them.
   * Otherwise, this distance will be a lower bound on
   * the distances between the items in the members.
   *
   * @return the exact or lower bound distance for this pair
   */
  public double getDistance() {
    return this.distance;
  }

  /**
   * Tests if both elements of the pair are leaf nodes
   *
   * @return true if both pair elements are leaf nodes
   */
  public boolean isLeaves() {
    return isLeaves(this.boundable1, this.boundable2);
  }
}
