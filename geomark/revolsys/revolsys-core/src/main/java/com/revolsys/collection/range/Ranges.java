package com.revolsys.collection.range;

import org.jeometry.common.number.Numbers;

import com.revolsys.util.Property;

public class Ranges {
  private static boolean isNumeric(final RangeSet rangeSet) {
    if (rangeSet == null) {
      return false;
    } else {
      for (final AbstractRange<?> range : rangeSet.getRanges()) {
        if (range instanceof LongRange) {
        } else if (range instanceof IntRange) {
        } else if (range instanceof LongPaddedRange) {
        } else if (range instanceof CrossProductRange) {
          final CrossProductRange crossProduct = (CrossProductRange)range;
          for (final AbstractRange<?> subRange : crossProduct.getRanges()) {
            if (subRange instanceof LongRange) {
            } else if (subRange instanceof IntRange) {
            } else if (subRange instanceof LongPaddedRange) {
            } else {
              return false;
            }
          }
        } else {
          return false;
        }
      }
      return true;
    }
  }

  public static boolean isNumeric(final String rangeSpec) {
    try {
      final RangeSet rangeSet = RangeSet.newRangeSet(rangeSpec);
      return isNumeric(rangeSet);
    } catch (final Throwable e) {
      return false;
    }
  }

  public static AbstractRange<?> newRange(final char value) {
    if (Numbers.isDigit(value)) {
      return new IntRange(value - '0');
    } else if (CharRange.isLowerOrUpper(value)) {
      return new CharRange(value);
    } else {
      return new StringSingletonRange(value);
    }
  }

  public static AbstractRange<?> newRange(final char from, final char to) {
    if (Numbers.isDigit(from) && Numbers.isDigit(to)) {
      return new IntRange(from - '0', to - '0');
    } else {
      return new CharRange(from, to);
    }
  }

  public static AbstractRange<?> newRange(final int value) {
    return new IntRange(value);
  }

  public static AbstractRange<?> newRange(final int from, final int to) {
    return new IntRange(from, to);
  }

  public static AbstractRange<?> newRange(final long value) {
    return new LongRange(value);
  }

  public static AbstractRange<?> newRange(final long from, final long to) {
    return new LongRange(from, to);
  }

  public static AbstractRange<?> newRange(Object value) {
    value = toValue(value);
    if (value == null) {
      return null;
    } else if (value instanceof Long) {
      return newRange(((Long)value).longValue());
    } else if (Numbers.isPrimitiveIntegral(value)) {
      return newRange(((Number)value).intValue());
    } else if (value instanceof Character) {
      final Character character = (Character)value;
      return newRange(character.charValue());
    } else {
      return new StringSingletonRange(value.toString());
    }
  }

  public static AbstractRange<?> newRange(final Object from, final Object to) {
    final Object fromValue = toValue(from);
    final Object toValue = toValue(to);
    if (fromValue == null) {
      return newRange(toValue);
    }
    if (fromValue instanceof Long) {
      final long fromLong = (Long)fromValue;
      if (toValue instanceof Long) {
        final long toLong = (Long)toValue;
        if (fromLong != 0 && from.toString().charAt(0) == '0'
          || toLong != 0 && to.toString().charAt(0) == '0') {
          return new LongPaddedRange(fromLong, toLong);
        } else {
          return newRange(fromLong, toLong);
        }
      } else if (toValue instanceof Integer) {
        final long toLong = (Integer)toValue;
        if (fromLong != 0 && from.toString().charAt(0) == '0'
          || toLong != 0 && to.toString().charAt(0) == '0') {
          return new LongPaddedRange(fromLong, toLong);
        } else {
          return newRange(fromLong, toLong);
        }
      } else {
        throw new RangeInvalidException("Cannot create range from " + fromValue + " (Long) and "
          + toValue + " (" + toValue.getClass().getSimpleName() + ")");
      }
    } else if (fromValue instanceof Integer) {
      final int fromInt = (Integer)fromValue;
      if (toValue instanceof Long) {
        final long toLong = (Long)toValue;
        if (fromInt != 0 && from.toString().charAt(0) == '0'
          || toLong != 0 && to.toString().charAt(0) == '0') {
          return new LongPaddedRange(fromInt, toLong);
        } else {
          return newRange(fromInt, toLong);
        }
      } else if (toValue instanceof Integer) {
        final int toInt = (Integer)toValue;
        if (fromInt != 0 && from.toString().charAt(0) == '0'
          || toInt != 0 && to.toString().charAt(0) == '0') {
          return new LongPaddedRange(fromInt, toInt);
        } else {
          return newRange(fromInt, toInt);
        }
      } else {
        throw new RangeInvalidException("Cannot create range from " + fromValue + " (Long) and "
          + toValue + " (" + toValue.getClass().getSimpleName() + ")");
      }
    } else if (fromValue instanceof Character) {
      final char fromChar = (Character)fromValue;
      if (toValue instanceof Character) {
        final char toChar = (Character)toValue;
        return newRange(fromChar, toChar);
      } else {
        throw new RangeInvalidException("Cannot create range from " + fromValue
          + " (Character) and " + toValue + " (" + toValue.getClass().getSimpleName() + ")");
      }
    } else {
      throw new RangeInvalidException("Cannot create range from " + fromValue + " (String) and "
        + toValue + " (" + toValue.getClass().getSimpleName() + ")");
    }
  }

  public static Object toValue(final Object value) {
    if (value == null) {
      return null;
    } else if (Numbers.isPrimitiveIntegral(value)) {
      final Number number = (Number)value;
      final long longValue = number.longValue();
      final int intValue = (int)longValue;
      if (intValue == longValue) {
        return intValue;
      } else {
        return longValue;
      }
    } else if (value instanceof Character) {
      final Character character = (Character)value;
      return character.charValue();
    } else {
      return toValue(value.toString());
    }
  }

  public static Object toValue(final String value) {
    if (Property.hasValue(value)) {
      final Long longValue = Numbers.toLong(value);
      if (longValue == null) {
        if (value.length() == 1) {
          final char character = value.charAt(0);
          if (CharRange.isLowerOrUpper(character)) {
            return character;
          }
        }
        return value;
      } else {
        final int intValue = longValue.intValue();
        if (intValue == longValue) {
          return intValue;
        } else {
          return longValue;
        }
      }
    } else {
      return null;
    }
  }
}
