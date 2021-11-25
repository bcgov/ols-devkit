package org.jeometry.common.data.type;

import java.util.Arrays;
import java.util.Collection;

public interface DataType {
  static boolean equal(final Object object1, final Object object2) {
    final DataType dataType = DataTypes.getDataType(object1);
    return dataType.equals(object1, object2);
  }

  static boolean equal(final Object object1, final Object object2, final CharSequence... exclude) {
    return equal(object1, object2, Arrays.asList(exclude));
  }

  static boolean equal(final Object object1, final Object object2,
    final Collection<? extends CharSequence> excludeFieldNames) {
    if (object1 == null) {
      return object2 == null;
    } else if (object2 == null) {
      return false;
    } else if (object1 instanceof DataTypedValue) {
      final DataTypedValue value1 = (DataTypedValue)object1;
      return value1.equals(object2, excludeFieldNames);
    } else {
      final DataType dataType = DataTypes.getDataType(object1);
      return dataType.equals(object1, object2, excludeFieldNames);
    }
  }

  default int compareNullFirst(final Object object1, final Object object2) {
    if (object1 == null) {
      if (object2 == null) {
        return 0;
      } else {
        return -1;
      }
    } else {
      if (object2 == null) {
        return 1;
      } else {
        try {
          final Comparable<Object> value1 = toObject(object1);
          final Object value2 = toObject(object2);

          return value1.compareTo(value2);
        } catch (final Throwable e) {
          return object1.toString().compareTo(object2.toString());
        }
      }
    }
  }

  boolean equals(final Object object1, final Object object2);

  default boolean equals(final Object object1, final Object object2,
    final Collection<? extends CharSequence> excludeFieldNames) {
    return equals(object1, object2);
  }

  Class<?> getJavaClass();

  default <V> V getMaxValue() {
    return null;
  }

  default <V> V getMinValue() {
    return null;
  }

  String getName();

  default String getValidationName() {
    if (Number.class.isAssignableFrom(getJavaClass())) {
      return "number (" + getName() + ")";
    } else {
      return getName();
    }
  }

  boolean isAssignableFrom(Class<?> valueClass);

  boolean isAssignableTo(Class<?> valueClass);

  default boolean isInstance(final Object value) {
    final Class<?> javaClass = getJavaClass();
    return javaClass.isInstance(value);
  }

  default boolean isNumeric() {
    final Class<?> javaClass = getJavaClass();
    return Number.class.isAssignableFrom(javaClass);
  }

  default boolean isRequiresQuotes() {
    return true;
  }

  @SuppressWarnings("unchecked")
  default <V> V toObject(final Object value) {
    return (V)value;
  }

  @SuppressWarnings("unchecked")
  default <V> V toObject(final String value) {
    return (V)toObject((Object)value);
  }

  default String toString(final Object value) {
    if (value == null) {
      return null;
    } else {
      return value.toString();
    }
  }
}
