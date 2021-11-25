package org.jeometry.common.number;

public class Longs {
  public static final int BYTES_IN_LONG = 8;

  public static final long[] EMPTY_ARRAY = new long[0];

  public static long add(final long left, final Number right) {
    return left + right.longValue();
  }

  public static int compareDistance(final long x1, final long y1, final long x2, final long y2) {
    final double distance1 = Math.sqrt(x1 * x1 + y1 * y1);
    final double distance2 = Math.sqrt(x2 * x2 + y2 * y2);
    int compare = Double.compare(distance1, distance2);
    if (compare == 0) {
      compare = Long.compare(y1, y2);
      if (compare == 0) {
        compare = Long.compare(x1, x2);
      }
    }
    return compare;
  }

  public static long divide(final long left, final Number right) {
    return left / right.longValue();
  }

  public static boolean equals(final long value1, final Object value2) {
    if (value2 == null) {
      return false;
    } else {
      final Long long2 = toValid(value2);
      if (long2 == null) {
        return false;
      } else {
        return value1 == long2;
      }
    }
  }

  public static long mod(final long left, final Number right) {
    return left % right.longValue();
  }

  public static long multiply(final long left, final Number right) {
    return left * right.longValue();
  }

  public static int sgn(final long x) {
    if (x > 0L) {
      return 1;
    }
    if (x < 0L) {
      return -1;
    }
    return 0;
  }

  public static long subtract(final long left, final Number right) {
    return left - right.longValue();
  }

  public static long toLong(final byte[] bytes, final int offset) {
    final long high = (long)Integers.toInt(bytes, offset) << 32;
    final long low = (long)Integers.toInt(bytes, offset + 4) << 32 >>> 32;
    return high | low;
  }

  public static long toLong(final int upperInt, final int lowerInt) {
    final long lower = lowerInt & 0xffffffffL;
    final long upper = upperInt & 0xffffffffL;
    final long l = upper << 32 | lower;
    return l;
  }

  public static String toString(final long number) {
    return String.valueOf(number);
  }

  /**
   * Convert the value to a Long. If the value cannot be converted to a number
   * an exception is thrown
   *
   * @param value The value to convert.
   * @return the converted value.
   */
  public static Long toValid(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.longValue();
    } else {
      final String string = value.toString();
      return toValid(string);
    }
  }

  /**
   * Convert the value to a Long. If the value cannot be converted to a number and exception is thrown.
   *
   * @param string The value to convert.
   * @return the converted value.
   */
  public static Long toValid(final String string) {
    if (string == null) {
      return null;
    } else {
      boolean negative = false;
      int index = 0;
      final int length = string.length();
      long limit = -Long.MAX_VALUE;

      if (length == 0) {
        return null;
      } else {
        final char firstChar = string.charAt(0);
        switch (firstChar) {
          case '-':
            negative = true;
            limit = Long.MIN_VALUE;
          case '+':
            // The following applies to both + and - prefixes
            if (length == 1) {
              throw new IllegalArgumentException(string + " is not a valid int");
            }
            index++;
          break;
        }
        final long multmin = limit / 10;
        long result = 0;
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
                throw new IllegalArgumentException(string + " is not a valid int");
              }
              final int digit = character - '0';
              result *= 10;
              if (result < limit + digit) {
                throw new IllegalArgumentException(string + " is not a valid int");
              }
              result -= digit;
            break;
            default:
              throw new IllegalArgumentException(string + " is not a valid int");
          }
        }
        if (negative) {
          return result;
        } else {
          return (long)-result;
        }
      }
    }
  }

}
