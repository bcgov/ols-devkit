package org.jeometry.common.data.type;

public class ShortDataType extends AbstractDataType {
  public static short add(final short left, final Number right) {
    return (short)(left + right.shortValue());
  }

  public static short divide(final short left, final Number right) {
    return (short)(left / right.shortValue());
  }

  public static short mod(final short left, final Number right) {
    return (short)(left % right.shortValue());
  }

  public static short multiply(final short left, final Number right) {
    return (short)(left * right.shortValue());
  }

  public static boolean overlaps(final short min1, final short max1, final short min2,
    final short max2) {
    if (min1 > max1) {
      return overlaps(max1, min1, min2, max2);
    } else if (min2 > max2) {
      return overlaps(min1, max1, max2, min2);
    } else {
      if (min1 <= max2 && min2 <= max1) {
        return true;
      } else {
        return false;
      }
    }
  }

  public static short subtract(final short left, final Number right) {
    return (short)(left - right.shortValue());
  }

  public static Short toShort(final Object value) {
    try {
      return toValid(value);
    } catch (final Throwable e) {
      return null;
    }
  }

  public static Short toShort(final String value) {
    try {
      return toValid(value);
    } catch (final Throwable e) {
      return null;
    }
  }

  public static String toString(final short number) {
    return String.valueOf(number);
  }

  /**
   * Convert the value to a Short. If the value cannot be converted to a number
   * an exception is thrown
   *
   * @param value The value to convert.
   * @return the converted value.
   */
  public static Short toValid(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.shortValue();
    } else {
      final String string = value.toString();
      return toValid(string);
    }
  }

  /**
   * Convert the value to a Short. If the value cannot be converted to a number and exception is thrown.
   *
   * @param string The value to convert.
   * @return the converted value.
   */
  public static Short toValid(final String string) {
    if (string == null) {
      return null;
    } else {
      boolean negative = false;
      int index = 0;
      final int length = string.length();
      int limit = -Short.MAX_VALUE;

      if (length == 0) {
        return null;
      } else {
        final char firstChar = string.charAt(0);
        switch (firstChar) {
          case '-':
            negative = true;
            limit = Short.MIN_VALUE;
          case '+':
            // The following applies to both + and - prefixes
            if (length == 1) {
              throw new IllegalArgumentException(string + " is not a valid short");
            }
            index++;
          break;
        }
        final int multmin = limit / 10;
        short result = 0;
        for (; index < length; index++) {
          final char character = string.charAt(index);
          switch (character) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
              if (result < multmin) {
                throw new IllegalArgumentException(string + " is not a valid short");
              }
              final int digit = character - '0';
              result *= 10;
              if (result < limit + digit) {
                throw new IllegalArgumentException(string + " is not a valid short");
              }
              result -= digit;
            break;
            default:
              throw new IllegalArgumentException(string + " is not a valid short");
          }
        }
        if (negative) {
          return result;
        } else {
          return (short)-result;
        }
      }
    }
  }

  public ShortDataType() {
    super("short", Short.class, false);
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2) {
    return (short)value1 == (short)value2;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getMaxValue() {
    final Short max = Short.MAX_VALUE;
    return (V)max;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getMinValue() {
    final Short min = Short.MIN_VALUE;
    return (V)min;
  }

  @Override
  protected Object toObjectDo(final Object value) {
    final String string = DataTypes.toString(value);
    return toValid(string);
  }

  @Override
  protected String toStringDo(final Object value) {
    return String.valueOf((short)value);
  }
}
