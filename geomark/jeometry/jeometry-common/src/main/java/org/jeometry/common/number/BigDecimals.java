package org.jeometry.common.number;

import java.math.BigDecimal;
import java.math.BigInteger;

public class BigDecimals {
  /**
   *
   * @param left The left operand.
   * @param right The right operand.
   * @return The new amount.
   */
  public static BigDecimal add(final BigDecimal left, final Number right) {
    return left.add(toValid(right));
  }

  public static boolean equalsNotNull(final BigDecimal number1, final BigDecimal number2) {
    if (number1.compareTo(number2) == 0) {
      return true;
    } else {
      return false;
    }
  }

  public static BigDecimal getBigDecimal(final Object value) {
    if (value == null) {
      return null;
    } else {
      try {
        return toValid(value);
      } catch (final Exception e) {
        return null;
      }
    }
  }

  public static String toString(final BigDecimal number) {
    return number.toPlainString();
  }

  public static BigDecimal toValid(final Number number) {
    BigDecimal decimal;
    if (number instanceof BigDecimal) {
      decimal = (BigDecimal)number;
    } else if (number instanceof Byte) {
      final byte b = (Byte)number;
      decimal = new BigDecimal(b);
    } else if (number instanceof Short) {
      final short s = (Short)number;
      decimal = new BigDecimal(s);
    } else if (number instanceof Integer) {
      final int i = (Integer)number;
      decimal = new BigDecimal(i);
    } else if (number instanceof Long) {
      final long l = (Long)number;
      decimal = new BigDecimal(l);
    } else if (number instanceof Float) {
      final float f = (Float)number;
      decimal = new BigDecimal(f);
    } else if (number instanceof Double) {
      final double d = (Double)number;
      decimal = new BigDecimal(d);
    } else if (number instanceof BigInteger) {
      final BigInteger i = (BigInteger)number;
      decimal = new BigDecimal(i);
    } else {
      final String s = number.toString();
      decimal = new BigDecimal(s);
    }

    return decimal.stripTrailingZeros();
  }

  public static BigDecimal toValid(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return toValid(number);
    } else {
      final String string = value.toString();
      return toValid(string);
    }
  }

  public static BigDecimal toValid(final String string) {
    return new BigDecimal(string).stripTrailingZeros();
  }
}
