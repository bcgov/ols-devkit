package org.jeometry.common.number;

public class Bytes {
  public static final byte[] EMPTY_ARRAY = new byte[0];

  public static byte add(final byte left, final Number right) {
    return (byte)(left + right.byteValue());
  }

  public static byte divide(final byte left, final Number right) {
    return (byte)(left / right.byteValue());
  }

  public static byte mod(final byte left, final Number right) {
    return (byte)(left % right.byteValue());
  }

  public static byte multiply(final byte left, final Number right) {
    return (byte)(left * right.byteValue());
  }

  public static byte sgn(final byte x) {
    if (x > 0) {
      return 1;
    }
    if (x < 0) {
      return -1;
    }
    return 0;
  }

  public static byte subtract(final byte left, final Number right) {
    return (byte)(left - right.byteValue());
  }

  public static String toString(final byte number) {
    return String.valueOf(number);
  }

  /**
   * Convert the value to a Byte. If the value cannot be converted to a number
   * an exception is thrown
   *
   * @param value The value to convert.
   * @return the converted value.
   */
  public static Byte toValid(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.byteValue();
    } else {
      final String string = value.toString();
      return toValid(string);
    }
  }

  /**
   * Convert the value to a Byte. If the value cannot be converted to a number and exception is thrown.
   *
   * @param string The value to convert.
   * @return the converted value.
   */
  public static Byte toValid(final String string) {
    if (string == null) {
      return null;
    } else {
      boolean negative = false;
      int index = 0;
      final int length = string.length();
      int limit = -Byte.MAX_VALUE;

      if (length == 0) {
        return null;
      } else {
        final char firstChar = string.charAt(0);
        switch (firstChar) {
          case '-':
            negative = true;
            limit = Byte.MIN_VALUE;
          case '+':
            // The following applies to both + and - prefixes
            if (length == 1) {
              throw new IllegalArgumentException(string + " is not a valid int");
            }
            index++;
          break;
        }
        final int multmin = limit / 10;
        byte result = 0;
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
          return (byte)-result;
        }
      }
    }
  }

}
