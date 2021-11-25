package com.revolsys.collection.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.util.Cancellable;

public class CancelIterable<T> implements Iterator<T>, Iterable<T> {

  public static <V> Iterable<V> i(final Cancellable cancellable, final Iterable<V> iterable) {
    if (cancellable == null) {
      return iterable;
    } else {
      return new CancelIterable<>(cancellable, iterable);
    }
  }

  private final Cancellable cancellable;

  private Iterator<T> iterator;

  public CancelIterable(final Cancellable cancellable, final Iterable<T> iterable) {
    this(cancellable, iterable.iterator());
  }

  public CancelIterable(final Cancellable cancellable, final Iterator<T> iterator) {
    this.cancellable = cancellable;
    this.iterator = iterator;
  }

  private void close() {
    if (this.iterator instanceof AutoCloseable) {
      final AutoCloseable closeable = (AutoCloseable)this.iterator;
      try {
        closeable.close();
      } catch (final Exception e) {
      }
    }
  }

  @Override
  public boolean hasNext() {
    if (this.cancellable.isCancelled()) {
      close();
      return false;
    } else if (this.iterator.hasNext()) {
      return true;
    } else {
      close();
      return false;
    }
  }

  @Override
  public Iterator<T> iterator() {
    return this;
  }

  @Override
  public T next() {
    try {
      return this.iterator.next();
    } catch (final NoSuchElementException e) {
      close();
      throw e;
    }
  }

  @Override
  public void remove() {
    this.iterator.remove();
  }

}
