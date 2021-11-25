package com.revolsys.util;

import java.util.Comparator;

public class ComparableComparator<T extends Comparable<T>> implements Comparator<T> {

  @Override
  public int compare(final T value1, final T value2) {
    return value1.compareTo(value2);
  }
}
