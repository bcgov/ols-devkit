package com.revolsys.collection.bplus;

import java.util.AbstractList;
import java.util.Map;

import org.springframework.util.comparator.ComparableComparator;

import com.revolsys.io.page.PageManager;
import com.revolsys.io.page.PageValueManager;

public class BPlusTreeList<T> extends AbstractList<T> {

  public static <T> BPlusTreeList<T> newList(final PageManager pageManager,
    final PageValueManager<T> valueManager) {
    return new BPlusTreeList<>(pageManager, valueManager);
  }

  int size = 0;

  private final Map<Integer, T> tree;

  public BPlusTreeList(final PageManager pageManager, final PageValueManager<T> valueSerializer) {
    final ComparableComparator<Integer> comparator = new ComparableComparator<>();
    this.tree = BPlusTreeMap.newMap(pageManager, comparator, PageValueManager.INT, valueSerializer);
  }

  @Override
  public void add(final int index, final T value) {
    if (index < 0) {
      throw new IndexOutOfBoundsException("Index must be > 0 not " + index);
    } else if (index > size()) {
      throw new IndexOutOfBoundsException("Index must be <= " + size() + " not " + index);
    } else {
      if (index < this.size) {
        for (int i = this.size; this.size > index; i--) {
          final T oldValue = get(i - 1);
          this.tree.put(i, oldValue);
        }
      }
      this.tree.put(index, value);
    }
    this.size++;
  }

  @Override
  public T get(final int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException("Index must be > 0 not " + index);
    } else if (index >= size()) {
      throw new IndexOutOfBoundsException("Index must be < " + size() + " not " + index);
    } else {
      return this.tree.get(index);
    }
  }

  @Override
  public T set(final int index, final T value) {
    if (index < 0) {
      throw new IndexOutOfBoundsException("Index must be > 0 not " + index);
    } else if (index >= size()) {
      throw new IndexOutOfBoundsException("Index must be < " + size() + " not " + index);
    } else {
      final T oldValue = this.tree.get(index);
      this.tree.put(index, value);
      return oldValue;
    }
  }

  @Override
  public int size() {
    return this.size;
  }

}
