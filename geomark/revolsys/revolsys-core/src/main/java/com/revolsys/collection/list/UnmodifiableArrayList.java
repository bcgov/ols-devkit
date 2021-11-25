package com.revolsys.collection.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class UnmodifiableArrayList<V> extends ArrayList<V> {
  private static final long serialVersionUID = 1L;

  public UnmodifiableArrayList(final Iterable<? extends V> values) {
    if (values != null) {
      for (final V value : values) {
        super.add(value);
      }
      super.trimToSize();
    }
  }

  public UnmodifiableArrayList(@SuppressWarnings("unchecked") final V... values) {
    if (values != null) {
      for (final V value : values) {
        super.add(value);
      }
      super.trimToSize();
    }
  }

  @Override
  public void add(final int index, final V element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean add(final V e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(final Collection<? extends V> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(final int index, final Collection<? extends V> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void ensureCapacity(final int minCapacity) {
    throw new UnsupportedOperationException();
  }

  @Override
  public V remove(final int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove(final Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeIf(final Predicate<? super V> filter) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void removeRange(final int fromIndex, final int toIndex) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void replaceAll(final UnaryOperator<V> operator) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public V set(final int index, final V element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void sort(final Comparator<? super V> c) {
    throw new UnsupportedOperationException();
  }

}
