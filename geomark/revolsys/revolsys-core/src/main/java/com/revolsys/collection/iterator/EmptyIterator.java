package com.revolsys.collection.iterator;

import java.util.NoSuchElementException;

public class EmptyIterator<T> extends AbstractIterator<T> {
  public static <V> EmptyIterator<V> newIterator() {
    return new EmptyIterator<>();
  }

  @Override
  protected T getNext() throws NoSuchElementException {
    throw new NoSuchElementException();
  }
}
