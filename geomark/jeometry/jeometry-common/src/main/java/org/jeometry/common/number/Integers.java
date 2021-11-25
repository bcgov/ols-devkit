package org.jeometry.common.number;

import java.util.List;

public class Integers {
  public static final int BYTES_IN_INT = 4;

  public static final int[] EMPTY_ARRAY = new int[0];

  public static int add(final int left, final Number right) {
    return left + right.intValue();
  }

  public static int divide(final int left, final Number right) {
    return left / right.intValue();
  }

  public static boolean equals(final int value1, final Object value2) {
    if (value2 == null) {
      return false;
    } else {
      try {
        final Integer int2 = toValid(value2);
        return value1 == int2;
      } catch (final Exception e) {
        return false;
      }
    }
  }

  public static boolean isInteger(final Object value) {
    return toInteger(value) != null;
  }

  public static boolean isInteger(final String value) {
    return toInteger(value) != null;
  }

  public static int max(final int... values) {
    int max = Integer.MIN_VALUE;
    for (final int value : values) {
      if (value > max) {
        max = value;
      }
    }
    return max;
  }

  public static int max(final Iterable<Integer> numbers) {
    int min = Integer.MIN_VALUE;
    for (final Integer number : numbers) {
      final int value = number.intValue();
      if (value > min) {
        min = value;
      }
    }
    return min;
  }

  public static int min(final int... values) {
    int min = Integer.MAX_VALUE;
    for (final int value : values) {
      if (value < min) {
        min = value;
      }
    }
    return min;
  }

  public static int min(final Iterable<Integer> numbers) {
    int max = Integer.MAX_VALUE;
    for (final Integer number : numbers) {
      final int value = number.intValue();
      if (value < max) {
        max = value;
      }
    }
    return max;
  }

  public static int mod(final int left, final Number right) {
    return left % right.intValue();
  }

  public static int multiply(final int left, final Number right) {
    return left * right.intValue();
  }

  public static boolean overlaps(final int min1, final int max1, final int min2, final int max2) {
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

  public static int sgn(final int x) {
    if (x > 0) {
      return 1;
    }
    if (x < 0) {
      return -1;
    }
    return 0;
  }

  public static int subtract(final int left, final Number right) {
    return left - right.intValue();
  }

  public static int toInt(final byte[] bytes, final int offset) {
    final byte b1 = bytes[offset];
    final byte b2 = bytes[offset + 1];
    final byte b3 = bytes[offset + 2];
    final byte b4 = bytes[offset + 3];
    return b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | b4 & 0xFF;
  }

  public static int[] toIntArray(final List<? extends Number> numbers) {
    final int[] ints = new int[numbers.size()];
    for (int i = 0; i < ints.length; i++) {
      final Number number = numbers.get(i);
      ints[i] = number.intValue();
    }
    return ints;
  }

  public static int[] toIntArray(final String... values) {
    final int[] ints = new int[values.length];
    for (int i = 0; i < ints.length; i++) {
      ints[i] = Integer.parseInt(values[i]);
    }
    return ints;
  }

  public static int[] toIntArraySplit(final String value, final String regex) {
    return toIntArray(value.split(regex));
  }

  /**
   * Convert the value to a Integer. If the value cannot be converted to a number
   * null is returned instead of an exception.
   *
   * @param value The value to convert.
   * @return the converted value.
   */
  public static Integer toInteger(final Object value) {
    try {
      return toValid(value);
    } catch (final Throwable e) {
      return null;
    }
  }

  /**
   * Convert the value to a Integer. If the value cannot be converted to a number
   * null is returned instead of an exception.
   *
   * @param string The value to convert.
   * @return the converted value.
   */
  public static Integer toInteger(final String string) {
    try {
      return toValid(string);
    } catch (final Throwable e) {
      return null;
    }
  }

  public static String toString(final int number) {
    return String.valueOf(number);
  }

  /**
   * Convert the value to a Integer. If the value cannot be converted to a number
   * an exception is thrown
   *
   * @param value The value to convert.
   * @return the converted value.
   */
  public static Integer toValid(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.intValue();
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
  public static Integer toValid(final String string) {
    if (string == null) {
      return null;
    } else {
      boolean negative = false;
      int index = 0;
      final int length = string.length();
      int limit = -Integer.MAX_VALUE;

      if (length == 0) {
        return null;
      } else {
        final char firstChar = string.charAt(0);
        switch (firstChar) {
          case '-':
            negative = true;
            limit = Integer.MIN_VALUE;
          case '+':
            // The following applies to both + and - prefixes
            if (length == 1) {
              throw new IllegalArgumentException(string + " is not a valid int");
            }
            index++;
          break;
        }
        final int multmin = limit / 10;
        int result = 0;
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
          return -result;
        }
      }
    }
  }

  public static int U8_CLAMP(final int n) {
    if (n < 0) {
      return 0;
    } else if (n > 255) {
      return 255;
    } else {
      return n;
    }
  }

  public static int U8_FOLD(final int n) {
    if (n < 0) {
      return n + 256;
    } else if (n > 255) {
      return n - 256;
    } else {
      return n;
    }
  }

}
