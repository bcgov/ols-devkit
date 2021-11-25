package org.jeometry.common.data.type;

import java.math.BigInteger;

public class BigIntegerDataType extends AbstractDataType {
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

  public static BigInteger toValid(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof BigInteger) {
      final BigInteger number = (BigInteger)value;
      return number;
    } else {
      final String string = DataTypes.toString(value);
      return new BigInteger(string);
    }
  }

  public BigIntegerDataType() {
    super("integer", BigInteger.class, false);
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2) {
    return equalsNotNull((BigInteger)value1, (BigInteger)value2);
  }

  @Override
  protected Object toObjectDo(final Object value) {
    final String string = DataTypes.toString(value);
    final BigInteger integer = new BigInteger(string);
    return integer;
  }

  @Override
  protected String toStringDo(final Object value) {
    return ((BigInteger)value).toString();
  }
}
