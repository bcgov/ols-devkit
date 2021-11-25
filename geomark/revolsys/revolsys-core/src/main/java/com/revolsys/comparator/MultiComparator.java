package com.revolsys.comparator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MultiComparator<T> implements Comparator<T> {
  private List<Comparator<T>> comparators;

  public MultiComparator(final Comparator<T>... comparators) {
    this(Arrays.asList(comparators));
  }

  public MultiComparator(final List<Comparator<T>> comparators) {
    this.comparators = comparators;
  }

  @Override
  public int compare(final T object1, final T object2) {
    for (final Comparator<T> comparator : this.comparators) {
      final int compare = comparator.compare(object1, object2);
      if (compare != 0) {
        return compare;
      }
    }
    return 0;
  }
}
