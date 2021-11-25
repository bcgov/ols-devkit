package com.revolsys.collection.iterator;

import java.util.Iterator;

public class IteratorIterable<V> implements Iterable<V> {
  private final Iterator<V> iterator;

  public IteratorIterable(final Iterator<V> iterator) {
    super();
    this.iterator = iterator;
  }

  @Override
  public Iterator<V> iterator() {
    return this.iterator;
  }

}
