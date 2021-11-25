package com.revolsys.collection.iterator;

import java.util.Enumeration;
import java.util.Iterator;

public class IteratorEnumeration<T> implements Enumeration<T> {
  @SuppressWarnings("unchecked")
  public static <V> Enumeration<V> newEnumeration(final Iterable<V> iterable) {
    if (iterable instanceof Enumeration) {
      return (Enumeration<V>)iterable;
    } else {
      return newEnumeration(iterable.iterator());
    }
  }

  @SuppressWarnings("unchecked")
  public static <V> Enumeration<V> newEnumeration(final Iterator<V> iterator) {
    if (iterator instanceof Enumeration) {
      return (Enumeration<V>)iterator;
    } else {
      return new IteratorEnumeration<>(iterator);
    }
  }

  private final Iterator<T> iterator;

  public IteratorEnumeration(final Iterator<T> iterator) {
    this.iterator = iterator;
  }

  @Override
  public boolean hasMoreElements() {
    return this.iterator.hasNext();
  }

  @Override
  public T nextElement() {
    return this.iterator.next();
  }
}
