package org.jeometry.common.data.type;

import java.util.Arrays;
import java.util.Collection;

public interface DataTypeProxy {
  default boolean equals(final Object value1, final Object value2) {
    if (value1 == null) {
      return value2 == null;
    } else if (value2 == null) {
      return false;
    } else {
      final DataType dataType = getDataType();
      return dataType.equals(value1, value2);
    }
  }

  default boolean equals(final Object value1, final Object value2,
    final CharSequence... excludeFieldNames) {
    if (value1 == null) {
      return value2 == null;
    } else if (value2 == null) {
      return false;
    } else {
      final DataType dataType = getDataType();
      return dataType.equals(value1, value2, Arrays.asList(excludeFieldNames));
    }
  }

  default boolean equals(final Object value1, final Object value2,
    final Collection<? extends CharSequence> excludeFieldNames) {
    if (value1 == null) {
      return value2 == null;
    } else if (value2 == null) {
      return false;
    } else {
      final DataType dataType = getDataType();
      return dataType.equals(value1, value2, excludeFieldNames);
    }
  }

  DataType getDataType();
}
