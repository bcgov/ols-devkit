package com.revolsys.collection.iterator;

import java.util.Iterator;
import java.util.function.Supplier;

public class SupplierIterable<V> implements Iterable<V> {
  private final Supplier<Iterator<V>> factory;

  public SupplierIterable(final Supplier<Iterator<V>> factory) {
    this.factory = factory;
  }

  @Override
  public Iterator<V> iterator() {
    return this.factory.get();
  }

}
