package com.revolsys.util;

import org.jeometry.common.data.type.DataTypes;

public interface Booleans {
  static boolean getBoolean(final Object value) {
    final Boolean bool = valueOf(value);
    return bool != null && bool;
  }

  static boolean isFalse(final Object value) {
    try {
      final Boolean bool = valueOf(value);
      if (bool == null) {
        return false;
      } else {
        return !bool;
      }
    } catch (final IllegalArgumentException e) {
      return false;
    }
  }

  static boolean isTrue(final Object value) {
    try {
      final Boolean bool = valueOf(value);
      if (bool == null) {
        return false;
      } else {
        return bool;
      }
    } catch (final IllegalArgumentException e) {
      return false;
    }
  }

  static Boolean valueOf(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Boolean) {
      return (Boolean)value;
    } else {
      final String string = DataTypes.toString(value);
      return valueOf(string);
    }
  }

  static Boolean valueOf(final String string) {
    if (Property.hasValue(string)) {
      if ("1".equals(string)) {
        return true;
      } else if ("Y".equalsIgnoreCase(string)) {
        return true;
      } else if ("on".equals(string)) {
        return true;
      } else if ("true".equalsIgnoreCase(string)) {
        return true;
      } else if ("0".equals(string)) {
        return false;
      } else if ("N".equalsIgnoreCase(string)) {
        return false;
      } else if ("off".equals(string)) {
        return false;
      } else if ("false".equalsIgnoreCase(string)) {
        return false;
      } else {
        throw new IllegalArgumentException(string + " is not a valid boolean");
      }
    } else {
      return null;
    }
  }
}
