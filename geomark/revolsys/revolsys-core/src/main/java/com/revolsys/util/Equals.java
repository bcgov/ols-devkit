package com.revolsys.util;

public interface Equals {
  static boolean equals(final Object object1, final Object object2) {
    if (object1 == object2) {
      return true;
    } else if (object1 == null) {
      return false;
    } else if (object2 == null) {
      return false;
    } else {
      return object1.equals(object2);
    }
  }
}
