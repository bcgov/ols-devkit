package org.jeometry.common.number;

import java.math.BigInteger;

public class BigIntegers {
  public static boolean equalsNotNull(final BigInteger number1, final BigInteger number2) {
    if (number1.compareTo(number2) == 0) {
      return true;
    } else {
      return false;
    }
  }

  public static String toString(final BigInteger number) {
    return number.toString();
  }

}
