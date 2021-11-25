package com.revolsys.comparator;

import java.util.Comparator;

public interface ComparatorProxy<T> {
  Comparator<T> getComparator();
}
