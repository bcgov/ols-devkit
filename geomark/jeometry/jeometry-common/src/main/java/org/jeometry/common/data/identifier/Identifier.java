package org.jeometry.common.data.identifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jeometry.common.compare.CompareUtil;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.number.Numbers;

public interface Identifier {
  Identifier NULL = new SingleIdentifier(null);

  static Comparator<Identifier> COMPARATOR = (identifier1, identifier2) -> {
    if (identifier1 == identifier2) {
      return 0;
    } else if (identifier1 == null) {
      return 1;
    } else {
      return identifier1.compareTo(identifier2);
    }
  };

  static Comparator<Identifier> comparator() {
    return COMPARATOR;
  }

  /**
   * Check that the two identifiers are equal. If either are null then false will be returned.
   *
   * @param identifier1 The 1st identifier.
   * @param identifier2 The 2nd identifier.
   * @return True if the identifiers are not null and are equal. False otherwise.
   */
  static boolean equals(final Identifier identifier1, final Identifier identifier2) {
    if (identifier1 == null) {
      return false;
    } else if (identifier2 == null) {
      return false;
    } else {
      return identifier1.equals(identifier2);
    }
  }

  static Identifier newIdentifier(final int intValue) {
    return new IntegerIdentifier(intValue);
  }

  static Identifier newIdentifier(final long longValue) {
    if (longValue < Integer.MIN_VALUE || longValue > Integer.MAX_VALUE) {
      return new LongIdentifier(longValue);
    } else {
      final int intValue = (int)longValue;
      return newIdentifier(intValue);
    }
  }

  static Identifier newIdentifier(final Long longValue) {
    if (longValue == null) {
      return null;
    } else {
      return newIdentifier(longValue.longValue());
    }
  }

  static Identifier newIdentifier(final Object... values) {
    if (values == null || values.length == 0) {
      return null;
    } else if (values.length == 1) {
      final Object value = values[0];
      return newIdentifier(value);
    } else {
      return new ListIdentifier(values);
    }
  }

  static Identifier newIdentifier(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Long) {
      final long longValue = (Long)value;
      return newIdentifier(longValue);
    } else if (Numbers.isPrimitiveIntegral(value)) {
      final Number number = (Number)value;
      final int intValue = number.intValue();
      return newIdentifier(intValue);
    } else if (value instanceof Identifier) {
      return (Identifier)value;
    } else if (value instanceof Collection) {
      final Collection<?> idValues = (Collection<?>)value;
      return new ListIdentifier(idValues);
    } else {
      return new SingleIdentifier(value);
    }
  }

  static Identifier newInteger(final String id) {
    final int intValue = Integer.parseInt(id);
    return new IntegerIdentifier(intValue);
  }

  static <V> TreeMap<Identifier, V> newTreeMap() {
    return new TreeMap<>(COMPARATOR);
  }

  static <V> Map<Identifier, V> newTreeMap(final Map<Identifier, ? extends V> map) {
    final Map<Identifier, V> treeMap = newTreeMap();
    if (map != null) {
      treeMap.putAll(map);
    }
    return treeMap;
  }

  static TreeSet<Identifier> newTreeSet() {
    return new TreeSet<>(COMPARATOR);
  }

  static TreeSet<Identifier> newTreeSet(final Iterable<Identifier> values) {
    final TreeSet<Identifier> treeSet = newTreeSet();
    if (values != null) {
      for (final Identifier id : values) {
        treeSet.add(id);
      }
    }
    return treeSet;
  }

  static void setIdentifier(final Map<String, Object> record, final List<String> idFieldNames,
    final Identifier identifier) {
    if (identifier == null) {
      for (final String idFieldName : idFieldNames) {
        record.put(idFieldName, null);
      }
    } else {
      identifier.setIdentifier(record, idFieldNames);
    }
  }

  default int compareTo(final Identifier identifier2) {
    if (identifier2 == this) {
      return 0;
    } else if (identifier2 == null) {
      return -1;
    } else {
      final int valueCount1 = getValueCount();
      final int valueCount2 = identifier2.getValueCount();
      final int valueCount = Math.min(valueCount1, valueCount2);
      for (int i = 0; i < valueCount; i++) {
        final Object value1 = getValue(i);
        final Object value2 = identifier2.getValue(i);
        final int compare = CompareUtil.compare(value1, value2);
        if (compare != 0) {
          return compare;
        }
      }
      return Integer.compare(valueCount1, valueCount2);
    }
  }

  boolean equals(Identifier identifier);

  default Integer getInteger(final int index) {
    final Object value = getValue(index);
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.intValue();
    } else {
      return Integer.valueOf(value.toString());
    }
  }

  default Long getLong(final int index) {
    final Object value = getValue(index);
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.longValue();
    } else {
      return Long.valueOf(value.toString());
    }
  }

  default String getString(final int index) {
    final Object value = getValue(index);
    if (value == null) {
      return null;
    } else {
      return DataTypes.toString(value);
    }
  }

  <V> V getValue(final int index);

  default int getValueCount() {
    return 1;
  }

  List<Object> getValues();

  default boolean isSingle() {
    return true;
  }

  default void setIdentifier(final Map<String, Object> record, final List<String> fieldNames) {
    if (fieldNames.size() == getValueCount()) {
      for (int i = 0; i < fieldNames.size(); i++) {
        final String fieldName = fieldNames.get(i);
        final Object value = getValue(i);
        record.put(fieldName, value);
      }
    } else {
      throw new IllegalArgumentException(
        "Field names count for " + fieldNames + " != count for values " + this);
    }
  }

  default void setIdentifier(final Map<String, Object> record, final String... fieldNames) {
    setIdentifier(record, Arrays.asList(fieldNames));
  }

  default String toIdString() {
    return toString();
  }

  @SuppressWarnings("unchecked")
  default <V> V toSingleValue() {
    final int valueCount = getValueCount();
    if (valueCount == 0) {
      return null;
    } else if (valueCount == 1) {
      return (V)getValue(0);
    } else {
      throw new IllegalArgumentException(
        "Cannot create value for identifier with multiple parts " + this);
    }
  }
}
