package com.revolsys.comparator;

import java.util.Comparator;

public class IntArrayComparator implements Comparator<int[]> {
  @Override
  public int compare(final int[] object1, final int[] object2) {
    for (int i = 0; i < Math.max(object1.length, object2.length); i++) {
      if (i >= object1.length) {
        return -1;
      } else if (i >= object2.length) {
        return 1;
      } else {
        final int value1 = object1[i];
        final int value2 = object2[i];
        if (value1 < value2) {
          return -1;
        } else if (value1 > value2) {
          return 1;
        }
      }
    }
    return 0;
  }
}
