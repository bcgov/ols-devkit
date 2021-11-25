package com.revolsys.util;

import java.util.Iterator;
import java.util.function.Predicate;

public interface CancellableProxy extends Cancellable {
  @Override
  default void cancel() {
    final Cancellable cancellable = getCancellable();
    if (cancellable != null) {
      cancellable.cancel();
    }
  }

  @Override
  default <V> Iterable<V> cancellable(final Iterable<V> iterable) {
    final Cancellable cancellable = getCancellable();
    if (cancellable == null) {
      return iterable;
    } else {
      return cancellable.cancellable(iterable);
    }
  }

  @Override
  default <V> Iterable<V> cancellable(final Iterable<V> iterable, final Predicate<V> filter) {
    final Cancellable cancellable = getCancellable();
    if (cancellable == null) {
      return iterable;
    } else {
      return cancellable.cancellable(iterable, filter);
    }
  }

  @Override
  default <V> Iterator<V> cancellable(final Iterator<V> iterator) {
    final Cancellable cancellable = getCancellable();
    if (cancellable == null) {
      return iterator;
    } else {
      return cancellable.cancellable(iterator);
    }
  }

  @Override
  default <V> Iterator<V> cancellable(final Iterator<V> iterator, final Predicate<V> filter) {
    final Cancellable cancellable = getCancellable();
    if (cancellable == null) {
      return iterator;
    } else {
      return cancellable.cancellable(iterator, filter);
    }
  }

  Cancellable getCancellable();

  @Override
  default boolean isCancelled() {
    final Cancellable cancellable = getCancellable();
    if (cancellable == null) {
      return false;
    } else {
      return cancellable.isCancelled();
    }
  }
}
