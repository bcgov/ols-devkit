package org.jeometry.common.number;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class Doubles {
  public static final double[] EMPTY_ARRAY = new double[0];

  private static final char[] DIGIT_ONES = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8',
    '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7',
    '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6',
    '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5',
    '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4',
    '5', '6', '7', '8', '9',
  };

  private static final char[] DIGIT_TENS = {
    '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '1', '1', '1', '1', '1', '1', '1', '1',
    '1', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '3', '3', '3', '3', '3', '3', '3', '3',
    '3', '3', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '5', '5', '5', '5', '5', '5', '5',
    '5', '5', '5', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '7', '7', '7', '7', '7', '7',
    '7', '7', '7', '7', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '9', '9', '9', '9', '9',
    '9', '9', '9', '9', '9',
  };

  private final static char[] DIGITS = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
    'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
  };

  private static final double MIN_VALUE = 4.999999999999999 / 1e20;

  private static final double[] POWERS_OF_TEN_DOUBLE = new double[30];

  private static final long[] POWERS_OF_TEN_LONG = new long[19];

  static {
    POWERS_OF_TEN_LONG[0] = 1L;
    for (int i = 1; i < POWERS_OF_TEN_LONG.length; i++) {
      POWERS_OF_TEN_LONG[i] = POWERS_OF_TEN_LONG[i - 1] * 10L;
    }
    for (int i = 0; i < POWERS_OF_TEN_DOUBLE.length; i++) {
      POWERS_OF_TEN_DOUBLE[i] = Double.parseDouble("1e" + i);
    }
  }

  public static final int BYTES_IN_DOUBLE = 8;

  public static final String MAX_DOUBLE_STRING = toString(Double.MAX_VALUE);

  public static final String MIN_DOUBLE_STRING = toString(-Double.MAX_VALUE);

  public static double add(final double left, final Number right) {
    return left + right.doubleValue();
  }

  public static void append(final StringBuilder string, double number) {
    if (Double.isNaN(number)) {
      string.append("NaN");
    } else if (number == 0.0 || Math.abs(number) < MIN_VALUE) {
      string.append("0");
    } else if (!Double.isFinite(number)) {
      string.append(number);
    } else {
      final boolean negative = number < 0.0;
      if (negative) {
        number = -number;
        string.append('-');
      }
      // The only way to format precisely the double is to use the String
      // representation of the double, and then to do mathematical integer
      // operation on it.
      final String s = Double.toString(number);
      if (number >= 1e-3 && number < 1e7) {
        // Plain representation of double: "intPart.decimalPart"
        final int dot = s.indexOf('.');
        String decS = s.substring(dot + 1);
        int decLength = decS.length();
        if (19 >= decLength) {
          if ("0".equals(decS)) {
            // source is a mathematical integer
            string.append(s.substring(0, dot));
          } else {
            string.append(s);
            // Remove trailing zeroes
            for (int l = string.length() - 1; l >= 0 && string.charAt(l) == '0'; l--) {
              string.setLength(l);
            }
          }
          return;
        } else if (20 < decLength) {
          // ignore unnecessary DIGITS
          decLength = 20;
          decS = decS.substring(0, decLength);
        }
        final long intP = Long.parseLong(s.substring(0, dot));
        final long decP = Long.parseLong(decS);
        format(string, 19, intP, decP);
      } else {
        // Scientific representation of double: "x.xxxxxEyyy"
        final int dot = s.indexOf('.');
        final int exp = s.indexOf('E');
        int exposant = Integer.parseInt(s.substring(exp + 1));
        final String intS = s.substring(0, dot);
        final String decS = s.substring(dot + 1, exp);
        final int decLength = decS.length();
        if (exposant >= 0) {
          final int digits = decLength - exposant;
          if (digits <= 0) {
            // no decimal part,
            // no rounding involved
            string.append(intS);
            string.append(decS);
            for (int i = -digits; i > 0; i--) {
              string.append('0');
            }
          } else if (digits <= 19) {
            // decimal part precision is lower than scale,
            // no rounding involved
            string.append(intS);
            string.append(decS.substring(0, exposant));
            string.append('.');
            string.append(decS.substring(exposant));
          } else {
            // decimalDigits > scale,
            // Rounding involved
            final long intP = Long.parseLong(intS) * tenPow(exposant)
              + Long.parseLong(decS.substring(0, exposant));
            final long decP = Long.parseLong(decS.substring(exposant, exposant + 20));
            format(string, 19, intP, decP);
          }
        } else {
          // Only a decimal part is supplied
          exposant = -exposant;
          final int digits = 19 - exposant + 1;
          if (digits < 0) {
            string.append('0');
          } else if (digits == 0) {
            final long decP = Long.parseLong(intS);
            format(string, 19, 0L, decP);
          } else if (decLength < digits) {
            final long decP = Long.parseLong(intS) * tenPow(decLength + 1)
              + Long.parseLong(decS) * 10;
            format(string, exposant + decLength, 0L, decP);
          } else {
            final long subDecP = Long.parseLong(decS.substring(0, digits));
            final long decP = Long.parseLong(intS) * tenPow(digits) + subDecP;
            format(string, 19, 0L, decP);
          }
        }
      }
    }
  }

  public static double avg(final double a, final double b) {
    return (a + b) / 2d;
  }

  /**
   * Clamps a double value to a given range.
   * @param x the value to clamp
   * @param min the minimum value of the range
   * @param max the maximum value of the range
   * @return the clamped value
   */
  public static double clamp(final double x, final double min, final double max) {
    if (x < min) {
      return min;
    }
    if (x > max) {
      return max;
    }
    return x;
  }

  public static double divide(final double left, final Number right) {
    return left / right.doubleValue();
  }

  public static boolean equal(final double number1, final double number2) {
    if (Double.compare(number1, number2) == 0) {
      return true;
    } else {
      return false;
    }
  }

  public static boolean equal(final Object number1, final Object number2) {
    return equal((double)number1, (double)number2);
  }

  private static void format(final StringBuilder target, int scale, long intP, long decP) {
    if (decP != 0L) {
      // decP is the decimal part of source, truncated to scale + 1 digit.
      // Custom rounding: add 5
      decP += 5L;
      decP /= 10L;
      if (decP >= tenPowDouble(scale)) {
        intP++;
        decP -= tenPow(scale);
      }
      if (decP != 0L) {
        // Remove trailing zeroes
        while (decP % 10L == 0L) {
          decP = decP / 10L;
          scale--;
        }
      }
    }
    target.append(intP);
    if (decP != 0L) {
      target.append('.');
      // Use tenPow instead of tenPowDouble for scale below 18,
      // since the casting of decP to double may cause some imprecisions:
      // E.g. for decP = 9999999999999999L and scale = 17,
      // decP < tenPow(16) while (double) decP == tenPowDouble(16)
      while (scale > 0 && (scale > 18 ? decP < tenPowDouble(--scale) : decP < tenPow(--scale))) {
        // Insert leading zeroes
        target.append('0');
      }
      target.append(decP);
    }
  }

  public static int hashCode(final double d) {
    final long f = Double.doubleToLongBits(d);
    return (int)(f ^ f >>> 32);
  }

  public static double hypot(final double a, final double b) {
    // sqrt(a^2 + b^2) without under/overflow.
    double r;
    if (Math.abs(a) > Math.abs(b)) {
      r = b / a;
      r = Math.abs(a) * Math.sqrt(1 + r * r);
    } else if (b != 0) {
      r = a / b;
      r = Math.abs(b) * Math.sqrt(1 + r * r);
    } else {
      r = 0.0;
    }
    return r;
  }

  public static double makePrecise(final double scale, final double value) {
    if (scale <= 0) {
      return value;
    } else if (Double.isFinite(value)) {
      final double multiple = value * scale;
      final long scaledValue = Math.round(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  public static double makePreciseCeil(final double scale, final double value) {
    if (scale <= 0) {
      return value;
    } else if (Double.isFinite(value)) {
      final double multiple = value * scale;
      final long scaledValue = (long)Math.ceil(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  public static double makePreciseFloor(final double scale, final double value) {
    if (scale <= 0) {
      return value;
    } else if (Double.isFinite(value)) {
      final double multiple = value * scale;
      final long scaledValue = (long)Math.floor(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
    } else {
      return value;
    }
  }

  public static double max(final double... values) {
    double max = -Double.MAX_VALUE;
    for (final double value : values) {
      if (value > max) {
        max = value;
      }
    }
    return max;
  }

  public static double midpoint(final double d1, final double d2) {
    return d1 + (d2 - d1) / 2;
  }

  public static double min(final double... values) {
    double min = Double.MAX_VALUE;
    for (final double value : values) {
      if (value < min) {
        min = value;
      }
    }
    return min;
  }

  public static double mod(final double left, final Number right) {
    return left % right.doubleValue();
  }

  public static double multiply(final double left, final Number right) {
    return left * right.doubleValue();
  }

  public static boolean overlaps(final double min1, final double max1, final double min2,
    final double max2) {
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

  private static int parseInt(final String string, final int fromIndex, final int toIndex) {
    int number = 0;
    int index = fromIndex;
    boolean negative = false;
    if (string.charAt(index) == '-') {
      negative = true;
      index++;
    }
    while (index < toIndex) {
      final int digit = string.charAt(index++) - '0';
      number = number * 10 + digit;
    }
    if (negative) {
      return -number;
    } else {
      return number;
    }
  }

  private static long parseLong(final String string, final int fromIndex, final int toIndex) {
    long number = 0;
    int index = fromIndex;
    boolean negative = false;
    if (string.charAt(index) == '-') {
      negative = true;
      index++;
    }
    while (index < toIndex) {
      final int digit = string.charAt(index++) - '0';
      number = number * 10 + digit;
    }
    if (negative) {
      return -number;
    } else {
      return number;
    }
  }

  public static int sgn(final double x) {
    if (x > 0.0D) {
      return 1;
    }
    if (x < 0.0D) {
      return -1;
    }
    return 0;
  }

  public static double subtract(final double left, final Number right) {
    return left - right.doubleValue();
  }

  private static long tenPow(final int n) {
    return n < POWERS_OF_TEN_LONG.length ? POWERS_OF_TEN_LONG[n] : (long)Math.pow(10, n);
  }

  private static double tenPowDouble(final int n) {
    return n < POWERS_OF_TEN_DOUBLE.length ? POWERS_OF_TEN_DOUBLE[n] : Math.pow(10, n);
  }

  public static Double toDouble(final Object value) {
    try {
      return toValid(value);
    } catch (final Throwable e) {
      return null;
    }
  }

  public static Double toDouble(final String value) {
    try {
      return toValid(value);
    } catch (final Throwable e) {
      return null;
    }
  }

  public static double[] toDoubleArray(final List<? extends Number> numbers) {
    final double[] doubles = new double[numbers.size()];
    for (int i = 0; i < doubles.length; i++) {
      final Number number = numbers.get(i);
      doubles[i] = number.doubleValue();
    }
    return doubles;
  }

  public static double[] toDoubleArray(final String... values) {
    final double[] doubles = new double[values.length];
    for (int i = 0; i < doubles.length; i++) {
      doubles[i] = Double.valueOf(values[i]);
    }
    return doubles;
  }

  public static double[] toDoubleArraySplit(final String value) {
    return toDoubleArray(value.split(","));
  }

  public static double[] toDoubleArraySplit(final String value, final String regex) {
    return toDoubleArray(value.split(regex));
  }

  public static String toString(final double number) {
    final StringBuilder string = new StringBuilder();
    append(string, number);
    return string.toString();
  }

  /**
   * Convert the value to a Double. If the value cannot be converted to a number
   * an exception is thrown
   *
   * @param value The value to convert.
   * @return the converted value.
   */
  public static Double toValid(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.doubleValue();
    } else {
      final String string = value.toString();
      return toValid(string);
    }
  }

  /**
   * Convert the value to a Double. If the value cannot be converted to a number and exception is thrown.
   *
   * @param string The value to convert.
   * @return the converted value.
   */
  public static Double toValid(final String string) {
    if (string == null || string.length() == 0) {
      return null;
    } else {
      return Double.valueOf(string);
    }
  }

  public static void write(final Writer writer, final double number) throws IOException {
    int numberStartIndex = 0;

    // The only way to format precisely the double is to use the String
    // representation of the double, and then to do mathematical integer
    // operation on it.
    final String doubleString = Double.toString(number);
    if (doubleString.charAt(0) == '-') {
      writer.write('-');
      numberStartIndex++;
    }
    final int doubleStringLength = doubleString.length();
    int exponentIndex = -1;
    if (doubleStringLength > 4) {
      final int eMaxIndex = doubleStringLength - 2;
      int eMinIndex = eMaxIndex - 3;
      if (eMinIndex < 2) {
        eMinIndex = 2;
      }
      for (int i = eMaxIndex; i >= eMinIndex; i--) {
        final char c = doubleString.charAt(i);
        if (c == 'E') {
          exponentIndex = i;
          break;
        }
      }
    }
    if (exponentIndex == -1) {
      // Plain representation of double: "intPart.decimalPart"
      if (doubleString.charAt(doubleStringLength - 1) == '0'
        && doubleString.charAt(doubleStringLength - 2) == '.') {
        // source is a mathematical integer
        writer.write(doubleString, numberStartIndex, doubleStringLength - numberStartIndex - 2);
      } else {
        writer.write(doubleString, numberStartIndex, doubleStringLength - numberStartIndex);
      }
    } else {
      int exposant = parseInt(doubleString, exponentIndex + 1, doubleStringLength);
      final int decLength = exponentIndex - 2 - numberStartIndex;
      final int decimalStartIndex = numberStartIndex + 2;
      if (exposant >= 0) {
        final int digits = decLength - exposant;
        if (digits <= 0) {
          // no decimal part,
          writer.write(doubleString, numberStartIndex, 1);
          writer.write(doubleString, decimalStartIndex, decLength);
          for (int i = -digits; i > 0; i--) {
            writer.write('0');
          }
        } else {
          writer.write(doubleString, numberStartIndex, 1);
          writer.write(doubleString, decimalStartIndex, exposant);
          writer.write('.');
          writer.write(doubleString, decimalStartIndex + exposant, decLength - exposant);
        }
      } else {
        // Only a decimal part is supplied
        exposant = -exposant;
        final int digits = 19 - exposant + 1;
        if (digits < 0) {
          writer.write('0');
        } else {
          final int integerPart = doubleString.charAt(numberStartIndex) - '0';
          if (digits == 0) {
            write(writer, 19, 0L, integerPart);
          } else if (decLength < digits) {
            final long decP = integerPart * tenPow(decLength + 1)
              + parseLong(doubleString, decimalStartIndex, exponentIndex) * 10;
            write(writer, exposant + decLength, 0L, decP);
          } else {
            final long subDecP = parseLong(doubleString, decimalStartIndex,
              decimalStartIndex + digits);
            final long decP = integerPart * tenPow(digits) + subDecP;
            write(writer, 19, 0L, decP);
          }
        }
      }
    }
  }

  /**
   * Helper method to do the custom rounding used within formatDoublePrecise
   *
   * @param writer the buffer to write to
   * @param scale the expected rounding scale
   * @param intP the source integer part
   * @param decP the source decimal part, truncated to scale + 1 digit
   * @throws IOException if the value couldn't be written.
   */
  public static void write(final Writer writer, int scale, long intP, long decP)
    throws IOException {
    if (decP != 0L) {
      // decP is the decimal part of source, truncated to scale + 1 digit.
      // Custom rounding: add 5
      decP += 5L;
      decP /= 10L;
      if (decP >= tenPowDouble(scale)) {
        intP++;
        decP -= tenPow(scale);
      }
      if (decP != 0L) {
        // Remove trailing zeroes
        while (decP % 10L == 0L) {
          decP = decP / 10L;
          scale--;
        }
      }
    }
    writePositiveLong(writer, intP);
    if (decP != 0L) {
      writer.write('.');
      // Use tenPow instead of tenPowDouble for scale below 18,
      // since the casting of decP to double may cause some imprecisions:
      // E.g. for decP = 9999999999999999L and scale = 17,
      // decP < tenPow(16) while (double) decP == tenPowDouble(16)
      while (scale > 0 && (scale > 18 ? decP < tenPowDouble(--scale) : decP < tenPow(--scale))) {
        // Insert leading zeroes
        writer.write('0');
      }
      writePositiveLong(writer, decP);
    }
  }

  private static void writePositiveLong(final Writer writer, final long number) throws IOException {
    long power = 10;
    int size = 1;
    while (size < 19) {
      if (number < power) {
        break;
      } else {
        size++;
        power = 10 * power;
      }
    }
    final char[] buf = new char[size];
    long i = number;
    long q;
    int r;
    int charPos = size;

    // Get 2 DIGITS/iteration using longs until quotient fits into an int
    while (i > Integer.MAX_VALUE) {
      q = i / 100;
      // really: r = i - (q * 100);
      r = (int)(i - ((q << 6) + (q << 5) + (q << 2)));
      i = q;
      buf[--charPos] = DIGIT_ONES[r];
      buf[--charPos] = DIGIT_TENS[r];
    }

    // Get 2 DIGITS/iteration using ints
    int q2;
    int i2 = (int)i;
    while (i2 >= 65536) {
      q2 = i2 / 100;
      // really: r = i2 - (q * 100);
      r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
      i2 = q2;
      buf[--charPos] = DIGIT_ONES[r];
      buf[--charPos] = DIGIT_TENS[r];
    }

    // Fall thru to fast mode for smaller numbers
    // assert(i2 <= 65536, i2);
    for (;;) {
      q2 = i2 * 52429 >>> 16 + 3;
      r = i2 - ((q2 << 3) + (q2 << 1)); // r = i2-(q2*10) ...
      buf[--charPos] = DIGITS[r];
      i2 = q2;
      if (i2 == 0) {
        break;
      }
    }
    writer.write(buf);
  }
}
