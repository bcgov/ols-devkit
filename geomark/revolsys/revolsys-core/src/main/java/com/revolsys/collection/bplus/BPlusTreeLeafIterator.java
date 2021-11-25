package com.revolsys.collection.bplus;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;

class BPlusTreeLeafIterator<T> extends AbstractIterator<T> {

  private int currentIndex = 0;

  private final List<T> currentValues = new ArrayList<>();

  private final boolean key;

  private final BPlusTreeMap<?, ?> map;

  private final int modCount;

  private int nextPageId = 0;

  public BPlusTreeLeafIterator(final BPlusTreeMap<?, ?> map, final boolean key) {
    this.map = map;
    this.key = key;
    this.modCount = map.getModCount();
  }

  @Override
  protected T getNext() throws NoSuchElementException {
    if (this.map.getModCount() == this.modCount) {
      while (this.currentValues.isEmpty() || this.currentIndex >= this.currentValues.size()) {
        if (this.nextPageId < 0) {
          throw new NoSuchElementException();
        } else {
          this.nextPageId = this.map.getLeafValues(this.currentValues, this.nextPageId, this.key);
        }
      }
      final T value = this.currentValues.get(this.currentIndex++);
      return value;
    } else {
      throw new ConcurrentModificationException();
    }
  }
}
