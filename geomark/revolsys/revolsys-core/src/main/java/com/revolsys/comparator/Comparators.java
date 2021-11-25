package com.revolsys.comparator;

import java.util.Comparator;

public class Comparators {

  public static <T extends Comparable<T>> Comparator<T> newComparator() {
    return (final T o1, final T o2) -> o1.compareTo(o2);
  }

  private Comparators() {
  }
}
