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
package com.revolsys.geometry.model;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *  Iterates over all {@link Geometry}s in a {@link Geometry},
 *  (which may be either a collection or an atomic geometry).
 *  The iteration sequence follows a pre-order, depth-first traversal of the
 *  structure of the <code>GeometryCollection</code>
 *  (which may be nested). The original <code>Geometry</code> object is
 *  returned as well (as the first object), as are all sub-collections and atomic elements.
 *  It is  simple to ignore the intermediate <code>GeometryCollection</code> objects if they are not
 *  needed.
 *
 *@version 1.7
 */
public class GeometryCollectionIterator implements Iterator<Geometry> {

  /**
   *  Indicates whether or not the first element
   *  (the root <code>GeometryCollection</code>) has been returned.
   */
  private boolean atStart;

  /**
   *  The index of the <code>Geometry</code> that will be returned when <code>next</code>
   *  is called.
   */
  private int index;

  /**
   *  The number of <code>Geometry</code>s in the <code>GeometryCollection</code>.
   */
  private final int max;

  /**
   *  The <code>Geometry</code> being iterated over.
   */
  private final Geometry parent;

  /**
   *  The iterator over a nested <code>Geometry</code>, or <code>null</code>
   *  if this <code>GeometryCollectionIterator</code> is not currently iterating
   *  over a nested <code>GeometryCollection</code>.
   */
  private GeometryCollectionIterator subcollectionIterator;

  /**
   *  Constructs an iterator over the given <code>Geometry</code>.
   *
   *@param  parent  the geometry over which to iterate; also, the first
   *      element returned by the iterator.
   */
  public GeometryCollectionIterator(final Geometry parent) {
    this.parent = parent;
    this.atStart = true;
    this.index = 0;
    this.max = parent.getGeometryCount();
  }

  /**
   * Tests whether any geometry elements remain to be returned.
   *
   * @return true if more geometry elements remain
   */
  @Override
  public boolean hasNext() {
    if (this.atStart) {
      return true;
    }
    if (this.subcollectionIterator != null) {
      if (this.subcollectionIterator.hasNext()) {
        return true;
      }
      this.subcollectionIterator = null;
    }
    if (this.index >= this.max) {
      return false;
    }
    return true;
  }

  /**
   * Gets the next geometry in the iteration sequence.
   *
   * @return the next geometry in the iteration
   */
  @Override
  public Geometry next() {
    // the parent GeometryCollection is the first object returned
    if (this.atStart) {
      this.atStart = false;
      return this.parent;
    }
    if (this.subcollectionIterator != null) {
      if (this.subcollectionIterator.hasNext()) {
        return this.subcollectionIterator.next();
      } else {
        this.subcollectionIterator = null;
      }
    }
    if (this.index >= this.max) {
      throw new NoSuchElementException();
    }
    final Geometry geometry = this.parent.getGeometry(this.index++);
    if (geometry.isGeometryCollection()) {
      this.subcollectionIterator = new GeometryCollectionIterator(geometry);
      // there will always be at least one element in the sub-collection
      return this.subcollectionIterator.next();
    }
    return geometry;
  }

  /**
   * Removal is not supported.
   *
   * @throws  UnsupportedOperationException  This method is not implemented.
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException(getClass().getName());
  }
}
