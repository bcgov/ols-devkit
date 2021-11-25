/*
 * Copyright 2004-2007 Revolution Systems Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.util.Cancellable;
import com.revolsys.util.ExitLoopException;

/**
 * <p>
 * The Reader interface defines methods for reading objects of type T. Objects
 * can either by read as a {@link List} or using an {@link Iterator} or visited
 * using a {@link Consumer}.
 * </p>
 * <p>
 * The simplest and most effecient way to loop through all objects in the reader
 * is to use the following loop.
 * </p>
 *
 * <pre>
 * Reader&lt;T&gt; reader = ...
 * for (T object : reader) {
 *   // Do something with the object.
 * }
 * </pre>
 *
 * @author Paul Austin
 * @param <T> The type of the item to read.
 */
public interface Reader<T> extends Iterable<T>, ObjectWithProperties, BaseCloseable, Cancellable {
  Reader<?> EMPTY = wrap(Collections.emptyIterator());

  @SuppressWarnings("unchecked")
  static <V> Reader<V> empty() {
    return (Reader<V>)EMPTY;
  }

  static <I, O> Reader<O> wrap(final Iterator<I> iterator, final Function<I, O> converter) {
    return new IteratorConvertReader<>(iterator, converter);
  }

  static <V> Reader<V> wrap(final Iterator<V> iterator) {
    return new IteratorReader<>(iterator);
  }

  /**
   * Close the reader and all resources associated with it.
   */
  @Override
  default void close() {
  }

  default void forEach(final BiConsumer<Cancellable, ? super T> action) {
    forEach(this, action);
  }

  default void forEach(final Cancellable cancellable,
    final BiConsumer<Cancellable, ? super T> action) {
    try (
      Reader<?> reader = this) {
      if (iterator() != null) {
        try {
          for (final T item : this) {
            if (cancellable.isCancelled()) {
              return;
            } else {
              action.accept(cancellable, item);
            }
          }
        } catch (final ExitLoopException e) {
        }
      }
    }
  }

  default void forEach(final Cancellable cancellable, final Consumer<? super T> action) {
    try (
      Reader<?> reader = this) {
      if (iterator() != null) {
        try {
          for (final T item : this) {
            if (cancellable.isCancelled()) {
              return;
            } else {
              action.accept(item);
            }
          }
        } catch (final ExitLoopException e) {
        }
      }
    }
  }

  /**
   * Visit each item returned from the reader until all items have been visited
   * or the visit method returns false.
   *
   * @param visitor The visitor.
   */
  @Override
  default void forEach(final Consumer<? super T> action) {
    try (
      Reader<?> reader = this) {
      if (iterator() != null) {
        try {
          for (final T item : this) {
            action.accept(item);
          }
        } catch (final ExitLoopException e) {
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  default <V extends T> V getFirst() {
    try (
      Reader<?> reader = this) {
      if (iterator() != null) {
        for (final Object value : this) {
          return (V)value;
        }
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  default <V extends T> Iterable<V> i() {
    return (Iterable<V>)this;
  }

  /**
   * Open the reader so that it is ready to be read from.
   */
  default void open() {
  }

  default Stream<T> parallelStream() {
    return StreamSupport.stream(spliterator(), true);
  }

  default Stream<T> stream() {
    return StreamSupport.stream(spliterator(), false);
  }

  /**
   * Read all items and return a List containing the items.
   *
   * @return The list of items.
   */
  default List<T> toList() {
    final List<T> items = new ArrayList<>();
    try (
      Reader<?> reader = this) {
      if (iterator() != null) {
        for (final T item : this) {
          items.add(item);
        }
      }
    }
    return items;
  }
}
