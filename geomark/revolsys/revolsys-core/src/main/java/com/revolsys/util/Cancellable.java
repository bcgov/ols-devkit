package com.revolsys.util;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.collection.iterator.CancelIterable;
import com.revolsys.collection.iterator.Iterators;

public interface Cancellable {
  static Cancellable FALSE = () -> {
    return false;
  };

  default void cancel() {
  }

  default <V> Iterable<V> cancellable(final Iterable<V> iterable) {
    return new CancelIterable<>(this, iterable);
  }

  default <V> Iterable<V> cancellable(final Iterable<V> iterable, final Predicate<V> filter) {
    final Iterable<V> filteredIterator = Iterators.filter(iterable, filter);
    return new CancelIterable<>(this, filteredIterator);
  }

  default <V> Iterator<V> cancellable(final Iterator<V> iterator) {
    return new CancelIterable<>(this, iterator);
  }

  default <V> Iterator<V> cancellable(final Iterator<V> iterator, final Predicate<V> filter) {
    final Iterator<V> filteredIterator = Iterators.filter(iterator, filter);
    return new CancelIterable<>(this, filteredIterator);
  }

  /**
   *
   * @param iterable
   * @param action
   * @return true if cancelled, false otherwise
   */
  default <V> boolean forCancel(final Iterable<V> iterable, final Consumer<V> action) {
    for (final V value : iterable) {
      if (isCancelled()) {
        return true;
      } else {
        action.accept(value);
      }
    }
    return isCancelled();
  }

  boolean isCancelled();
}
