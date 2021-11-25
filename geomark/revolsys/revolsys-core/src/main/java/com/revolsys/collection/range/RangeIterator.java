package com.revolsys.collection.range;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class RangeIterator<V> implements Iterator<V> {
  private V current;

  private boolean hasNext = true;

  private final AbstractRange<V> range;

  public RangeIterator(final AbstractRange<V> range) {
    this.current = range.getFrom();
    this.range = range;
  }

  @Override
  public boolean hasNext() {
    return this.hasNext;
  }

  @Override
  public V next() {
    if (this.hasNext) {
      final V next = this.current;
      this.current = this.range.next(this.current);
      if (this.current == null || this.range.compareToValue(this.current) < 0) {
        this.hasNext = false;
      }
      return next;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

}
