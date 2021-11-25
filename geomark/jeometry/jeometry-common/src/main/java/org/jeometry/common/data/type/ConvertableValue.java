package org.jeometry.common.data.type;

import java.sql.Clob;
import java.sql.SQLException;

import org.jeometry.common.data.identifier.Identifier;

public interface ConvertableValue {

  default Boolean getBoolean() {
    return getValue(DataTypes.BOOLEAN);
  }

  default boolean getBoolean(final boolean defaultValue) {
    final Object value = getValue(DataTypes.BOOLEAN);
    if (value == null) {
      return defaultValue;
    } else if (value instanceof Boolean) {
      return (Boolean)value;
    } else {
      return Boolean.parseBoolean(value.toString());
    }
  }

  default Byte getByte() {
    return getValue(DataTypes.BYTE);
  }

  default byte getByte(final byte defaultValue) {
    final Byte value = getByte();
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  default Double getDouble() {
    return getValue(DataTypes.DOUBLE);
  }

  default double getDouble(final double defaultValue) {
    final Double value = getDouble();
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  default <E extends Enum<E>> E getEnum(final Class<E> enumType) {
    final String value = getString();
    if (value == null) {
      return null;
    } else {
      return Enum.valueOf(enumType, value);
    }
  }

  default Float getFloat() {
    return getValue(DataTypes.FLOAT);
  }

  default float getFloat(final float defaultValue) {
    final Float value = getFloat();
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  default Identifier getIdentifier() {
    final Object value = getValue();
    return Identifier.newIdentifier(value);
  }

  default Integer getInteger() {
    return getValue(DataTypes.INT);
  }

  default int getInteger(final int defaultValue) {
    final Integer value = getInteger();
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  default Long getLong() {
    return getValue(DataTypes.LONG);
  }

  default long getLong(final long defaultValue) {
    final Long value = getLong();
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  default Short getShort() {
    return getValue(DataTypes.SHORT);
  }

  default short getShort(final short defaultValue) {
    final Short value = getShort();
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  default String getString() {
    final Object value = getValue();
    if (value == null) {
      return null;
    } else if (value instanceof String) {
      return value.toString();
    } else if (value instanceof Clob) {
      final Clob clob = (Clob)value;
      try {
        return clob.getSubString(1, (int)clob.length());
      } catch (final SQLException e) {
        throw new RuntimeException("Unable to read clob", e);
      }
    } else {
      return DataTypes.toString(value);
    }
  }

  default String getString(final String defaultValue) {
    final String value = getString();
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  default String getUpperString() {
    final String string = getString();
    if (string == null) {
      return null;
    } else {
      return string.toUpperCase();
    }
  }

  <T extends Object> T getValue();

  default <T extends Object> T getValue(final DataType dataType) {
    final Object value = getValue();
    return dataType.toObject(value);
  }

  default <T extends Object> T getValue(final DataType dataType, final T defaultValue) {
    final T value = getValue(dataType);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  default <T extends Object> T getValue(final T defaultValue) {
    final T value = getValue();
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  default boolean hasValue() {
    final Object value = getValue();
    return value != null;
  }

}
