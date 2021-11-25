package com.revolsys.comparator;

import java.util.Comparator;

public class IgnoreCaseStringComparator implements Comparator<String> {
  @Override
  public int compare(final String s1, final String s2) {
    if (s1 == null) {
      if (s2 == null) {
        return 0;
      } else {
        return 1;
      }
    }
    if (s2 == null) {
      return -1;
    } else {
      int compare = s1.compareToIgnoreCase(s2);
      if (compare == 0) {
        compare = s1.compareTo(s2);
      }
      return compare;
    }
  }
}
