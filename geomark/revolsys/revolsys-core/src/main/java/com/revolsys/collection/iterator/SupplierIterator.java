package com.revolsys.collection.iterator;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

public class SupplierIterator<V> extends AbstractIterator<V> {

  private final Supplier<V> supplier;

  public SupplierIterator(final Supplier<V> supplier) {
    this.supplier = supplier;
  }

  @Override
  protected V getNext() throws NoSuchElementException {
    if (this.supplier != null) {
      final V value = this.supplier.get();
      if (value != null) {
        return value;
      }
    }
    throw new NoSuchElementException();
  }
}
