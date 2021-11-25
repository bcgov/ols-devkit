package org.jeometry.common.compare;

import java.math.BigDecimal;
import java.util.Comparator;

import org.jeometry.common.number.BigDecimals;

public class NumericComparator<T> implements Comparator<T> {

  public static int nullLastCompare(final Integer number1, final Integer number2) {
    if (number1 == null) {
      if (number2 == null) {
        return 0;
      } else {
        return 1;
      }
    } else if (number2 == null) {
      return -1;
    } else {
      return number1.compareTo(number2);
    }
  }

  public static int nullLastCompare(final Object value1, final Object value2) {
    if (value1 == null) {
      if (value2 == null) {
        return 0;
      } else {
        return 1;
      }
    } else if (value2 == null) {
      return -1;
    } else {
      BigDecimal number1 = null;
      try {
        number1 = BigDecimals.toValid(value1);
      } catch (final Exception e) {
      }
      BigDecimal number2 = null;
      try {
        number2 = BigDecimals.toValid(value2);
      } catch (final Exception e) {
      }
      if (number1 == null) {
        if (number2 == null) {
          return value1.toString().compareTo(value2.toString());
        } else {
          return 1;
        }
      } else {
        if (number2 == null) {
          return -1;
        } else {
          return number1.compareTo(number2);
        }
      }
    }
  }

  public static int numericCompare(final Object value1, final Object value2) {
    if (value1 == null) {
      if (value2 == null) {
        return 0;
      } else {
        return -1;
      }
    } else if (value2 == null) {
      return 1;
    } else {
      BigDecimal number1 = null;
      try {
        number1 = BigDecimals.toValid(value1);
      } catch (final Exception e) {
      }
      BigDecimal number2 = null;
      try {
        number2 = BigDecimals.toValid(value2);
      } catch (final Exception e) {
      }
      if (number1 == null) {
        if (number2 == null) {
          return value1.toString().compareTo(value2.toString());
        } else {
          return -1;
        }
      } else {
        if (number2 == null) {
          return 1;
        } else {
          return number1.compareTo(number2);
        }
      }
    }
  }

  @Override
  public int compare(final T value1, final T value2) {
    return numericCompare(value1, value2);
  }
}
