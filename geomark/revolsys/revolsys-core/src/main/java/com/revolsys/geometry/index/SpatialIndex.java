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
package com.revolsys.geometry.index;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jeometry.common.function.Consumer3;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.predicate.Predicates;
import com.revolsys.visitor.CreateListVisitor;
import com.revolsys.visitor.SingleObjectVisitor;

/**
 * The basic operations supported by classes
 * implementing spatial index algorithms.
 * <p>
 * A spatial index typically provides a primary filter for range rectangle queries.
 * A secondary filter is required to test for exact intersection.
 * The secondary filter may consist of other kinds of tests,
 * such as testing other spatial relationships.
 *
 * @version 1.7
 */
public interface SpatialIndex<T> extends GeometryFactoryProxy {
  void clear();

  default boolean forEach(final BoundingBoxProxy boundingBoxProxy,
    final Consumer<? super T> action) {
    final BoundingBox boundingBox = convertBoundingBox(boundingBoxProxy);
    final double minX = boundingBox.getMinX();
    final double minY = boundingBox.getMinY();
    final double maxX = boundingBox.getMaxX();
    final double maxY = boundingBox.getMaxY();
    return forEach(minX, minY, maxX, maxY, action);
  }

  default boolean forEach(final BoundingBoxProxy boundingBoxProxy,
    final Predicate<? super T> filter, final Consumer<? super T> action) {
    final BoundingBox boundingBox = convertBoundingBox(boundingBoxProxy);
    final double minX = boundingBox.getMinX();
    final double minY = boundingBox.getMinY();
    final double maxX = boundingBox.getMaxX();
    final double maxY = boundingBox.getMaxY();
    return forEach(minX, minY, maxX, maxY, filter, action);
  }

  boolean forEach(final Consumer<? super T> action);

  boolean forEach(double x, double y, Consumer<? super T> action);

  boolean forEach(double minX, double minY, double maxX, double maxY, Consumer<? super T> action);

  default boolean forEach(final double minX, final double minY, final double maxX,
    final double maxY, final Predicate<? super T> filter, final Consumer<? super T> action) {
    final Consumer<? super T> filteredAction = Predicates.newConsumer(filter, action);
    return forEach(minX, minY, maxX, maxY, filteredAction);
  }

  default boolean forEach(final double x, final double y, final Predicate<? super T> filter,
    final Consumer<? super T> action) {
    final Consumer<? super T> filteredAction = Predicates.newConsumer(filter, action);
    return forEach(x, y, filteredAction);
  }

  default boolean forEach(final Predicate<? super T> filter, final Consumer<? super T> action) {
    final Consumer<? super T> filteredAction = Predicates.newConsumer(filter, action);
    return forEach(filteredAction);
  }

  default T getFirst(final BoundingBox boundingBox, final Predicate<T> filter) {
    final SingleObjectVisitor<T> visitor = new SingleObjectVisitor<>(filter);
    forEach(boundingBox, visitor);
    return visitor.getObject();
  }

  default T getFirstBoundingBox(final Geometry geometry, final Predicate<T> filter) {
    if (geometry == null) {
      return null;
    } else {
      final BoundingBox boundingBox = geometry.getBoundingBox();
      return getFirst(boundingBox, filter);
    }
  }

  default List<T> getItems() {
    final CreateListVisitor<T> visitor = new CreateListVisitor<>();
    forEach(visitor);
    return visitor.getList();
  }

  /**
   * Queries the index for all items whose extents intersect the given search {@link BoundingBox}
   * Note that some kinds of indexes may also return objects which do not in fact
   * intersect the query envelope.
   *
   * @param boundingBox the envelope to query for
   * @return a list of the items found by the query
   */
  default List<T> getItems(final BoundingBoxProxy boundingBox) {
    return BoundingBox.newArray(this::forEach, boundingBox);
  }

  default List<T> getItems(final BoundingBoxProxy boundingBox, final Predicate<? super T> filter) {
    final Consumer3<BoundingBoxProxy, Predicate<? super T>, Consumer<T>> forEachFunction = this::forEach;
    return BoundingBox.<T> newArray(forEachFunction, boundingBox, filter);
  }

  default List<T> getItems(final double x, final double y) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<>();
    forEach(x, y, visitor);
    return visitor.getList();
  }

  default List<T> getItems(final double minX, final double minY, final double maxX,
    final double maxY) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<>();
    forEach(minX, minY, maxX, maxY, visitor);
    return visitor.getList();
  }

  default List<T> getItems(final double minX, final double minY, final double maxX,
    final double maxY, final Predicate<? super T> filter) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<>();
    forEach(minX, minY, maxX, maxY, filter, visitor);
    return visitor.getList();
  }

  default List<T> getItems(final double x, final double y, final Predicate<? super T> filter) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<>();
    forEach(x, y, filter, visitor);
    return visitor.getList();
  }

  int getSize();

  /**
   * Adds a spatial item with an extent specified by the given {@link BoundingBox} to the index
   */
  void insertItem(BoundingBox boundingBox, T item);

  default void insertItem(final BoundingBoxProxy boundingBoxProxy, final T item) {
    final BoundingBox boundingBox = boundingBoxProxy.getBoundingBox();
    insertItem(boundingBox, item);
  }

  /**
   * Removes a single item from the tree.
   *
   * @param boundingBox the BoundingBox of the item to remove
   * @param item the item to remove
   * @return <code>true</code> if the item was found
   */
  boolean removeItem(BoundingBox boundingBox, T item);

  default void setGeometryFactory(final GeometryFactory geometryFactory) {
  }

}
